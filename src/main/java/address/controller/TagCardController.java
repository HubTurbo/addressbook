package address.controller;

import address.model.datatypes.tag.Tag;
import address.exceptions.DuplicateTagException;
import address.model.ModelManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class TagCardController {
    @FXML
    private VBox box;
    @FXML
    private Label tagName;

    private Tag tag;
    private MainController mainController;
    private TagListController tagListController;

    public static VBox getDummyTagCard(TagListController tagListController, MainController mainController) {
        VBox vBox = new VBox();
        vBox.getChildren().add(new Label("Click to add new tag"));
        vBox.setPadding(new Insets(10, 10, 10, 10));

        vBox.setOnMouseClicked(mouseEv -> {
            switch (mouseEv.getButton()) {
                case PRIMARY :
                    if (mouseEv.getClickCount() == 1) {
                        mainController.addTagData();
                        tagListController.refreshList();
                    }
                    break;
            }
        });
        return vBox;
    }

    public TagCardController(Tag tag, MainController mainController, TagListController tagListController) {
        this.mainController = mainController;
        this.tag = tag;
        this.tagListController = tagListController;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TagListCard.fxml"));
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize() {
        tagName.setText(tag.getName());
        setListeners();
    }

    private ContextMenu getContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem newTagItem = new MenuItem("New");
        newTagItem.setOnAction(event -> handleAddTagAction());
        MenuItem editTag = new MenuItem("Edit");
        editTag.setOnAction(event -> handleEditTagAction());
        MenuItem removeTag = new MenuItem("Remove");
        removeTag.setOnAction(event -> handleDeleteTagAction());

        contextMenu.getItems().addAll(newTagItem, editTag, removeTag);

        return contextMenu;
    }

    public void setListeners() {
        box.setOnMouseClicked(mouseEv -> {
            switch (mouseEv.getButton()) {
            case PRIMARY :
                if (mouseEv.getClickCount() >= 2) {
                    handleEditTagAction();
                }
                break;
            case SECONDARY :
                if (mouseEv.getClickCount() == 1) {
                    getContextMenu().show(tagName, Side.BOTTOM, 0, 0);
                }
                break;
            }
        });
    }

    private void handleAddTagAction() {
        mainController.addTagData();
        tagListController.refreshList();
    }

    private void handleEditTagAction() {
        mainController.editTagData(tag);
        tagListController.refreshList();
    }

    private void handleDeleteTagAction() {
        mainController.deleteTagData(tag);
        tagListController.refreshList();
    }

    public VBox getLayout() {
        return box;
    }
}
