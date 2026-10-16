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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EditStudentProfileController {

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
    private final ObservableList<String> dbOptions =
            FXCollections.observableArrayList("MySQL", "Postgres", "MongoDB", "SQLite");
    private final ObservableList<String> comments = FXCollections.observableArrayList();

    private StudentProfile original;

    @FXML
    public void initialize() {
        roleChoice.setItems(FXCollections.observableArrayList("Front-End", "Back-End", "Full-Stack", "Data", "Other"));
        academicChoice.setItems(FXCollections.observableArrayList("Freshman", "Sophomore", "Junior", "Senior", "Graduate"));

        employedRadio.setToggleGroup(jobGroup);
        notEmployedRadio.setToggleGroup(jobGroup);

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

        try {
            langs.setAll(Database.getAllLanguages());
        } catch (SQLException e) {
            FxUtil.error("DB Error", e.getMessage());
        }

        whitelistCheck.selectedProperty().addListener((obs, oldV, v) -> {
            if (v) blacklistCheck.setSelected(false);
        });
        blacklistCheck.selectedProperty().addListener((obs, oldV, v) -> {
            if (v) whitelistCheck.setSelected(false);
        });
    }

    public void setStudent(StudentProfile sp) {
        this.original = sp;

        nameField.setText(sp.getStudentName());
        roleChoice.getSelectionModel().select(sp.getPreferredRole());
        academicChoice.getSelectionModel().select(sp.getAcademicStatus());

        boolean employed = "Employed".equalsIgnoreCase(sp.getJobStatus());
        (employed ? employedRadio : notEmployedRadio).setSelected(true);
        jobDetailsArea.setDisable(!employed);
        jobDetailsArea.setText(sp.getJobDetails() == null ? "" : sp.getJobDetails());


        List<String> langNames = Arrays.stream((sp.getLanguagesKnown() == null ? "" : sp.getLanguagesKnown())
                        .split("\\s*,\\s*"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
        langs.stream().filter(pl -> langNames.contains(pl.getName()))
                .forEach(pl -> languagesList.getSelectionModel().select(pl));


        List<String> dbs = Arrays.stream((sp.getKnownDatabases() == null ? "" : sp.getKnownDatabases())
                        .split("\\s*,\\s*"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
        dbOptions.stream().filter(dbs::contains)
                .forEach(db -> databasesList.getSelectionModel().select(db));

        whitelistCheck.setSelected(sp.isWhitelisted());
        blacklistCheck.setSelected(sp.isBlacklisted());

        refreshComments();
    }

    private void refreshComments() {
        comments.clear();
        if (original == null) return;
        try {
            List<String> fromDb = Database.getCommentsForStudentDesc(original.getId());
            comments.addAll(fromDb); // newest first
        } catch (SQLException e) {
            FxUtil.error("DB Error", e.getMessage());
        }
    }

    @FXML
    private void onAddComment(ActionEvent event) {
        if (original == null) return;
        String raw = commentArea.getText() == null ? "" : commentArea.getText().trim();
        if (raw.isEmpty()) return;

        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String content = stamp + " — " + raw;

        try {
            Database.addStudentComment(original.getId(), content);
            commentArea.clear();
            comments.add(0, content); // newest on top
        } catch (SQLException e) {
            FxUtil.error("DB Error", e.getMessage());
        }
    }

    @FXML
    private void onSave(ActionEvent e) {
        if (original == null) return;

        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) { FxUtil.error("Validation", "Student name is required."); return; }

        boolean employed = jobGroup.getSelectedToggle() == employedRadio;
        String jobStatus = employed ? "Employed" : "Not Employed";
        String jobDetails = jobDetailsArea.getText() == null ? "" : jobDetailsArea.getText().trim();

        String langsCsv = languagesList.getSelectionModel().getSelectedItems()
                .stream().map(ProgrammingLanguage::getName).collect(Collectors.joining(", "));
        String dbsCsv = String.join(", ", databasesList.getSelectionModel().getSelectedItems());


        String facultyComments = original.getCommentsFaculty() == null ? "" : original.getCommentsFaculty();

        StudentProfile updated = new StudentProfile(
                original.getId(),
                name,
                roleChoice.getValue(),
                academicChoice.getValue(),
                jobStatus,
                jobDetails,
                langsCsv,
                whitelistCheck.isSelected(),
                blacklistCheck.isSelected(),
                dbsCsv,
                facultyComments
        );

        try {
            Database.updateStudent(updated);
            FxUtil.info("Saved", "Student profile updated.");
            FxUtil.switchScene(e, Views.SEARCH_STUDENTS, Views.SEARCH_STUDENTS_TITLE);
        } catch (SQLException ex) {
            FxUtil.error("DB Error", ex.getMessage());
        }
    }

    @FXML
    private void onCancel(ActionEvent e) {
        FxUtil.switchScene(e, Views.SEARCH_STUDENTS, Views.SEARCH_STUDENTS_TITLE);
    }
}
