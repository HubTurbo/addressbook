package address.controller;

import address.model.TagSelectionEditDialogModel;
import address.model.datatypes.tag.SelectableTag;
import address.model.datatypes.tag.Tag;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
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
import java.util.stream.Collectors;

/**
 * Dialog to select a subset of tags from an available list of tags
 *
 * Stage, initially selected and full list of tags should be set before showing stage
 */
public class TagSelectionEditDialogController extends EditDialogController {
    private static final String TRANSITION_END = "end";
    private static final int TAG_LABEL_WIDTH = 235;
    private static final String STYLE_SELECTED_BACKGROUND = "-fx-background-color: blue;";

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
        this.model = new TagSelectionEditDialogModel();

        addListeners();
        Platform.runLater(() -> tagSearch.requestFocus());
    }

    public void setTags(List<Tag> tags, List<Tag> assignedTags) {
        model.init(tags, assignedTags, "");
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        dialogStage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                e.consume();
                handleCancel();
            }
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
                handleOk();
            }
        });
        this.dialogStage = dialogStage;
    }

    public List<Tag> getFinalAssignedTags() {
        return model.getAssignedTags();
    }

    private ScaleTransition getPaneTransition(AnchorPane pane) {
        ScaleTransition transition = new ScaleTransition(Duration.millis(200), pane);
        transition.setFromX(0);
        transition.setFromY(0);
        transition.setFromZ(0);
        transition.setToX(1);
        transition.setToY(1);
        transition.setToZ(1);
        return transition;
    }

    /**
     * Returns the list of nodes that represent the given list of tags
     *
     * @param contactTagList
     * @return
     */
    private List<Node> getSelectableTagListNodes(List<? extends SelectableTag> contactTagList) {
        return contactTagList.stream()
                .map(tag -> getNodeForSelectableTag(tag))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns the list of nodes that represent the given list of tags
     *
     * @param contactTagList
     * @return
     */
    private List<Node> getTagListNodes(List<? extends Tag> contactTagList) {
        return contactTagList.stream()
                .map(this::getNodeForTag)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Label getNodeForSelectableTag(SelectableTag contactTag) {
        Label newLabel = new Label(contactTag.getName());
        if (contactTag.isSelected()) {
            newLabel.setStyle(STYLE_SELECTED_BACKGROUND);
        }
        newLabel.setPrefWidth(TAG_LABEL_WIDTH);
        return newLabel;
    }
    private Label getNodeForTag(Tag contactTag) {
        Label newLabel = new Label(contactTag.getName());
        newLabel.setPrefWidth(TAG_LABEL_WIDTH);
        return newLabel;
    }

    /**
     * Returns a VBox containing the list of tags' labels
     *
     * Each of the labels might be blue (selected) depending on the respective tag's isSelected() property
     *
     * @param contactTagList
     * @return
     */
    private VBox getTagsVBox(List<? extends SelectableTag> contactTagList) {
        List<Node> tagNodes = getSelectableTagListNodes(contactTagList);
        VBox content = new VBox();
        content.getChildren().addAll(tagNodes);
        return content;
    }

    private void addListeners() {
        tagSearch.textProperty().addListener((observableValue, oldValue, newValue) -> handleTagInput(newValue));
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

        model.getAssignedTags().addListener((ListChangeListener<Tag>) change -> {
            while (change.next()) tagList.getChildren().setAll(getTagListNodes(change.getList()));
        });
        model.getFilteredTags().addListener((ListChangeListener<SelectableTag>) change -> {
            while (change.next()) tagResults.setContent(getTagsVBox(change.getList()));
        });
    }

    private void playReversedTransition() {
        transition.setOnFinished((e) -> dialogStage.close());
        transition.setRate(-1);
        transition.playFrom(TRANSITION_END);
    }

    /**
     * Handles when the text in the tag search field has changed
     * @param newTags
     */
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
}
