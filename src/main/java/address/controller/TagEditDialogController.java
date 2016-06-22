package address.controller;

import address.model.datatypes.tag.Tag;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class TagEditDialogController extends EditDialogController {
    @FXML
    private TextField tagNameField;

    private Tag editedTag;

    @FXML
    public void initialize() {
        Platform.runLater(() -> tagNameField.requestFocus());
    }

    public void setInitialTagData(Tag tag) {
        tagNameField.setText(tag.getName());
    }

    @Override
    public void setDialogStage(Stage dialogStage) {
        dialogStage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                e.consume();
                handleCancel();
            }
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
                handleOk();
            }
        });
        this.dialogStage = dialogStage;
    }

    public Tag getEditedTag() {
        return editedTag;
    }

    private void showErrorAlert(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialogStage);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private boolean isInputValid() {
        if (isEmptyName()) {
            showErrorAlert("Invalid name", "Name cannot be empty");
            return false;
        }
        return true;
    }

    private boolean isEmptyName() {
        return tagNameField.getText().isEmpty();
    }

    @FXML
    protected void handleOk() {
        if (!isInputValid()) return;
        editedTag = new Tag();
        editedTag.setName(tagNameField.getText());

        isOkClicked = true;
        dialogStage.close();
    }

    @FXML
    protected void handleCancel() {
        dialogStage.close();
    }
}
