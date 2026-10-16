package studentkb.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public final class FxUtil {

    private static final String VIEWS_DIR = "/studentkb/views/";
    private static final String STYLESHEET =
            FxUtil.class.getResource("/studentkb/styles/app.css").toExternalForm();

    private FxUtil() {}

    public static FXMLLoader loader(String fxml) {
        return new FXMLLoader(FxUtil.class.getResource(VIEWS_DIR + fxml));
    }

    /** Creates a scene with the app stylesheet applied. Use this instead of {@code new Scene(...)}. */
    public static Scene scene(Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(STYLESHEET);
        return scene;
    }

    public static Scene scene(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(STYLESHEET);
        return scene;
    }

    public static void switchScene(ActionEvent event, String fxml, String title) {
        try {
            FXMLLoader loader = loader(fxml);
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            error("Load Error", "Failed to load: " + fxml + "\n" + e.getMessage());
        }
    }

    public static void info(String title, String content) {
        show(Alert.AlertType.INFORMATION, title, content);
    }

    public static void error(String title, String content) {
        show(Alert.AlertType.ERROR, title, content);
    }

    private static void show(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().getStylesheets().add(STYLESHEET);
        alert.showAndWait();
    }
}
