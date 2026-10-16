package studentkb.controller;

import studentkb.util.FxUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;

public class CommentViewController {
    @FXML private TextArea fullCommentArea;

    public void setComment(String content) {
        fullCommentArea.setText(content == null ? "" : content);
    }

    @FXML
    private void onClose(ActionEvent e) {
        ((Node) e.getSource()).getScene().getWindow().hide();
    }
}
