package address.controller;

import address.events.EventManager;
import address.model.datatypes.ContactGroup;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class GroupEditDialogController extends EditDialogController {
    @FXML
    private TextField groupNameField;

    private ContactGroup finalGroup;

    public GroupEditDialogController() {
        EventManager.getInstance().registerHandler(this);
    }

    public void setInitialGroupData(ContactGroup group) {
        groupNameField.setText(group.getName());
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
        return groupNameField.getText().isEmpty();
    }

    public ContactGroup getFinalInput() {
        return finalGroup;
    }

    @FXML
    protected void handleOk() {
        if (!isInputValid()) return;
        finalGroup = new ContactGroup();
        finalGroup.setName(groupNameField.getText());

        isOkClicked = true;
        dialogStage.close();
    }

    @FXML
    protected void handleCancel() {
        dialogStage.close();
    }
}
