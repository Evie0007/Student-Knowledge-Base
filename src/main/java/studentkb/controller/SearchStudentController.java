package studentkb.controller;

import studentkb.app.Views;
import studentkb.db.Database;
import studentkb.model.StudentProfile;
import studentkb.util.FxUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchStudentController {

    @FXML private TextField nameField;
    @FXML private TextField roleField;
    @FXML private TextField languagesField;
    @FXML private TextField academicStatusField;
    @FXML private TextField jobStatusField;
    @FXML private TextField databasesField; // search by databases too

    @FXML private TableView<StudentProfile> table;
    @FXML private TableColumn<StudentProfile, String>  nameCol;
    @FXML private TableColumn<StudentProfile, String>  roleCol;
    @FXML private TableColumn<StudentProfile, String>  academicCol;
    @FXML private TableColumn<StudentProfile, String>  jobStatusCol;
    @FXML private TableColumn<StudentProfile, String>  jobDetailsCol;
    @FXML private TableColumn<StudentProfile, String>  languagesCol;
    @FXML private TableColumn<StudentProfile, String>  databasesCol;
    @FXML private TableColumn<StudentProfile, String>  commentsCol;
    @FXML private TableColumn<StudentProfile, Boolean> whitelistCol;
    @FXML private TableColumn<StudentProfile, Boolean> blacklistCol;

    private final ObservableList<StudentProfile> backing = FXCollections.observableArrayList();
    private FilteredList<StudentProfile> filtered;
    private SortedList<StudentProfile> sorted;

    private Map<Integer, String> latestCommentCache = new HashMap<>();

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStudentName()));
        roleCol.setCellValueFactory(cd -> new SimpleStringProperty(nvl(cd.getValue().getPreferredRole())));
        academicCol.setCellValueFactory(cd -> new SimpleStringProperty(nvl(cd.getValue().getAcademicStatus())));
        jobStatusCol.setCellValueFactory(cd -> new SimpleStringProperty(nvl(cd.getValue().getJobStatus())));
        jobDetailsCol.setCellValueFactory(cd -> new SimpleStringProperty(nvl(cd.getValue().getJobDetails())));
        languagesCol.setCellValueFactory(cd -> new SimpleStringProperty(nvl(cd.getValue().getLanguagesKnown())));
        databasesCol.setCellValueFactory(cd -> new SimpleStringProperty(nvl(cd.getValue().getKnownDatabases())));


        commentsCol.setCellValueFactory(cd ->
                new SimpleStringProperty(displayComment(cd.getValue()))
        );

        whitelistCol.setCellValueFactory(cd -> new SimpleBooleanProperty(cd.getValue().isWhitelisted()));
        blacklistCol.setCellValueFactory(cd -> new SimpleBooleanProperty(cd.getValue().isBlacklisted()));
        whitelistCol.setCellFactory(CheckBoxTableCell.forTableColumn(whitelistCol));
        blacklistCol.setCellFactory(CheckBoxTableCell.forTableColumn(blacklistCol));
        whitelistCol.setEditable(false);
        blacklistCol.setEditable(false);

        filtered = new FilteredList<>(backing, sp -> true);
        sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.setPlaceholder(new Label("No matching students"));

        nameCol.setSortType(TableColumn.SortType.ASCENDING);
        table.getSortOrder().setAll(nameCol);

        nameField.setOnAction(e -> applyFilter());
        roleField.setOnAction(e -> applyFilter());
        languagesField.setOnAction(e -> applyFilter());
        academicStatusField.setOnAction(e -> applyFilter());
        jobStatusField.setOnAction(e -> applyFilter());
        if (databasesField != null) databasesField.setOnAction(e -> applyFilter());

        nameField.textProperty().addListener((o,ov,nv) -> applyFilter());
        roleField.textProperty().addListener((o,ov,nv) -> applyFilter());
        languagesField.textProperty().addListener((o,ov,nv) -> applyFilter());
        academicStatusField.textProperty().addListener((o,ov,nv) -> applyFilter());
        jobStatusField.textProperty().addListener((o,ov,nv) -> applyFilter());
        if (databasesField != null) databasesField.textProperty().addListener((o,ov,nv) -> applyFilter());

        reloadAsync();
    }

    private String displayComment(StudentProfile sp) {
        String faculty = nvl(sp.getCommentsFaculty());
        if (!faculty.isEmpty()) return faculty;
        String latest = latestCommentCache.get(sp.getId());
        return latest == null ? "" : latest;
    }

    @FXML
    private void searchStudents(ActionEvent e) {
        applyFilter();
    }

    @FXML
    private void clearSearch(ActionEvent e) {
        nameField.clear();
        roleField.clear();
        languagesField.clear();
        academicStatusField.clear();
        jobStatusField.clear();
        if (databasesField != null) databasesField.clear();
        filtered.setPredicate(sp -> true);
        if (!table.getSortOrder().isEmpty()) table.sort();
    }

    @FXML
    private void showAllStudents(ActionEvent e) {
        clearSearch(e);
        reloadAsync();
    }

    @FXML
    private void deleteSelectedStudent(ActionEvent e) {
        StudentProfile sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                Database.deleteStudent(sel.getId());
                return null;
            }
        };
        task.setOnSucceeded(ev -> backing.remove(sel));
        task.setOnFailed(ev -> FxUtil.error("DB Error", task.getException().getMessage()));
        start(task, "delete-student");
    }

    @FXML
    private void editSelectedStudent(ActionEvent e) {
        StudentProfile sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            javafx.fxml.FXMLLoader loader = FxUtil.loader(Views.EDIT_STUDENT);
            javafx.scene.Parent root = loader.load();
            EditStudentProfileController ctrl = loader.getController();
            ctrl.setStudent(sel);
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            stage.setScene(FxUtil.scene(root));
            stage.setTitle("Edit Student Profile");
            stage.show();
        } catch (Exception ex) {
            FxUtil.error("Load Error", ex.getMessage());
        }
    }

    @FXML
    private void onBack(ActionEvent e) {
        FxUtil.switchScene(e, Views.HOME, Views.APP_TITLE);
    }

    private void applyFilter() {
        final String qName      = lc(nameField.getText());
        final String qRole      = lc(roleField.getText());
        final String qLangs     = lc(languagesField.getText());
        final String qAcademic  = lc(academicStatusField.getText());
        final String qJobStatus = lc(jobStatusField.getText());
        final String qDbs       = databasesField == null ? "" : lc(databasesField.getText());

        filtered.setPredicate(sp -> {
            if (!contains(sp.getStudentName(),     qName))     return false;
            if (!contains(sp.getPreferredRole(),   qRole))     return false;
            if (!contains(sp.getAcademicStatus(),  qAcademic)) return false;
            if (!contains(sp.getJobStatus(),       qJobStatus))return false;
            if (!contains(sp.getLanguagesKnown(),  qLangs))    return false;
            if (!contains(sp.getKnownDatabases(),  qDbs))      return false;
            return true;
        });

        if (!table.getSortOrder().isEmpty()) table.sort();
    }

    private void reloadAsync() {
        StudentProfile prev = table.getSelectionModel().getSelectedItem();

        Task<List<StudentProfile>> task = new Task<>() {
            @Override protected List<StudentProfile> call() throws Exception {
                return Database.getAllStudents();
            }
        };
        Task<Map<Integer,String>> latestTask = new Task<>() {
            @Override protected Map<Integer, String> call() throws Exception {
                return Database.getLatestCommentsForAllStudents();
            }
        };

        task.setOnSucceeded(ev -> {
            backing.setAll(task.getValue());
            if (!table.getSortOrder().isEmpty()) table.sort();
            if (prev != null) {
                backing.stream().filter(sp -> sp.getId() == prev.getId()).findFirst()
                        .ifPresent(sp -> table.getSelectionModel().select(sp));
            }
            table.refresh();
        });
        task.setOnFailed(ev -> FxUtil.error("DB Error", task.getException().getMessage()));

        latestTask.setOnSucceeded(ev -> {
            latestCommentCache = latestTask.getValue();
            table.refresh();
        });
        latestTask.setOnFailed(ev -> latestCommentCache = new HashMap<>());

        start(task, "load-students");
        start(latestTask, "load-latest-comments");
    }

    private static void start(Task<?> task, String name) {
        Thread t = new Thread(task, name);
        t.setDaemon(true);
        t.start();
    }

    private static String nvl(String s) { return s == null ? "" : s; }
    private static String lc(String s) { return s == null ? "" : s.trim().toLowerCase(); }
    private static boolean contains(String haystack, String needleLc) {
        if (needleLc.isEmpty()) return true;
        String h = haystack == null ? "" : haystack.toLowerCase();
        return h.contains(needleLc);
    }
}
