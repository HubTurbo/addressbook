package address.controller;

import address.model.ContactGroup;
import address.model.ModelManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class GroupCardController {
    @FXML
    private VBox box;
    @FXML
    private Label groupName;

    MainController mainController;
    ModelManager modelManager;
    ContactGroup group;

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
        setListener();
    }

    private ContextMenu getContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem newGroup = new MenuItem("New");
        newGroup.setOnAction(e -> {
                ContactGroup tempGroup = new ContactGroup();
                boolean okClicked = mainController.showGroupEditDialog(tempGroup);
                if (okClicked) {
                    modelManager.addGroup(tempGroup);
                }
            });
        MenuItem editGroup = new MenuItem("Edit");
        editGroup.setOnAction(e -> mainController.showGroupEditDialog(group));
        MenuItem removeGroup = new MenuItem("Remove");
        removeGroup.setOnAction(e -> modelManager.deleteGroup(group));


        contextMenu.getItems().addAll(newGroup, editGroup, removeGroup);

        return contextMenu;
    }

    public void setListener() {
        box.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                    mainController.showGroupEditDialog(group);
                }
                if (mouseEvent.getButton().equals(MouseButton.SECONDARY) && mouseEvent.getClickCount() == 1) {
                    getContextMenu().show(groupName, Side.BOTTOM, 0, 0);
                }
            });
    }

    public VBox getLayout() {
        return box;
    }
}
