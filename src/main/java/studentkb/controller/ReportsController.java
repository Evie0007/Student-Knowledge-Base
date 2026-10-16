package studentkb.controller;

import studentkb.app.Views;
import studentkb.db.Database;
import studentkb.model.StudentProfile;
import studentkb.util.FxUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsController {

    @FXML private RadioButton allRadio;
    @FXML private RadioButton whitelistedRadio;
    @FXML private RadioButton blacklistedRadio;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    @FXML private TableView<StudentProfile> studentsTable;
    @FXML private TableColumn<StudentProfile, String> nameColumn;
    @FXML private TableColumn<StudentProfile, String> roleColumn;
    @FXML private TableColumn<StudentProfile, String> statusColumn;
    @FXML private TableColumn<StudentProfile, String> jobColumn;
    @FXML private TableColumn<StudentProfile, String> languagesColumn;
    @FXML private TableColumn<StudentProfile, String> commentColumn;

    private final ToggleGroup filterGroup = new ToggleGroup();
    private final ObservableList<StudentProfile> data = FXCollections.observableArrayList();


    private Map<Integer, String> latestCommentCache = new HashMap<>();

    @FXML
    public void initialize() {

        allRadio.setToggleGroup(filterGroup);
        whitelistedRadio.setToggleGroup(filterGroup);
        blacklistedRadio.setToggleGroup(filterGroup);
        allRadio.setSelected(true);
        filterGroup.selectedToggleProperty().addListener((o, ov, nv) -> loadAndFilter());


        nameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("preferredRole"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("academicStatus"));
        jobColumn.setCellValueFactory(new PropertyValueFactory<>("jobStatus"));
        languagesColumn.setCellValueFactory(new PropertyValueFactory<>("languagesKnown"));


        commentColumn.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                        excerpt(displayComment(cd.getValue()), 90)
                ));

        studentsTable.setItems(data);
        studentsTable.setPlaceholder(new Label("No students to show"));


        studentsTable.setRowFactory(tv -> {
            TableRow<StudentProfile> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (!row.isEmpty()
                        && ev.getButton() == MouseButton.PRIMARY
                        && ev.getClickCount() == 2) {
                    openDetailsPopup(row.getItem());
                }
            });
            return row;
        });

        loadAndFilter();
    }

    private String displayComment(StudentProfile sp) {
        String faculty = nv(sp.getCommentsFaculty());
        if (!faculty.isEmpty()) return faculty;
        return nv(latestCommentCache.get(sp.getId()));
    }

    private static String excerpt(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static String nv(String s) { return s == null ? "" : s; }

    @FXML
    private void onRefresh(ActionEvent event) {
        loadAndFilter();
    }

    @FXML
    private void onBack(ActionEvent event) {
        FxUtil.switchScene(event, Views.HOME, Views.APP_TITLE);
    }

    private void loadAndFilter() {
        try {

            List<StudentProfile> list = Database.getAllStudents();


            if (whitelistedRadio.isSelected()) {
                list.removeIf(sp -> !sp.isWhitelisted());
            } else if (blacklistedRadio.isSelected()) {
                list.removeIf(sp -> !sp.isBlacklisted());
            }

            data.setAll(list);


            try {
                latestCommentCache = Database.getLatestCommentsForAllStudents();
            } catch (SQLException ignore) {
                latestCommentCache = new HashMap<>();
            }


            if (!studentsTable.getSortOrder().contains(nameColumn)) {
                nameColumn.setSortType(TableColumn.SortType.ASCENDING);
                studentsTable.getSortOrder().setAll(nameColumn);
            }
            studentsTable.sort();
            studentsTable.refresh();
        } catch (SQLException e) {
            FxUtil.error("DB Error", e.getMessage());
        }
    }

    private void openDetailsPopup(StudentProfile sp) {
        try {
            FXMLLoader loader = FxUtil.loader(Views.PROFILE_DETAILS);
            Parent root = loader.load();

            ProfileDetailsController ctrl = loader.getController();
            ctrl.setStudent(sp);

            Stage popup = new Stage();
            popup.initModality(Modality.NONE);
            popup.setTitle("Student Profile Details");
            popup.setScene(FxUtil.scene(root, 750, 700));
            popup.show();
        } catch (Exception ex) {
            FxUtil.error("Load Error", ex.getMessage());
        }
    }

    @FXML
    private void onFilterChanged(ActionEvent e) {
        loadAndFilter();
    }
}
