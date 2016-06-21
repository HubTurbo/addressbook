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
    private static final String TRANSITION_END = "end";
    private static final int TAG_LABEL_WIDTH = 235;

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
        transition = getPaneTransition(mainPane);
        transition.play();

        addListeners();
        EventManager.getInstance().registerHandler(this);
        Platform.runLater(() -> tagSearch.requestFocus());
    }

    private ScaleTransition getPaneTransition(AnchorPane pane) {
        ScaleTransition transition = new ScaleTransition(Duration.millis(200), pane);
        transition.setAutoReverse(true);
        transition.setFromX(0);
        transition.setFromY(0);
        transition.setFromZ(0);
        transition.setToX(1);
        transition.setToY(1);
        transition.setToZ(1);
        return transition;
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
        tagList.getChildren().addAll(getTagListNodes(e.getResultTag(), true));
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

    /**
     * Returns the list of nodes that represent the given list of tags
     *
     * @param contactTagList
     * @param shouldIgnoreSelectedProperty if false, then the label may have a blue background
     *                                     depending on its selected status
     * @return
     */
    private List<Node> getTagListNodes(List<SelectableTag> contactTagList, boolean shouldIgnoreSelectedProperty) {
        List<Node> tagList = new ArrayList<>();
        contactTagList.stream()
                .forEach(contactTag -> {
                    Label newLabel = new Label(contactTag.getName());
                    if (!shouldIgnoreSelectedProperty && contactTag.isSelected()) {
                        newLabel.setStyle("-fx-background-color: blue;");
                    }
                    newLabel.setPrefWidth(TAG_LABEL_WIDTH);
                    tagList.add(newLabel);
                });

        return tagList;
    }

    /**
     * Returns a VBox containing the list of tags' labels
     *
     * Each label might be blue (selected) depending on the respective tag's isSelected() property
     *
     * @param contactTagList
     * @return
     */
    private VBox getTagsVBox(List<SelectableTag> contactTagList) {
        List<Node> tagNodes = getTagListNodes(contactTagList, false);
        VBox content = new VBox();
        tagNodes.stream()
                .forEach(tagNode -> content.getChildren().add(tagNode));
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

    /**
     * Handles when the user indicates confirmation
     *
     * Assumes that transition is not null
     */
    protected void handleOk() {
        isOkClicked = true;
        playReversedTransition();
    }

    /**
     * Handles when the user indicates cancellation
     *
     * Assumes that transition is not null
     */
    protected void handleCancel() {
        playReversedTransition();
    }

    private void playReversedTransition() {
        transition.setOnFinished((e) -> dialogStage.close());
        transition.setRate(-1);
        transition.playFrom(TRANSITION_END);
    }
}
