package address.controller;

import address.events.EventManager;
import address.events.TagSearchResultsChangedEvent;
import address.events.TagsChangedEvent;
import address.model.TagSelectionEditDialogModel;
import address.model.datatypes.tag.SelectableTag;
import address.model.datatypes.tag.Tag;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class TagSelectionEditDialogController extends EditDialogController {
    @FXML
    ScrollPane tagList;

    @FXML
    ScrollPane tagResults;

    @FXML
    TextField tagSearch;

    private TagSelectionEditDialogModel model;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        addListeners();
        EventManager.getInstance().registerHandler(this);
        Platform.runLater(() -> tagSearch.requestFocus());
    }

    public void setTags(List<Tag> tags, List<Tag> assignedTags) {
        this.model = new TagSelectionEditDialogModel(tags, assignedTags);
    }

    @Subscribe
    public void handleTagSearchResultsChangedEvent(TagSearchResultsChangedEvent e) {
        tagResults.setContent(getTagsVBox(e.getSelectableTags(), true));
    }

    @Subscribe
    public void handleTagsChangedEvent(TagsChangedEvent e) {
        tagList.setContent(getTagsVBox(e.getResultTag(), false));
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private VBox getTagsVBox(List<SelectableTag> contactTagList, boolean isSelectable) {
        VBox content = new VBox();
        contactTagList.stream()
                .forEach(contactTag -> {
                    Label newLabel = new Label(contactTag.getName());
                    if (isSelectable && contactTag.isSelected()) {
                        newLabel.setStyle("-fx-background-color: blue;");
                    }
                    newLabel.setPrefWidth(261);
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

    @FXML
    protected void handleOk() {
        isOkClicked = true;
        dialogStage.close();
    }


    @FXML
    protected void handleCancel() {
        dialogStage.close();
    }
}
