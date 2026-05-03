package pt.ul.fc.css.tascaeats.javafx.util;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Configuração comum da janela JavaFX para login e aplicação principal.
 */
public final class WindowManager {

    private WindowManager() {
    }

    public static void configureLoginStage(Stage stage, Scene scene) {
        if (stage == null || scene == null) {
            return;
        }

        stage.setResizable(true);
        stage.setMinWidth(460);
        stage.setMinHeight(580);
        stage.setMaximized(false);
        installFullscreenShortcut(stage, scene);
        stage.centerOnScreen();
    }

    public static void configureMainStage(Stage stage, Scene scene) {
        if (stage == null || scene == null) {
            return;
        }

        stage.setResizable(true);
        stage.setMinWidth(1120);
        stage.setMinHeight(720);
        stage.setFullScreenExitHint("Pressione ESC ou F11 para sair do ecrã inteiro");
        installFullscreenShortcut(stage, scene);

        if (stage.getWidth() < 1120 || stage.getHeight() < 720) {
            stage.setWidth(1320);
            stage.setHeight(860);
        }
    }

    public static void configureDialogStage(Stage stage) {
        if (stage == null) {
            return;
        }

        stage.setResizable(true);
        stage.setMinWidth(640);
        stage.setMinHeight(540);
        stage.centerOnScreen();
    }

    private static void installFullscreenShortcut(Stage stage, Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
                event.consume();
            }
        });
    }
}