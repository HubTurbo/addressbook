package address.controller;

import address.events.EventManager;
import address.events.TagSearchResultsChangedEvent;
import address.events.TagsChangedEvent;
import address.model.TagSelectionEditDialogModel;
import address.model.datatypes.tag.SelectableTag;
import address.model.datatypes.tag.Tag;
import com.google.common.eventbus.Subscribe;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class TagSelectionEditDialogController extends EditDialogController {
    @FXML
    AnchorPane mainPane;

    @FXML
    FlowPane tagList;

    @FXML
    ScrollPane tagResults;

    @FXML
    TextField tagSearch;

    private TagSelectionEditDialogModel model;
    private Stage dialogStage;
    private ScaleTransition transition;

    @FXML
    public void initialize() {
        transition = new ScaleTransition(Duration.millis(200), mainPane);
        transition.setAutoReverse(true);
        transition.setFromX(0);
        transition.setFromY(0);
        transition.setFromZ(0);
        transition.setToX(1);
        transition.setToY(1);
        transition.setToZ(1);
        transition.play();

        addListeners();
        EventManager.getInstance().registerHandler(this);
        Platform.runLater(() -> tagSearch.requestFocus());
    }

    public void setTags(List<Tag> tags, List<Tag> assignedTags) {
        this.model = new TagSelectionEditDialogModel(tags, assignedTags);
    }

    @Subscribe
    public void handleTagSearchResultsChangedEvent(TagSearchResultsChangedEvent e) {
        tagResults.setContent(getTagsVBox(e.getSelectableTags()));
    }

    @Subscribe
    public void handleTagsChangedEvent(TagsChangedEvent e) {
        tagList.getChildren().clear();
        tagList.getChildren().addAll(getTagListNodes(e.getResultTag()));
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        dialogStage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) handleCancel();
            if (e.getCode() == KeyCode.ENTER) handleOk();
        });
        this.dialogStage = dialogStage;
    }

    private List<Node> getTagListNodes(List<SelectableTag> contactTagList) {
        List<Node> tagList = new ArrayList<>();
        contactTagList.stream()
                .forEach(contactTag -> {
                    Label newLabel = new Label(contactTag.getName());
                    newLabel.setPrefWidth(235);
                    tagList.add(newLabel);
                });

        return tagList;
    }

    private VBox getTagsVBox(List<SelectableTag> contactTagList) {
        VBox content = new VBox();
        contactTagList.stream()
                .forEach(contactTag -> {
                    Label newLabel = new Label(contactTag.getName());
                    if (contactTag.isSelected()) {
                        newLabel.setStyle("-fx-background-color: blue;");
                    }
                    newLabel.setPrefWidth(235);
                    content.getChildren().add(newLabel);
                });

        return content;
    }

    private void addListeners() {
        tagSearch.textProperty().addListener((observableValue, oldValue, newValue) -> {
            handleTagInput(newValue);
        });
        tagSearch.setOnKeyTyped(e -> {
            if (!e.getCharacter().equals(" ")) return;

            e.consume();
            model.toggleSelection();
            tagSearch.clear();
        });
        tagSearch.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case DOWN:
                    e.consume();
                    model.selectNext();
                    break;
                case UP:
                    e.consume();
                    model.selectPrevious();
                    break;
                default:
                    break;
            }
        });
    }

    public List<Tag> getFinalAssignedTags() {
        return model.getAssignedTags();
    }


    @FXML
    protected void handleTagInput(String newTags) {
        model.setFilter(newTags);
    }

    protected void handleOk() {
        isOkClicked = true;
        if (transition != null) {
            transition.setOnFinished((e) -> dialogStage.close());
            transition.setRate(-1);
            transition.playFrom("end");
        } else {
            dialogStage.close();
        }
    }

    protected void handleCancel() {
        if (transition != null) {
            transition.setOnFinished((e) -> dialogStage.close());
            transition.setRate(-1);
            transition.playFrom("end");
        } else {
            dialogStage.close();
        }
    }
}
