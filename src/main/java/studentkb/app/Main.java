package studentkb.app;

import studentkb.db.Database;
import studentkb.util.FxUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Database.initialize();

        FXMLLoader loader = FxUtil.loader(Views.HOME);
        Scene scene = FxUtil.scene(loader.load(), 800, 800);
        stage.setTitle(Views.APP_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
