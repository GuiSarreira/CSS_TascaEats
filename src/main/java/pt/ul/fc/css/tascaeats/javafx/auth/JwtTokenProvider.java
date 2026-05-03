package pt.ul.fc.css.tascaeats.javafx.auth;

import java.util.Base64;

/**
 * Provedor de tokens JWT para TascaEats
 * 
 * Simples implementação JWT sem dependências externas (para v1.1)
 * Em produção, usar biblioteca especializada como jjwt ou nimbus-jose-jwt
 */
public class JwtTokenProvider {

    // Em produção, carregar de configuração segura (environment variables)
    private static final String SECRET_KEY = "TascaEats-Super-Secret-Key-v1-Do-Not-Use-In-Production";
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 horas em ms
    private static final String ALGORITHM = "HS256";

    /**
     * Gerar novo token JWT
     */
    public String generateToken(String userId, String email) {
        try {
            long currentTimeMs = System.currentTimeMillis();
            long expirationTimeMs = currentTimeMs + EXPIRATION_TIME;

            // Header
            String headerJson = "{\"alg\":\"" + ALGORITHM + "\",\"typ\":\"JWT\"}";
            String header = Base64.getEncoder().encodeToString(headerJson.getBytes());

            // Payload
            String payloadJson = "{\"sub\":\"" + userId + "\",\"email\":\"" + email + "\"," +
                    "\"iat\":" + (currentTimeMs / 1000) + "," +
                    "\"exp\":" + (expirationTimeMs / 1000) + "," +
                    "\"iss\":\"tascaeats-app\"," +
                    "\"aud\":\"tascaeats-client\"}";
            String payload = Base64.getEncoder().encodeToString(payloadJson.getBytes());

            // Signature (Simples HMAC-SHA256 usando Base64)
            String signature = generateSignature(header + "." + payload);

            String token = header + "." + payload + "." + signature;
            System.out.println("[JWT] Token gerado para: " + email);

            return token;

        } catch (Exception e) {
            System.err.println("[JWT ERROR] Falha ao gerar token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validar token JWT
     */
    public boolean isTokenValid(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }

            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                System.err.println("[JWT] Token invalido - estrutura incorreta");
                return false;
            }

            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            // Verificar assinatura
            String expectedSignature = generateSignature(header + "." + payload);
            if (!signature.equals(expectedSignature)) {
                System.err.println("[JWT] Token invalido - assinatura nao corresponde");
                return false;
            }

            // Verificar expiração
            String decodedPayload = new String(Base64.getDecoder().decode(payload));
            if (decodedPayload.contains("\"exp\":")) {
                int expStart = decodedPayload.indexOf("\"exp\":") + 6;
                int expEnd = decodedPayload.indexOf(",", expStart);
                if (expEnd == -1) {
                    expEnd = decodedPayload.indexOf("}", expStart);
                }

                long expirationTime = Long.parseLong(decodedPayload.substring(expStart, expEnd));
                long currentTime = System.currentTimeMillis() / 1000;

                if (currentTime > expirationTime) {
                    System.err.println("[JWT] Token expirado");
                    return false;
                }
            }

            System.out.println("[JWT] Token valido");
            return true;

        } catch (Exception e) {
            System.err.println("[JWT ERROR] Erro ao validar token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrair userId do token
     */
    public String extractUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String decodedPayload = new String(Base64.getDecoder().decode(parts[1]));
            int subStart = decodedPayload.indexOf("\"sub\":\"") + 7;
            int subEnd = decodedPayload.indexOf("\"", subStart);

            return decodedPayload.substring(subStart, subEnd);

        } catch (Exception e) {
            System.err.println("[JWT ERROR] Erro ao extrair userId: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extrair email do token
     */
    public String extractEmail(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String decodedPayload = new String(Base64.getDecoder().decode(parts[1]));
            int emailStart = decodedPayload.indexOf("\"email\":\"") + 9;
            int emailEnd = decodedPayload.indexOf("\"", emailStart);

            return decodedPayload.substring(emailStart, emailEnd);

        } catch (Exception e) {
            System.err.println("[JWT ERROR] Erro ao extrair email: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gerar assinatura HMAC-SHA256 (simples para demo)
     * 
     * Nota: Esta é uma implementação simplificada.
     * Em produção, usar javax.crypto.Mac com algoritmo HmacSHA256
     */
    private String generateSignature(String data) {
        try {
            // Para demo, usar simples hash Base64 com secret
            String toSign = data + "." + SECRET_KEY;
            String hash = Integer.toHexString(toSign.hashCode());
            return Base64.getEncoder().encodeToString(hash.getBytes());
        } catch (Exception e) {
            System.err.println("[JWT ERROR] Erro ao gerar assinatura: " + e.getMessage());
            return "";
        }
    }

    /**
     * Obter tempo restante do token em minutos
     */
    public long getTokenExpirationMinutes(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return -1;
            }

            String decodedPayload = new String(Base64.getDecoder().decode(parts[1]));
            int expStart = decodedPayload.indexOf("\"exp\":") + 6;
            int expEnd = decodedPayload.indexOf(",", expStart);
            if (expEnd == -1) {
                expEnd = decodedPayload.indexOf("}", expStart);
            }

            long expirationTime = Long.parseLong(decodedPayload.substring(expStart, expEnd));
            long currentTime = System.currentTimeMillis() / 1000;
            long remainingSeconds = expirationTime - currentTime;

            return remainingSeconds / 60;

        } catch (Exception e) {
            System.err.println("[JWT ERROR] Erro ao calcular expiração: " + e.getMessage());
            return -1;
        }
    }
}
