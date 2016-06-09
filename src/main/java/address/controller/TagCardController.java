package address.controller;

import address.model.datatypes.tag.Tag;
import address.exceptions.DuplicateTagException;
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

public class TagCardController {
    @FXML
    private VBox box;
    @FXML
    private Label tagName;

    private Tag tag;
    private MainController mainController;
    private ModelManager modelManager;

    public TagCardController(Tag tag, MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;
        this.tag = tag;

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
        newTagItem.setOnAction(event -> {
                Optional<Tag> newTag = Optional.of(new Tag());
                while (true) { // keep re-asking until user provides valid input or cancels operation.
                    newTag = mainController.getTagDataInput(newTag.get());
                    if (!newTag.isPresent()) break;
                    try {
                        modelManager.addTag(newTag.get());
                        break;
                    } catch (DuplicateTagException e) {
                        mainController.showAlertDialogAndWait(AlertType.WARNING, "Warning",
                                                              "Cannot have duplicate tags", e.toString());
                    }
                }
            });
        MenuItem editTag = new MenuItem("Edit");
        editTag.setOnAction(event -> handleEditTagAction());
        MenuItem removeTag = new MenuItem("Remove");
        removeTag.setOnAction(e -> modelManager.deleteTag(tag));


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

    private void handleEditTagAction() {
        Optional<Tag> updated = Optional.of(new Tag(tag));
        while (true) { // keep re-asking until user provides valid input or cancels operation.
            updated = mainController.getTagDataInput(updated.get());

            if (!updated.isPresent()) break;

            try {
                modelManager.updateTag(tag, updated.get());
                break;
            } catch (DuplicateTagException e) {
                mainController.showAlertDialogAndWait(AlertType.WARNING, "Warning", "Cannot have duplicate tag",
                                                      e.toString());
            }
        }
    }

    public VBox getLayout() {
        return box;
    }
}
