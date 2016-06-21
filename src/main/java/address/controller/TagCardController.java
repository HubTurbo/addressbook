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
import java.util.Optional;

public class TagCardController {
    @FXML
    private VBox box;
    @FXML
    private Label tagName;

    private Tag tag;
    private MainController mainController;
    private ModelManager modelManager;
    private TagListController tagListController;

    private static boolean isAddSuccessful(MainController mainController, ModelManager modelManager, Tag newTag) {
        try {
            modelManager.addTag(newTag);
            return true;
        } catch (DuplicateTagException e) {
            mainController.showAlertDialogAndWait(AlertType.WARNING, "Warning", "Cannot have duplicate tag",
                    e.toString());
            return false;
        }
    }

    public static VBox getDummyTagCard(TagListController tagListController, MainController mainController, ModelManager  modelManager) {
        VBox vBox = new VBox();
        vBox.getChildren().add(new Label("Click to add new tag"));
        vBox.setPadding(new Insets(10, 10, 10, 10));
        ContextMenu contextMenu = new ContextMenu();


        vBox.setOnMouseClicked(mouseEv -> {
            switch (mouseEv.getButton()) {
                case PRIMARY :
                    if (mouseEv.getClickCount() == 1) {
                        Optional<Tag> newTag = Optional.of(new Tag());
                        do {
                            newTag = mainController.getTagDataInput(newTag.get());
                        } while (newTag.isPresent() && !isAddSuccessful(mainController, modelManager, newTag.get()));
                        tagListController.refreshList();
                    }
                    break;
            }
        });
        return vBox;
    }

    public TagCardController(Tag tag, MainController mainController, ModelManager modelManager, TagListController tagListController) {
        this.mainController = mainController;
        this.modelManager = modelManager;
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
        Optional<Tag> newTag = Optional.of(new Tag());
        do {
            newTag = mainController.getTagDataInput(newTag.get());
        } while (newTag.isPresent() && !isAddSuccessful(newTag.get()));
        tagListController.refreshList();
    }

    private void handleEditTagAction() {
        Optional<Tag> updatedTag = Optional.of(new Tag(tag));
        do {
            updatedTag = mainController.getTagDataInput(updatedTag.get());
        } while (updatedTag.isPresent() && !isUpdateSuccessful(tag, updatedTag.get()));
        tagListController.refreshList();
    }

    private void handleDeleteTagAction() {
        modelManager.deleteTag(tag);
        tagListController.refreshList();
    }

    /**
     * Attempts to update the model with the given new tag, and returns the result
     *
     * @param oldTag
     * @param newTag
     * @return true if successful
     */
    private boolean isUpdateSuccessful(Tag oldTag, Tag newTag) {
        try {
            modelManager.updateTag(oldTag, newTag);
            return true;
        } catch (DuplicateTagException e) {
            mainController.showAlertDialogAndWait(AlertType.WARNING, "Warning", "Cannot have duplicate tag",
                    e.toString());
            return false;
        }
    }

    /**
     * Attempts to add the given new tag to the model, and returns the result
     *
     * @param newTag
     * @return true if successful
     */
    private boolean isAddSuccessful(Tag newTag) {
        try {
            modelManager.addTag(newTag);
            return true;
        } catch (DuplicateTagException e) {
            mainController.showAlertDialogAndWait(AlertType.WARNING, "Warning", "Cannot have duplicate tag",
                    e.toString());
            return false;
        }
    }

    public VBox getLayout() {
        return box;
    }
}
