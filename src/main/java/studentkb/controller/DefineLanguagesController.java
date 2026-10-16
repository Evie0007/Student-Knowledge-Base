package studentkb.controller;

import studentkb.app.Views;
import studentkb.db.Database;
import studentkb.model.ProgrammingLanguage;
import studentkb.util.FxUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

public class DefineLanguagesController {

    @FXML private TextField nameField;
    @FXML private ListView<ProgrammingLanguage> languagesList;
    @FXML private Button addButton;
    @FXML private Button deleteButton;

    private final ObservableList<ProgrammingLanguage> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        languagesList.setItems(data);
        refresh();
        languagesList.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            deleteButton.setDisable(val == null);
        });
    }

    private void refresh() {
        try {
            List<ProgrammingLanguage> langs = Database.getAllLanguages();
            data.setAll(langs);
            int count = langs.size();
        } catch (SQLException e) {
            FxUtil.error("DB Error", e.getMessage());
        }
    }

    @FXML
    private void onAddLanguage() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) {
            FxUtil.error("Validation", "Language name cannot be empty.");
            return;
        }
        try {
            boolean ok = Database.addLanguage(name);
            if (!ok) {
                FxUtil.error("Add Failed", "Language existed");
            } else {
                nameField.clear();
                refresh();
            }
        } catch (SQLException e) {
            FxUtil.error("DB Error", e.getMessage());
        }
    }

    @FXML
    private void onDeleteLanguage() {
        ProgrammingLanguage sel = languagesList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            Database.deleteLanguage(sel.getId());
            refresh();
        } catch (SQLException e) {
            FxUtil.error("DB Error", e.getMessage());
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        FxUtil.switchScene(event, Views.HOME, Views.APP_TITLE);
    }
}
