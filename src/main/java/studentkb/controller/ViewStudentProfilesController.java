package studentkb.controller;

import studentkb.app.Views;
import studentkb.db.Database;
import studentkb.model.StudentProfile;
import studentkb.util.FxUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ViewStudentProfilesController {

    @FXML private TableView<StudentProfile> table;
    @FXML private TableColumn<StudentProfile, String> nameCol;
    @FXML private TableColumn<StudentProfile, String> roleCol;
    @FXML private TableColumn<StudentProfile, String> academicCol;
    @FXML private TableColumn<StudentProfile, String> jobStatusCol;
    @FXML private TableColumn<StudentProfile, String> jobDetailsCol;
    @FXML private TableColumn<StudentProfile, String> languagesCol;
    @FXML private TableColumn<StudentProfile, String> databaseCol;
    @FXML private TableColumn<StudentProfile, String> commentsCol;
    @FXML private TableColumn<StudentProfile, Boolean> whitelistCol;
    @FXML private TableColumn<StudentProfile, Boolean> blacklistCol;
    @FXML private Button backButton;

    private final ObservableList<StudentProfile> data = FXCollections.observableArrayList();

    private Map<Integer, String> latestCommentCache = new HashMap<>();

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("preferredRole"));
        academicCol.setCellValueFactory(new PropertyValueFactory<>("academicStatus"));
        jobStatusCol.setCellValueFactory(new PropertyValueFactory<>("jobStatus"));
        jobDetailsCol.setCellValueFactory(new PropertyValueFactory<>("jobDetails"));
        languagesCol.setCellValueFactory(new PropertyValueFactory<>("languagesKnown"));
        databaseCol.setCellValueFactory(new PropertyValueFactory<>("knownDatabases"));

        commentsCol.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(displayComment(cd.getValue()))
        );

        whitelistCol.setCellValueFactory(cd -> new SimpleBooleanProperty(cd.getValue().isWhitelisted()));
        blacklistCol.setCellValueFactory(cd -> new SimpleBooleanProperty(cd.getValue().isBlacklisted()));
        whitelistCol.setCellFactory(CheckBoxTableCell.forTableColumn(whitelistCol));
        blacklistCol.setCellFactory(CheckBoxTableCell.forTableColumn(blacklistCol));
        whitelistCol.setEditable(false);
        blacklistCol.setEditable(false);

        nameCol.setComparator(String::compareToIgnoreCase);
        table.setItems(data);

        refresh();
    }

    private String displayComment(StudentProfile sp) {
        String faculty = sp.getCommentsFaculty() == null ? "" : sp.getCommentsFaculty();
        if (!faculty.isBlank()) return faculty;
        String latest = latestCommentCache.get(sp.getId());
        return latest == null ? "" : latest;
    }

    private void refresh() {
        try {
            List<StudentProfile> all = Database.getAllStudents();
            latestCommentCache = Database.getLatestCommentsForAllStudents(); // load fallback map
            data.setAll(all);

            table.getSortOrder().setAll(nameCol);
            nameCol.setSortType(TableColumn.SortType.ASCENDING);
            table.sort();
        } catch (SQLException e) {
            FxUtil.error("DB Error", e.getMessage());
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        FxUtil.switchScene(event, Views.HOME, Views.APP_TITLE);
    }
}
