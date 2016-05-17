package address.controller;

import address.exceptions.DuplicateGroupException;
import address.model.ContactGroup;
import address.model.ModelManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;

public class GroupCardController {
    @FXML
    private VBox box;
    @FXML
    private Label groupName;

    private ContactGroup group;
    private MainController mainController;
    private ModelManager modelManager;

    public GroupCardController(ContactGroup group, MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;
        this.group = group;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GroupListCard.fxml"));
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize() {
        groupName.setText(group.getName());
        setListeners();
    }

    private ContextMenu getContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem newGroupItem = new MenuItem("New");
        newGroupItem.setOnAction(event -> {
                Optional<ContactGroup> newGroup = Optional.of(new ContactGroup());
                while (true) { // keep re-asking until user provides valid input or cancels operation.
                    newGroup = mainController.getGroupDataInput(newGroup.get());
                    if (!newGroup.isPresent()) break;
                    try {
                        modelManager.addGroup(newGroup.get());
                        break;
                    } catch (DuplicateGroupException e) {
                        mainController.showAlertDialogAndWait(AlertType.WARNING, "Warning",
                                                              "Cannot have duplicate groups", e.toString());
                    }
                }
            });
        MenuItem editGroup = new MenuItem("Edit");
        editGroup.setOnAction(event -> handleEditGroupAction());
        MenuItem removeGroup = new MenuItem("Remove");
        removeGroup.setOnAction(e -> modelManager.deleteGroup(group));


        contextMenu.getItems().addAll(newGroupItem, editGroup, removeGroup);

        return contextMenu;
    }

    public void setListeners() {
        box.setOnMouseClicked(mouseEv -> {
                switch (mouseEv.getButton()) {
                case PRIMARY :
                    if (mouseEv.getClickCount() >= 2) {
                        handleEditGroupAction();
                    }
                    break;
                case SECONDARY :
                    if (mouseEv.getClickCount() == 1) {
                        getContextMenu().show(groupName, Side.BOTTOM, 0, 0);
                    }
                    break;
                }
            });
    }

    private void handleEditGroupAction() {
        Optional<ContactGroup> updated = Optional.of(new ContactGroup(group));
        while (true) { // keep re-asking until user provides valid input or cancels operation.
            updated = mainController.getGroupDataInput(updated.get());

            if (!updated.isPresent()) break;

            try {
                modelManager.updateGroup(group, updated.get());
                break;
            } catch (DuplicateGroupException e) {
                mainController.showAlertDialogAndWait(AlertType.WARNING, "Warning", "Cannot have duplicate group",
                                                      e.toString());
            }
        }
    }

    public VBox getLayout() {
        return box;
    }
}
