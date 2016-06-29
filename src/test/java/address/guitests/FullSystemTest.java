package address.guitests;

import javafx.scene.input.KeyCode;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

public class FullSystemTest extends GuiTestBase {
    @Before
    public void cleanup() {
        File file = new File("src/test/data/sandbox/s.xml");
        file.delete();
    }

    @Test
    public void scenarioOne() {
        clickOn("Tags").clickOn("New Tag");
        verifyThat("#tagNameField", hasText(""));

        clickOn("#tagNameField").write("colleagues");
        verifyThat("#tagNameField", hasText("colleagues"));

        type(KeyCode.ENTER)
                .clickOn("Muster").type(KeyCode.E).sleep(1000).clickOn("#firstNameField").push(shortcut(KeyCode.A)).eraseText(4).write("John")
                .clickOn("#lastNameField").eraseText(6).write("Tan")
                .clickOn("#cityField").write("Singapore")
                .clickOn("#githubUserNameField").write("john123")
                .clickOn("#tagList").write("coll").type(KeyCode.SPACE)
                .type(KeyCode.ENTER).sleep(2000)//wait for closing animation
                .type(KeyCode.ENTER)
        //.clickOn("#filterField").write("tag:colleagues").type(KeyCode.ENTER)
//                .sleep(1000)
        .clickOn("John")
                .type(KeyCode.D)
                .sleep(1000)
                .clickOn("Tags").clickOn("Manage Tags").rightClickOn("colleagues").clickOn("New").write("company")
                .type(KeyCode.ENTER).type(KeyCode.ESCAPE)
        .clickOn("#filterField").push(shortcut(KeyCode.A)).eraseText(1).type(KeyCode.ENTER)
        .clickOn("Ruth").type(KeyCode.E).clickOn("#streetField").write("My Street")
                .clickOn("#githubUserNameField").write("ruth321")
                .type(KeyCode.ENTER)
        .clickOn("#filterField").write("name:Mueller").type(KeyCode.ENTER)
                .clickOn("Martin").type(KeyCode.E).clickOn("#tagList").write("frien").type(KeyCode.SPACE)
                .type(KeyCode.ENTER).sleep(200)
                .type(KeyCode.ENTER)
        .clickOn("Help").clickOn("About").clickOn("OK")

        .clickOn("New").clickOn("#firstNameField").write("Ming").clickOn("OK").targetWindow("Invalid Fields").clickOn("OK")
                .clickOn("#lastNameField").write("Lee").clickOn("OK")
        .clickOn("File").clickOn("[Local] Save As...").sleep(1000).type(KeyCode.S).type(KeyCode.ENTER)
        .clickOn("File").clickOn("[Local] Save").clickOn("File").clickOn("Exit");
    }
}
