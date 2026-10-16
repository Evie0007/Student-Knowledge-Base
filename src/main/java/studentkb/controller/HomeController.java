package studentkb.controller;

import studentkb.app.Views;
import studentkb.util.FxUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class HomeController {

    @FXML
    private void onDefineLanguages(ActionEvent event) {
        FxUtil.switchScene(event, Views.DEFINE_LANGUAGES, Views.DEFINE_LANGUAGES_TITLE);
    }

    @FXML
    private void onDefineStudentProfile(ActionEvent event) {
        FxUtil.switchScene(event, Views.DEFINE_STUDENT, Views.DEFINE_STUDENT_TITLE);
    }

    @FXML
    private void onViewStudentProfiles(ActionEvent event) {
        FxUtil.switchScene(event, Views.VIEW_STUDENTS, Views.VIEW_STUDENTS_TITLE);
    }

    @FXML
    private void onSearchStudentProfiles(ActionEvent event) {
        FxUtil.switchScene(event, Views.SEARCH_STUDENTS, Views.SEARCH_STUDENTS_TITLE);
    }

    @FXML
    private void onViewReports(ActionEvent event) {
        FxUtil.switchScene(event, Views.REPORTS, Views.REPORTS_TITLE);
    }
}
