package pt.ul.fc.css.tascaeats.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pt.ul.fc.css.tascaeats.javafx.util.WindowManager;

/**
 * Aplicação JavaFX TascaEats — Entry Point para Fase G
 * 
 * Esta aplicação utiliza gRPC para comunicar com o backend Spring Boot:
 * - REST API: http://localhost:8082
 * - gRPC Server: localhost:9092
 * 
 * O JavaFX roda como cliente desktop nativo, conectando-se ao servidor gRPC.
 */
public class TascaEatsFXApp extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("[TascaEatsFXApp] Iniciar aplicacao JavaFX...");

        try {
            // Carregar login.fxml com fallback para diferentes classloaders.
            java.net.URL fxmlUrl = resolveResource("fxml/login.fxml");
            if (fxmlUrl == null) {
                throw new IllegalStateException("Recurso FXML não encontrado: fxml/login.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root, 500, 600);

            primaryStage.setTitle("TascaEats — Login");
            primaryStage.setScene(scene);
            WindowManager.configureLoginStage(primaryStage, scene);
            primaryStage.show();

            System.out.println("[TascaEatsFXApp] Aplicacao iniciada com sucesso");

        } catch (Exception e) {
            System.err.println("[TascaEatsFXApp] Erro ao carregar aplicacao: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private java.net.URL resolveResource(String path) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            java.net.URL url = contextClassLoader.getResource(path);
            if (url != null) {
                return url;
            }
        }

        java.net.URL url = TascaEatsFXApp.class.getResource("/" + path);
        if (url != null) {
            return url;
        }

        url = TascaEatsFXApp.class.getClassLoader().getResource(path);
        if (url != null) {
            return url;
        }

        // Fallback when classpath resolution is unstable under javafx:run on Windows.
        java.nio.file.Path fromTarget = java.nio.file.Paths.get("target", "classes", path);
        if (java.nio.file.Files.exists(fromTarget)) {
            try {
                return fromTarget.toUri().toURL();
            } catch (java.net.MalformedURLException ignored) {
            }
        }

        java.nio.file.Path fromSource = java.nio.file.Paths.get("src", "main", "resources", path);
        if (java.nio.file.Files.exists(fromSource)) {
            try {
                return fromSource.toUri().toURL();
            } catch (java.net.MalformedURLException ignored) {
            }
        }

        return null;
    }

    @Override
    public void stop() throws Exception {
        // Encerrar aplicação
        System.exit(0);
    }
}
