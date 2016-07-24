package address.controller;

import address.model.datatypes.tag.Tag;
import address.model.ModelManager;
import address.ui.Ui;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Dialog to show the list of available tags
 *
 * Stage, tags, ui and modelManager should be set before showing stage
 */
public class TagListController extends UiController{
    Stage stage;
    Ui ui;
    ModelManager modelManager;

    @FXML
    private AnchorPane mainPane;
    @FXML
    private ScrollPane tags;

    public void setStage(Stage stage) {
        stage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                e.consume();
                stage.close();
            }
        });
        this.stage = stage;
    }

    public void setTags(ObservableList<Tag> tagList) {
        tags.setContent(getTagsVBox(tagList, ui));
    }

    public void setUi(Ui ui) {
        this.ui = ui;
    }

    public void setModelManager(ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    public VBox getTagsVBox(ObservableList<Tag> tagList, Ui ui) {
        VBox vBox = new VBox();

        if (tagList.size() == 0) {
            vBox.getChildren().add(TagCardController.getDummyTagCard(this, ui));
            return vBox;
        }
        tagList.stream()
                .forEach(tag -> {
                    TagCardController tagCardController = new TagCardController(tag, ui, this);
                    vBox.getChildren().add(tagCardController.getLayout());
                });
        return vBox;
    }

    public void refreshList() {
        setTags(modelManager.getTagsAsReadOnlyObservableList());
        stage.sizeToScene();
    }
}
