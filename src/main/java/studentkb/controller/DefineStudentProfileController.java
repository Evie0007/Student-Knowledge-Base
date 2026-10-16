package studentkb.controller;

import studentkb.app.Views;
import studentkb.db.Database;
import studentkb.model.ProgrammingLanguage;
import studentkb.model.StudentProfile;
import studentkb.util.FxUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class DefineStudentProfileController {

    @FXML private TextField nameField;
    @FXML private ChoiceBox<String> roleChoice;
    @FXML private ChoiceBox<String> academicChoice;

    @FXML private RadioButton employedRadio;
    @FXML private RadioButton notEmployedRadio;
    private final ToggleGroup jobGroup = new ToggleGroup();

    @FXML private TextArea jobDetailsArea;

    @FXML private ListView<ProgrammingLanguage> languagesList;
    @FXML private ListView<String> databasesList;

    @FXML private TextArea commentArea;
    @FXML private ListView<String> commentsList;

    @FXML private CheckBox whitelistCheck;
    @FXML private CheckBox blacklistCheck;

    private final ObservableList<ProgrammingLanguage> langs = FXCollections.observableArrayList();
    private final ObservableList<String> dbOptions = FXCollections.observableArrayList("MySQL", "Postgres", "MongoDB", "SQLite");
    private final ObservableList<String> comments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        roleChoice.setItems(FXCollections.observableArrayList("Front-End", "Back-End", "Full-Stack", "Data", "Other"));
        roleChoice.getSelectionModel().selectFirst();

        academicChoice.setItems(FXCollections.observableArrayList("Freshman", "Sophomore", "Junior", "Senior", "Graduate"));
        academicChoice.getSelectionModel().selectFirst();

        employedRadio.setToggleGroup(jobGroup);
        notEmployedRadio.setToggleGroup(jobGroup);
        notEmployedRadio.setSelected(true);

        jobDetailsArea.setDisable(true);
        jobGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            boolean employed = jobGroup.getSelectedToggle() == employedRadio;
            jobDetailsArea.setDisable(!employed);
            if (!employed) jobDetailsArea.clear();
        });

        languagesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        languagesList.setItems(langs);

        databasesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        databasesList.setItems(dbOptions);

        commentsList.setItems(comments);

        whitelistCheck.selectedProperty().addListener((obs, oldV, v) -> { if (v) blacklistCheck.setSelected(false); });
        blacklistCheck.selectedProperty().addListener((obs, oldV, v) -> { if (v) whitelistCheck.setSelected(false); });

        refreshLanguages();
    }

    private void refreshLanguages() {
        try {
            langs.setAll(Database.getAllLanguages());
        } catch (SQLException e) {
            FxUtil.error("DB Error", e.getMessage());
        }
    }

    @FXML
    private void onAddComment(ActionEvent event) {
        String raw = commentArea.getText() == null ? "" : commentArea.getText().trim();
        if (raw.isEmpty()) return;
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        comments.add(stamp + " — " + raw);
        commentArea.clear();
    }

    @FXML
    private void onSave(ActionEvent event) {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) { FxUtil.error("Validation", "Student name is required."); return; }
        if (roleChoice.getValue() == null || academicChoice.getValue() == null) {
            FxUtil.error("Validation", "Preferred role and academic status are required."); return;
        }

        boolean employed = jobGroup.getSelectedToggle() == employedRadio;
        String jobDetails = jobDetailsArea.getText() == null ? "" : jobDetailsArea.getText().trim();
        if (employed && jobDetails.isBlank()) { FxUtil.error("Validation", "Job details are required when employed."); return; }

        var selectedLangs = languagesList.getSelectionModel().getSelectedItems();
        if (selectedLangs == null || selectedLangs.isEmpty()) { FxUtil.error("Validation", "Select at least one programming language."); return; }

        var selectedDbs = databasesList.getSelectionModel().getSelectedItems();
        if (selectedDbs == null || selectedDbs.isEmpty()) { FxUtil.error("Validation", "Select at least one database."); return; }

        try {
            if (Database.studentExistsByFullNameIgnoreCase(name)) {
                FxUtil.error("Duplicate", "A profile with this full name already exists."); return;
            }
        } catch (SQLException e) {
            FxUtil.error("DB Error", e.getMessage()); return;
        }

        String langsCsv = selectedLangs.stream().map(ProgrammingLanguage::getName).collect(Collectors.joining(", "));
        String dbsCsv = String.join(", ", selectedDbs);
        String jobStatus = employed ? "Employed" : "Not Employed";

        String commentsFaculty = "";

        StudentProfile sp = new StudentProfile(
                0,
                name,
                roleChoice.getValue(),
                academicChoice.getValue(),
                jobStatus,
                jobDetails,
                langsCsv,
                whitelistCheck.isSelected(),
                blacklistCheck.isSelected(),
                dbsCsv,
                commentsFaculty
        );

        try {
            int newId = Database.insertStudentReturningId(sp);
            Database.setStudentDatabasesCsv(newId, dbsCsv);
            for (String c : comments) Database.addStudentComment(newId, c);

            FxUtil.info("Saved", "Student profile saved.");
            FxUtil.switchScene(event, Views.HOME, Views.APP_TITLE);
        } catch (SQLException e) {
            FxUtil.error("DB Error", e.getMessage());
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        FxUtil.switchScene(event, Views.HOME, Views.APP_TITLE);
    }
}
