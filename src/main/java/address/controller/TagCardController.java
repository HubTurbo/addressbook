package address.controller;

import address.model.datatypes.tag.Tag;

import address.ui.Ui;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class TagCardController extends UiController{
    @FXML
    private VBox box;
    @FXML
    private Label tagName;

    private Tag tag;
    private Ui ui;
    private TagListController tagListController;

    public TagCardController(Tag tag, Ui ui, TagListController tagListController) {
        this.ui = ui;
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

    public static VBox getDummyTagCard(TagListController tagListController, Ui ui) {
        VBox vBox = new VBox();
        Label label = new Label("Click to add new tag");
        label.setPrefWidth(280);
        vBox.getChildren().add(label);
        vBox.setPadding(new Insets(10, 10, 10, 10));

        vBox.setOnMouseClicked(mouseEv -> {
            switch (mouseEv.getButton()) {
                case PRIMARY:
                    if (mouseEv.getClickCount() == 1) {
                        ui.addTagData();
                        tagListController.refreshList();
                    }
                    break;
            }
        });
        return vBox;
    }

    @FXML
    public void initialize() {
        tagName.setText(tag.getName());
        setListeners();
    }

    public void setListeners() {
        box.setOnMouseClicked(mouseEv -> {
            switch (mouseEv.getButton()) {
                case PRIMARY:
                    if (mouseEv.getClickCount() >= 2) {
                        handleEditTagAction();
                    }
                    break;
                case SECONDARY:
                    if (mouseEv.getClickCount() == 1) {
                        getContextMenu().show(tagName, Side.BOTTOM, 0, 0);
                    }
                    break;
                default:
                    break;
            }
        });
    }

    public VBox getLayout() {
        return box;
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

    private void handleAddTagAction() {
        ui.addTagData();
        tagListController.refreshList();
    }

    private void handleEditTagAction() {
        ui.editTagData(tag);
        tagListController.refreshList();
    }

    private void handleDeleteTagAction() {
        ui.deleteTagData(tag);
        tagListController.refreshList();
    }
}
