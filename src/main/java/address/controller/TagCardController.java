package address.controller;

import address.model.datatypes.tag.Tag;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * A controller for TagList's card
 */
public class TagCardController extends UiController{

    private static final String VIEW_TAG_LIST_CARD_FXML = "/view/TagListCard.fxml";

    @FXML
    private VBox box;
    @FXML
    private Label tagName;

    private Tag tag;
    private MainController mainController;
    private TagListController tagListController;

    public TagCardController(Tag tag, MainController mainController, TagListController tagListController) {
        this.mainController = mainController;
        this.tag = tag;
        this.tagListController = tagListController;

        FXMLLoader loader = new FXMLLoader(getClass().getResource(VIEW_TAG_LIST_CARD_FXML));
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static VBox getDummyTagCard(TagListController tagListController, MainController mainController) {
        VBox vBox = new VBox();
        Label label = new Label("Click to add new tag");
        label.setPrefWidth(280);
        vBox.getChildren().add(label);
        vBox.setPadding(new Insets(10, 10, 10, 10));

        return vBox;
    }

    @FXML
    public void initialize() {
        tagName.setText(tag.getName());
    }

    public VBox getLayout() {
        return box;
    }

}
