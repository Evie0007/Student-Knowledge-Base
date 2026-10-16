module studentkb {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;

    exports studentkb.app;
    exports studentkb.controller;
    exports studentkb.db;
    exports studentkb.model;
    exports studentkb.util;

    opens studentkb.app to javafx.fxml;
    opens studentkb.controller to javafx.fxml;
    opens studentkb.model to javafx.base;
}
