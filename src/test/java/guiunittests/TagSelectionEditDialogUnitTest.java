package guiunittests;

import address.TestApp;
import address.controller.TagSelectionEditDialogController;
import address.model.datatypes.tag.Tag;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.GuiTest.find;

//TODO: Create or use a handle for this test class
public class TagSelectionEditDialogUnitTest extends GuiUnitTestBase {
    private FlowPane tagList;
    private TextField tagSearch;
    private ScrollPane tagResults;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(TestApp.class.getResource("/view/TagSelectionEditDialog.fxml"));
            AnchorPane baseNode = loader.load();

            TagSelectionEditDialogController controller = loader.getController();

            Tag tag1 = new Tag("enemies");
            Tag tag2 = new Tag("friends");
            Tag tag3 = new Tag("relatives");

            List<Tag> assignedTags = new ArrayList<>();
            assignedTags.add(tag1);

            List<Tag> allTags = new ArrayList<>();
            allTags.add(tag1);
            allTags.add(tag2);
            allTags.add(tag3);

            controller.setTags(allTags, assignedTags);

            primaryStage.setScene(new Scene(baseNode));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() {
        tagList = find("#tagList");
        tagSearch = find("#tagSearch");
        tagResults = find("#tagResults");
    }

    @Test
    public void testTagSearch() {
        guiRobot.clickOn(tagSearch).write("frien");

        assertEquals(1, ((VBox) tagResults.getContent()).getChildren().size());
        assertEquals(1, tagList.getChildren().size());
    }

    @Test
    public void testTagSearch2() {
        guiRobot.clickOn(tagSearch).write("rela");

        assertEquals(1, ((VBox) tagResults.getContent()).getChildren().size());
        assertEquals(1, tagList.getChildren().size());
    }

    @Test
    public void testTagSearch3() {
        guiRobot.clickOn(tagSearch).write("e");

        assertEquals(3, ((VBox) tagResults.getContent()).getChildren().size());
        assertEquals(1, tagList.getChildren().size());
    }

    @Test
    public void testTagSearch4() {
        guiRobot.clickOn(tagSearch).write("frie frie");

        assertEquals(1, ((VBox) tagResults.getContent()).getChildren().size());
        assertEquals(2, tagList.getChildren().size());
    }
}
