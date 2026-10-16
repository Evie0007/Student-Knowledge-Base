package studentkb.controller;

import studentkb.db.Database;
import studentkb.model.StudentProfile;
import studentkb.util.FxUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import studentkb.app.Views;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class ProfileDetailsController {


    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private Label academicLabel;
    @FXML private Label jobLabel;
    @FXML private Label jobDetailsLabel;
    @FXML private Label languagesLabel;
    @FXML private Label databasesLabel;
    @FXML private Label statusLabel;
    @FXML private Label facultyCommentsLabel;


    @FXML private TableView<CommentRow> commentsTable;
    @FXML private TableColumn<CommentRow, String> dateColumn;
    @FXML private TableColumn<CommentRow, String> commentColumn;

    private final ObservableList<CommentRow> commentRows = FXCollections.observableArrayList();
    private StudentProfile student;

    @FXML
    public void initialize() {
        dateColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getDate()));

        commentColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getExcerpt(90)));
        commentsTable.setItems(commentRows);

        commentsTable.setRowFactory(tv -> {
            TableRow<CommentRow> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (!row.isEmpty() && ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 1) {
                    openFullComment(row.getItem().getComment());
                }
            });
            return row;
        });
    }

    public void setStudent(StudentProfile sp) {
        this.student = sp;

        nameLabel.setText(nvl(sp.getStudentName()));
        roleLabel.setText(nvl(sp.getPreferredRole()));
        academicLabel.setText(nvl(sp.getAcademicStatus()));
        jobLabel.setText(nvl(sp.getJobStatus()));
        jobDetailsLabel.setText(nvl(sp.getJobDetails()));
        languagesLabel.setText(nvl(sp.getLanguagesKnown()));
        databasesLabel.setText(nvl(sp.getKnownDatabases()));

        String flags = (sp.isWhitelisted() ? "Whitelisted" : "") +
                ((sp.isWhitelisted() && sp.isBlacklisted()) ? " & " : "") +
                (sp.isBlacklisted() ? "Blacklisted" : "");
        statusLabel.setText(flags.isBlank() ? "—" : flags);

        facultyCommentsLabel.setText(nvl(sp.getCommentsFaculty()));
        loadComments();
    }

    private void loadComments() {
        commentRows.clear();
        if (student == null) return;
        try {
            List<String> comments = Database.getCommentsForStudentDesc(student.getId()); // newest first
            for (String raw : comments) {
                commentRows.add(new CommentRow(extractDate(raw), extractText(raw)));
            }
        } catch (SQLException e) {
            FxUtil.error("DB Error", e.getMessage());
        }
    }

    private void openFullComment(String fullText) {
        try {
            FXMLLoader loader = FxUtil.loader(Views.COMMENTS);
            Parent root = loader.load();
            CommentViewController ctrl = loader.getController();
            ctrl.setComment(fullText);

            Stage popup = new Stage();
            popup.initModality(Modality.NONE); // separate floating window
            popup.setTitle("Comment");
            popup.setScene(FxUtil.scene(root, 600, 400));
            popup.show();
        } catch (Exception ex) {
            FxUtil.error("Load Error", ex.getMessage());
        }
    }


    @FXML
    private void onClose(ActionEvent e) {
        ((Node) e.getSource()).getScene().getWindow().hide();
    }

    private static String nvl(String s) { return s == null || s.isBlank() ? "—" : s; }


    private static String extractDate(String content) {
        if (content == null) return "";
        int idx = content.indexOf("—");
        return idx > 0 ? content.substring(0, idx).trim() : "";
    }
    private static String extractText(String content) {
        if (content == null) return "";
        int idx = content.indexOf("—");
        return (idx >= 0 && idx + 1 < content.length()) ? content.substring(idx + 1).trim() : content.trim();
    }


    public static class CommentRow {
        private final String date;
        private final String comment; // full text

        public CommentRow(String date, String comment) {
            this.date = date;
            this.comment = comment;
        }
        public String getDate() { return date; }
        public String getComment() { return comment; }


        public String getExcerpt(int maxChars) {
            if (comment == null) return "";
            return comment.length() <= maxChars ? comment : comment.substring(0, Math.max(0, maxChars - 1)) + "…";
        }
    }
}
