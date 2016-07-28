package guitests.guihandles;

import address.controller.MainController;
import address.model.datatypes.tag.Tag;
import guitests.GuiRobot;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a handle for the dialog used for tagging a person.
 */
public class TagPersonDialogHandle extends GuiHandle {

    private static final String TAG_LIST_FIELD_ID = "#tagList";
    private static final String TAG_SEARCH_FIELD_ID = "#tagSearch";

    public TagPersonDialogHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage, MainController.DIALOG_TITLE_TAG_SELECTION);
    }

    public List<Tag> getAssignedTagList() {
        FlowPane pane = (FlowPane) getNode(TAG_LIST_FIELD_ID);
        List<Label> tags = pane.getChildren().stream().map(o -> (Label) o).collect(Collectors.toCollection(ArrayList::new));
        return tags.stream().map(t -> new Tag(t.getText())).collect(Collectors.toCollection(ArrayList::new));
    }

    public TagPersonDialogHandle searchAndAcceptTag(String queryText) {
        return enterSearchQuery(queryText).acceptSuggestedTag();
    }

    public TagPersonDialogHandle searchAndAcceptTags(String... tagQueries) {
        TagPersonDialogHandle tagPersonDialogHandle = this;
        for (String tagQuery : tagQueries) {
            tagPersonDialogHandle = tagPersonDialogHandle.searchAndAcceptTag(tagQuery);
        }
        return tagPersonDialogHandle;
    }

    public TagPersonDialogHandle enterSearchQuery(String queryText) {
        typeTextField(TAG_SEARCH_FIELD_ID, queryText);
        return this;
    }

    public TagPersonDialogHandle acceptSuggestedTag() {
        guiRobot.type(KeyCode.SPACE);
        return this;
    }

    public void close() {
        super.pressEnter();
        guiRobot.sleep(200); // wait for closing animation

    }
}
