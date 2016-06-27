package address.guitests;

import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Test;

import java.io.File;

public class FullSystemTest extends GuiTestBase {
    @Test
    public void scenarioOne() {
        clickOn("Tags").clickOn("New Tag")
                .clickOn("#tagNameField").write("colleagues").type(KeyCode.ENTER)
                .clickOn("Muster").type(KeyCode.E).clickOn("#firstNameField").eraseText(4).write("John")
                .clickOn("#lastNameField").eraseText(6).write("Tan")
                .clickOn("#cityField").write("Singapore")
                .clickOn("#githubUserNameField").write("john123")
                .clickOn("#tagList").write("coll").type(KeyCode.SPACE)
                .type(KeyCode.ENTER).sleep(200)//wait for closing animation
                .type(KeyCode.ENTER)
        .clickOn("#filterField").write("tag:colleagues").type(KeyCode.ENTER)
        .clickOn("John").type(KeyCode.D).sleep(1000)
                .clickOn("Tags").clickOn("Manage Tags").rightClickOn("colleagues").clickOn("New").write("company")
                .type(KeyCode.ENTER).type(KeyCode.ESCAPE)
        .clickOn("#filterField").push(shortcut(KeyCode.A)).eraseText(1).type(KeyCode.ENTER)
        .clickOn("Cornelia").type(KeyCode.E).clickOn("#streetField").write("My Street")
                .clickOn("#githubUserNameField").write("cornelia321")
                .type(KeyCode.ENTER)
        .clickOn("#filterField").write("name:Mueller").type(KeyCode.ENTER)
                .clickOn("Martin").type(KeyCode.E).clickOn("#tagList").write("frien").type(KeyCode.SPACE)
                .type(KeyCode.ENTER).sleep(200)
                .type(KeyCode.ENTER)
        .clickOn("Help").clickOn("About").clickOn("OK")
        .clickOn("File").clickOn("[Local] Save As...").sleep(1000).type(KeyCode.S).type(KeyCode.ENTER)
        .clickOn("File").clickOn("[Local] Save").clickOn("File").clickOn("Exit");
    }

    @After
    public void cleanup() {
        File file = new File("s.xml");
        file.delete();
    }
}
