package guiunittests;

import address.TestApp;
import address.controller.PersonEditDialogController;
import address.testutil.ApplicationTest;
import address.testutil.TypicalTestData;

import guitests.guihandles.EditPersonDialogHandle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * To test the basic features of the edit dialog
 */
public class PersonEditDialogUnitTest extends ApplicationTest {

    TypicalTestData data = new TypicalTestData();
    EditPersonDialogHandle handle;


    @Test
    public void testEditDialog_tabAndWrite() throws IOException {

        assertTrue(handle.isFirstNameFieldFocused());
        handle.write("First name");
        assertEquals("First name", handle.getFirstName());

        handle.pressTab();
        assertTrue(handle.isLastNameFieldFocused());
        handle.write("Last name");
        assertEquals("Last name", handle.getLastName());

        handle.pressTab();
        assertTrue(handle.isStreetFieldFocused());
        handle.write("Street");
        assertEquals("Street", handle.getStreet());

        handle.pressTab();
        assertTrue(handle.isCityFieldFocused());
        handle.write("City");
        assertEquals("City", handle.getCity());

        handle.pressTab();
        assertTrue(handle.isPostalCodeFieldFocused());
        handle.write("Postal");
        assertEquals("Postal", handle.getPostalCode());

        handle.pressTab();
        assertTrue(handle.isBirthdayFieldFocused());
        handle.write("01.03.1909");
        assertEquals("01.03.1909", handle.getBirthday());

        handle.pressTab();
        assertTrue(handle.isGithubUsernameFieldFocused());
        handle.write("Github username");
        assertEquals("Github username", handle.getGithubUserName());

        handle.pressTab();
        assertTrue(handle.isTagListFocused());
        handle.pressTab();
        assertTrue(handle.isOkButtonFocused());
        handle.pressTab();
        assertTrue(handle.isCancelButtonFocused());
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(TestApp.class.getResource("/view/PersonEditDialog.fxml"));
        AnchorPane baseNode = loader.load();

        PersonEditDialogController controller = loader.getController();

        controller.setInitialPersonData(data.dan);
        controller.setTags(data.book.getTagList(), data.dan.getTags());
        controller.setDialogStage(stage);
        stage.setScene(new Scene(baseNode));
        stage.show();
        handle = new EditPersonDialogHandle(this, stage, null);
    }
}
