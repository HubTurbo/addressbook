package guitests.guihandles;

import address.model.datatypes.person.Person;
import address.util.DateTimeUtil;
import com.google.common.io.Files;
import guitests.GuiRobot;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.loadui.testfx.GuiTest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.NoSuchElementException;

public class EditPersonDialogHandle extends GuiHandle {

    public static final String EDIT_TITLE = "Edit Person";
    public static final String ADD_TITLE = "New Person";
    private static final String FIRST_NAME_FIELD_ID = "#firstNameField";
    private static final String LAST_NAME_FIELD_ID = "#lastNameField";
    private static final String STREET_FIELD_ID = "#streetField";
    private static final String CITY_FIELD_ID = "#cityField";
    private static final String POSTAL_CODE_FIELD_ID = "#postalCodeField";
    private static final String BIRTHDAY_FIELD_ID = "#birthdayField";
    private static final String GITHUB_USER_NAME_FIELD_ID = "#githubUserNameField";
    private static final String TAG_SEARCH_FIELD_ID = "#tagList";
    private static final String CANCEL_BUTTON_TEXT = "Cancel";

    public EditPersonDialogHandle(GuiRobot guiRobot, Stage primaryStage, String stageTitle) {
        super(guiRobot, primaryStage, stageTitle);
    }

    public boolean isValidEditDialog() {
        return getNode(FIRST_NAME_FIELD_ID) != null && getNode(LAST_NAME_FIELD_ID) != null
               && getNode(STREET_FIELD_ID) != null && getNode(CITY_FIELD_ID) != null
               && getNode(POSTAL_CODE_FIELD_ID) != null && getNode(BIRTHDAY_FIELD_ID) != null
               && getNode(GITHUB_USER_NAME_FIELD_ID) != null && getNode(TAG_SEARCH_FIELD_ID) != null;
    }

    public boolean isInputValidationErrorDialogShown() {
        try{
            Window window = guiRobot.window("Invalid Fields");
            return window != null && window.isShowing();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public String getFirstName(){
        return getTextFieldText(FIRST_NAME_FIELD_ID);
    }

    public String getLastName(){
        return getTextFieldText(LAST_NAME_FIELD_ID);
    }

    public void clickCancel(){
        guiRobot.clickOn(CANCEL_BUTTON_TEXT);
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public String getCity() {
        return getTextFieldText(CITY_FIELD_ID);
    }

    public String getGithubUserName() {
        return getTextFieldText(GITHUB_USER_NAME_FIELD_ID);
    }

    public EditPersonDialogHandle enterFirstName(String firstName) {
        typeTextField(FIRST_NAME_FIELD_ID, firstName);
        return this;
    }

    public EditPersonDialogHandle enterLastName(String lastName) {

        typeTextField(LAST_NAME_FIELD_ID, lastName);
        return this;
    }

    public EditPersonDialogHandle enterCity(String city) {
        typeTextField(CITY_FIELD_ID, city);
        return this;
    }

    public void enterStreet(String street) {
        typeTextField(STREET_FIELD_ID, street);
    }

    public void enterPostalCode(String postalCode) {
        typeTextField(POSTAL_CODE_FIELD_ID, postalCode);
    }

    public void enterBirthday(LocalDate birthday) {
        typeTextField(BIRTHDAY_FIELD_ID, DateTimeUtil.format(birthday));
    }

    public EditPersonDialogHandle enterGithubId(String githubId) {
        typeTextField(GITHUB_USER_NAME_FIELD_ID, githubId);
        return this;
    }

    public TagPersonDialogHandle openTagPersonDialog() {
        guiRobot.clickOn(TAG_SEARCH_FIELD_ID)
                .sleep(200); // wait for opening animation
        return new TagPersonDialogHandle(guiRobot, primaryStage);
    }

    public void enterNewValues(Person newValues) throws IOException {
        File file = null;
        enterFirstName(newValues.getFirstName());
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("1.png"));
        enterLastName(newValues.getLastName());
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("2.png"));
        enterStreet(newValues.getStreet());
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("3.png"));
        enterCity(newValues.getCity());
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("4.png"));
        enterPostalCode(newValues.getPostalCode());
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("5.png"));
        enterBirthday(newValues.getBirthday());
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("6.png"));
        enterGithubId(newValues.getGithubUsername());
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("7.png"));
        TagPersonDialogHandle tagPersonDialog = openTagPersonDialog();
        newValues.getTagList().stream()
                .forEach( (t) -> tagPersonDialog.enterSearchQuery(t.getName()).acceptSuggestedTag());
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("8.png"));
        tagPersonDialog.close();
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("9.png"));
    }


}
