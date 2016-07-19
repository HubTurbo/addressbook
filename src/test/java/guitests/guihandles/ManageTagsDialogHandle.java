package guitests.guihandles;

import guitests.GuiRobot;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Provides a handle to the dialog used for managing tags.
 */
public class ManageTagsDialogHandle extends GuiHandle {

    public static final String EDIT_TAG_TEXT_FIELD = "#tagNameField";

    public ManageTagsDialogHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage);
    }

    private ScrollPane getScrollPane() {
        return (ScrollPane) getNode("#tagListTags");
    }

    public List<String> getListOfTagNames() {
        ObservableList<Node> childrenUnmodifiable = ((VBox) getScrollPane().getContent()).getChildren();
        return childrenUnmodifiable.stream().map(n -> ((Label) n.lookup("#tagName")).getText())
                                            .collect(Collectors.toCollection(ArrayList::new));

    }

    public boolean contains(String value) {
        return getListOfTagNames().stream().filter(value::equals).findAny().isPresent();
    }

    public void openEditTagDialog(String tag) {
        guiRobot.doubleClickOn(tag);
    }

    public String getEditTagDialogText() {
        return getTextFieldText(EDIT_TAG_TEXT_FIELD);
    }

    public void changeEditTagDialogText(String text) {
        guiRobot.clickOn(EDIT_TAG_TEXT_FIELD).push(KeyCode.SHORTCUT, KeyCode.A).eraseText(1).write(text);
    }

    public boolean isChangeEditTagDialogOpen() {
        try{
            return guiRobot.window("Edit Tag") != null;
        } catch (NoSuchElementException e) {
            return false;
        }
    }


}
