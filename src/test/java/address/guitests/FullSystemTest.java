package address.guitests;

import guitests.GuiTestBase;
import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

public class FullSystemTest extends GuiTestBase {
    @Test
    public void scenarioOne() {
        // Attempt to create new tag, then cancel
        clickOn("Tags").clickOn("New Tag");
        verifyThat("#tagNameField", hasText(""));
        push(KeyCode.ESCAPE);

        // Attempt to create new tag
        clickOn("Tags").clickOn("New Tag");
        verifyThat("#tagNameField", hasText(""));

        // Set name of tag to be "colleagues"
        clickOn("#tagNameField").write("colleagues");
        verifyThat("#tagNameField", hasText("colleagues"));
        type(KeyCode.ENTER);

        // Edit Hans Muster to John Tan, and edit details
        clickOn("Muster").type(KeyCode.E)
                .clickOn("#firstNameField").push(KeyCode.SHORTCUT, KeyCode.A).eraseText(1).write("John")
                .clickOn("#lastNameField").eraseText(6).write("Tan")
                .clickOn("#cityField").write("Singapore")
                .clickOn("#githubUserNameField").write("john123")
                .clickOn("#tagList")
                .sleep(200) // wait for opening animation
                    .clickOn("#tagSearch")
                    .write("coll").type(KeyCode.SPACE)
                    .type(KeyCode.ENTER)
                .sleep(200); // wait for closing animation
        verifyThat("#firstNameField", hasText("John"));
        verifyThat("#lastNameField", hasText("Tan"));
        verifyThat("#cityField", hasText("Singapore"));
        verifyThat("#githubUserNameField", hasText("john123"));
        type(KeyCode.ENTER);

        // filter persons list with "colleagues" tag
        clickOn("#filterField").write("tag:colleagues").type(KeyCode.ENTER);
        verifyThat("#filterField", hasText("tag:colleagues"));

        /* The cursor can't move to the filter in 3 secs, which at the time, john is changed back to Hans
        // verify John is in the list, and try to delete
        clickOn("John").type(KeyCode.D);
        */
        // remove filter again
        clickOn("#filterField").push(KeyCode.SHORTCUT, KeyCode.A).eraseText(1).type(KeyCode.ENTER);

        // edit Ruth Mueller's github username
        clickOn("Ruth").type(KeyCode.E).clickOn("#streetField").write("My Street")
                .clickOn("#githubUserNameField").write("ruth321")
                .type(KeyCode.ENTER);

        // filter based on "Mueller" name
        clickOn("#filterField").write("name:Mueller").type(KeyCode.ENTER);

        // edit Martin Mueller's tags
        clickOn("Martin").type(KeyCode.E)
                .clickOn("#tagList")
                .sleep(200)// wait for opening animation
                    .write("frien").type(KeyCode.SPACE)
                    .type(KeyCode.ENTER)
                .sleep(200)// wait for closing animation
                .type(KeyCode.ENTER);

        // ensure "About" dialog opens
        clickOn("Help").clickOn("About").clickOn("OK");

        // create a new person Ming Lee, check that last name cannot be blank
        clickOn("New")
                .clickOn("#firstNameField").write("Ming").clickOn("OK")
                .targetWindow("Invalid Fields").clickOn("OK")
                .clickOn("#lastNameField").write("Lee").clickOn("OK");

        // save file
        clickOn("File").clickOn("[Local] Save");

        // add new tag "company"
        clickOn("Tags").clickOn("Manage Tags")
                .rightClickOn("colleagues").clickOn("New")
                .clickOn("#tagNameField").write("company");
        verifyThat("#tagNameField", hasText("company"));
        push(KeyCode.ENTER);

        // verify that company is in the tag list
        rightClickOn("company").clickOn("Edit");
        verifyThat("#tagNameField", hasText("company"));

        // UNABLE to launch file chooser in mac's headless mode
        // UNABLE to close tag list dialog in headless mode
    }
}
