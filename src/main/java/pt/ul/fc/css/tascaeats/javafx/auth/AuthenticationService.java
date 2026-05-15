package pt.ul.fc.css.tascaeats.javafx.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço de autenticação JWT para TascaEats
 * 
 * Gerencia tokens JWT, sessões de usuário e autenticação
 * v1.1 - Implementação cliente-side
 */
public class AuthenticationService {

    private static final String USERS_BY_EMAIL_URL = "http://localhost:8082/api/users/email?email=";
    private static final String USERS_FILTER_URL = "http://localhost:8082/api/users/filtros?nome=";

    private static AuthenticationService instance;
    private CurrentUser currentUser;
    private Map<String, Object> sessionContext;
    private JwtTokenProvider tokenProvider;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private AuthenticationService() {
        this.currentUser = null;
        this.sessionContext = new HashMap<>();
        this.tokenProvider = new JwtTokenProvider();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Obter instância singleton
     */
    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * Autenticar usuário com email e password
     * 
     * Nota: Conforme enunciado, qualquer palavra-passe é aceite contanto que o
     * utilizador seja válido.
     * O sistema deve distinguir entre clientes, entregadores e administradores.
     * 
     * @param email    Email do usuário
     * @param password Senha do usuário (qualquer será aceite para usuários válidos)
     * @return true se autenticado com sucesso
     */
    public boolean authenticate(String emailOrUsername, String password) {
        if (emailOrUsername == null || emailOrUsername.trim().isEmpty()) {
            System.err.println("[Auth] Identificador vazio");
            return false;
        }

        if (password == null || password.length() < 1) {
            System.err.println("[Auth] Senha nao pode estar vazia");
            return false;
        }

        String identifier = emailOrUsername.trim();
        String normalizedEmail = sanitizeEmail(identifier);

        // Demo nativa: conta ADMIN
        if (normalizedEmail.equals("admin@example.com")) {
            createSession(normalizedEmail, "demo-admin-001", "ADMIN");
            return true;
        }

        // Demo: Aceitar admin@tascaeats.pt como ADMIN
        if (normalizedEmail.equals("admin@tascaeats.pt")) {
            createSession(normalizedEmail, "admin-001", "ADMIN");
            return true;
        }

        // Demo: Aceitar entregador@tascaeats.pt como ENTREGADOR
        if (normalizedEmail.equals("entregador@tascaeats.pt") || normalizedEmail.equals("entregador@example.com")) {
            createSession(normalizedEmail, "entregador-001", "ENTREGADOR");
            return true;
        }

        ResolvedUser backendUser = resolveUserFromBackend(identifier);
        if (backendUser != null) {
            if ("CLIENTE".equals(backendUser.role)) {
                System.err.println("[Auth] Login bloqueado para CLIENTE na interface nativa: " + backendUser.email);
                return false;
            }
            createSession(backendUser.email, backendUser.userId, backendUser.role);
            return true;
        }

        // Fallback: Aceitar qualquer email com formato válido (simples validação)
        if (normalizedEmail.contains("@") && normalizedEmail.contains(".")) {
            // Determinar tipo baseado em patterns
            String tipo = "CLIENTE";
            if (normalizedEmail.contains("admin"))
                tipo = "ADMIN";
            if (normalizedEmail.contains("entregador"))
                tipo = "ENTREGADOR";

            // Interface nativa só permite ADMIN e ENTREGADOR.
            if ("CLIENTE".equals(tipo)) {
                System.err.println("[Auth] Login bloqueado para CLIENTE na interface nativa: " + normalizedEmail);
                return false;
            }

            createSession(normalizedEmail, "user-" + System.nanoTime(), tipo);
            return true;
        }

        System.err.println("[Auth] Autenticacao falhou para: " + identifier);
        return false;
    }

    private String sanitizeEmail(String rawEmail) {
        String cleaned = rawEmail == null ? "" : rawEmail.trim().toLowerCase();
        while (cleaned.startsWith("-")) {
            cleaned = cleaned.substring(1).trim();
        }
        return cleaned;
    }

    private ResolvedUser resolveUserFromBackend(String emailOrUsername) {
        try {
            if (emailOrUsername.contains("@")) {
                String encodedEmail = URLEncoder.encode(emailOrUsername, StandardCharsets.UTF_8);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(USERS_BY_EMAIL_URL + encodedEmail))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    JsonNode root = objectMapper.readTree(response.body());
                    return toResolvedUser(root);
                }
            }

            String encodedName = URLEncoder.encode(emailOrUsername, StandardCharsets.UTF_8);
            HttpRequest byNameRequest = HttpRequest.newBuilder()
                    .uri(URI.create(USERS_FILTER_URL + encodedName))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> byNameResponse = httpClient.send(byNameRequest, HttpResponse.BodyHandlers.ofString());
            if (byNameResponse.statusCode() >= 200 && byNameResponse.statusCode() < 300) {
                JsonNode users = objectMapper.readTree(byNameResponse.body());
                for (JsonNode user : users) {
                    String nome = user.path("nome").asText("").trim();
                    String email = user.path("email").asText("").trim();
                    String normalized = emailOrUsername.trim().toLowerCase();
                    if (nome.equalsIgnoreCase(emailOrUsername)
                            || email.equalsIgnoreCase(emailOrUsername)
                            || email.toLowerCase().startsWith(normalized + "@")) {
                        return toResolvedUser(user);
                    }
                }
                if (users.isArray() && !users.isEmpty()) {
                    return toResolvedUser(users.get(0));
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private ResolvedUser toResolvedUser(JsonNode node) {
        String role = node.path("role").asText("").trim().toUpperCase();
        String email = sanitizeEmail(node.path("email").asText(""));
        String userId = String.valueOf(node.path("id").asLong(0L));
        if (("ADMIN".equals(role) || "ENTREGADOR".equals(role) || "CLIENTE".equals(role))
                && !email.isBlank() && !"0".equals(userId)) {
            return new ResolvedUser(email, role, userId);
        }
        return null;
    }

    private static class ResolvedUser {
        private final String email;
        private final String role;
        private final String userId;

        private ResolvedUser(String email, String role, String userId) {
            this.email = email;
            this.role = role;
            this.userId = userId;
        }
    }

    /**
     * Criar sessão após autenticação bem-sucedida
     */
    private void createSession(String email, String userId, String userType) {
        String token = tokenProvider.generateToken(userId, email);
        currentUser = new CurrentUser(userId, email, token);

        sessionContext.put("userId", userId);
        sessionContext.put("email", email);
        sessionContext.put("userType", userType);
        sessionContext.put("token", token);
        sessionContext.put("loginTime", System.currentTimeMillis());

        System.out.println("[Auth] Sessao criada para: " + email + " (ID: " + userId + ", Tipo: " + userType + ")");
    }

    /**
     * Fazer logout
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("[Auth] Logout do usuario: " + currentUser.email);
        }
        currentUser = null;
        sessionContext.clear();
    }

    /**
     * Verificar se usuário está autenticado
     */
    public boolean isAuthenticated() {
        return currentUser != null && !currentUser.token.isEmpty();
    }

    /**
     * Obter usuário atual
     */
    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    /**
     * Obter token JWT
     */
    public String getToken() {
        return currentUser != null ? currentUser.token : null;
    }

    /**
     * Verificar se token é válido
     */
    public boolean isTokenValid() {
        if (currentUser == null) {
            return false;
        }
        return tokenProvider.isTokenValid(currentUser.token);
    }

    /**
     * Renovar token
     */
    public boolean refreshToken() {
        if (currentUser == null) {
            return false;
        }

        String newToken = tokenProvider.generateToken(currentUser.userId, currentUser.email);
        if (newToken != null) {
            currentUser.token = newToken;
            sessionContext.put("token", newToken);
            System.out.println("[Auth] Token renovado");
            return true;
        }

        return false;
    }

    /**
     * Dados do usuário autenticado
     */
    public static class CurrentUser {
        public final String userId;
        public final String email;
        public String token;

        public CurrentUser(String userId, String email, String token) {
            this.userId = userId;
            this.email = email;
            this.token = token;
        }

        @Override
        public String toString() {
            return "CurrentUser{" +
                    "userId='" + userId + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

    /**
     * Obter contexto da sessão
     */
    public Map<String, Object> getSessionContext() {
        return new HashMap<>(sessionContext);
    }

    /**
     * Obter tipo de utilizador (CLIENTE, ADMIN, ENTREGADOR)
     */
    public String getUserType() {
        return (String) sessionContext.getOrDefault("userType", "CLIENTE");
    }

    /**
     * Quanto tempo faz que o usuário está autenticado (em segundos)
     */
    public long getSessionDurationSeconds() {
        if (!isAuthenticated()) {
            return 0;
        }
        Object loginTimeObj = sessionContext.get("loginTime");
        if (loginTimeObj instanceof Long) {
            long loginTime = (Long) loginTimeObj;
            return (System.currentTimeMillis() - loginTime) / 1000;
        }
        return 0;
    }
}
