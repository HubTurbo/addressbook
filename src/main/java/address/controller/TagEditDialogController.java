package address.controller;

import address.events.EventManager;
import address.model.datatypes.tag.Tag;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class TagEditDialogController extends EditDialogController {
    @FXML
    private TextField tagNameField;

    private Tag editedTag;

    public TagEditDialogController() {
        EventManager.getInstance().registerHandler(this);
    }

    public void setInitialTagData(Tag tag) {
        tagNameField.setText(tag.getName());
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

    public Tag getFinalInput() {
        return editedTag;
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
