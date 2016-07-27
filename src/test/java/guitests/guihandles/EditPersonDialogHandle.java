package guitests.guihandles;

import address.model.datatypes.person.Person;

import address.model.datatypes.tag.Tag;
import commons.DateTimeUtil;
import guitests.GuiRobot;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class EditPersonDialogHandle extends GuiHandle {

    public static final String EDIT_TITLE = "Edit Person";
    public static final String ADD_TITLE = "New Person";
    public static final String FIRST_NAME_FIELD_ID = "#firstNameField";
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
        assertTrue(isShowingEditDialog());
    }

    /**
     * Checks if the Edit Dialog is rendered correctly.
     * @return True : if the edit dialog is rendered with the necessary graphic to enter person details
     *         False : if otherwise.
     */
    public boolean isShowingEditDialog() {
        return getNode(FIRST_NAME_FIELD_ID) != null && getNode(LAST_NAME_FIELD_ID) != null
               && getNode(STREET_FIELD_ID) != null && getNode(CITY_FIELD_ID) != null
               && getNode(POSTAL_CODE_FIELD_ID) != null && getNode(BIRTHDAY_FIELD_ID) != null
               && getNode(GITHUB_USER_NAME_FIELD_ID) != null && getNode(TAG_SEARCH_FIELD_ID) != null;
    }

    public boolean isShowingEmptyEditDialog() {
        return getFirstName().equals("") && getLastName().equals("")
                && getCity().equals("") && getGithubUserName().equals("")
                && getStreet().equals("") && getBirthday().equals("")
                && getAssignedTagList().size() == 0;
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

    public String getStreet() {
        return getTextFieldText(STREET_FIELD_ID);
    }

    public String getBirthday() {
        return getTextFieldText(BIRTHDAY_FIELD_ID);
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

    public boolean isShowingPerson(Person person) {
        return getFirstName().equals(person.getFirstName()) && getLastName().equals(person.getLastName())
               && getCity().equals(person.getCity()) && getGithubUserName().equals(person.getGithubUsername())
               && getStreet().equals(person.getStreet()) //TODO: Can't compare birthday, as empty = null
               && getAssignedTagList().equals(person.getTagList());
    }

    public List<Tag> getAssignedTagList() {
        ScrollPane pane = (ScrollPane) getNode(TAG_SEARCH_FIELD_ID);
        VBox box = (VBox) pane.getContent();
        List<Label> tags = box.getChildren().stream().map(o -> (Label) o).collect(Collectors.toCollection(ArrayList::new));
        return tags.stream().map(t -> new Tag(t.getText())).collect(Collectors.toCollection(ArrayList::new));
    }

    public EditPersonDialogHandle enterNewValues(Person newValues) {
        enterFirstName(newValues.getFirstName());
        enterLastName(newValues.getLastName());
        enterStreet(newValues.getStreet());
        enterCity(newValues.getCity());
        enterPostalCode(newValues.getPostalCode());
        if (newValues.getBirthday() != null) {
            enterBirthday(newValues.getBirthday());
        }
        enterGithubId(newValues.getGithubUsername());
        TagPersonDialogHandle tagPersonDialog = openTagPersonDialog();
        newValues.getTagList().stream()
                 .forEach( (t) -> tagPersonDialog.enterSearchQuery(t.getName()).acceptSuggestedTag());
        tagPersonDialog.close();
        return this;
    }

    @Override
    public void pressEnter() {
        super.pressEnter();
        focusOnMainApp();
    }

    @Override
    protected void pressEsc() {
        super.pressEsc();
        focusOnMainApp();
    }

    @Override
    public void clickOk() {
        super.clickOk();
        guiRobot.sleep(500);
        focusOnMainApp();
    }
}
