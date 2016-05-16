package address.controller;

import address.events.EventManager;
import address.model.ContactGroup;
import address.model.ModelManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.util.List;

public class GroupEditDialogController extends EditDialogController {
    @FXML
    private TextField groupNameField;

    private ContactGroup group;
    private ModelManager modelManager;
    private List<ContactGroup> groups;

    public void setGroup(ContactGroup group) {
        this.group = group;
        groupNameField.setText(group.getName());
    }

    public void setGroups(List<ContactGroup> groups) {
        this.groups = groups;
    }

    public void setModelManager(ModelManager modelManager) {
        this.modelManager = modelManager;
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

        if (isExistingName()) {
            showErrorAlert("Invalid name", "Group name already exists!");
            return false;
        }
        return true;
    }

    private boolean isExistingName() {
        return groups.contains(new ContactGroup(groupNameField.getText()));
    }

    private boolean isEmptyName() {
        return groupNameField.getText().isEmpty();
    }

    @FXML
    protected void handleOk() {
        if (!isInputValid()) return;
        ContactGroup updatedGroup = new ContactGroup();
        updatedGroup.setName(groupNameField.getText());
        modelManager.updateGroup(group, updatedGroup);
        isOkClicked = true;
        dialogStage.close();
    }

    @FXML
    protected void handleCancel() {
        dialogStage.close();
    }
}
