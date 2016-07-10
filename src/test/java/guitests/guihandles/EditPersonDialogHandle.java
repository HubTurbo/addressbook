package guitests.guihandles;

import address.model.datatypes.person.Person;
import address.util.DateTimeUtil;
import guitests.GuiRobot;
import javafx.stage.Stage;

import java.time.LocalDate;

public class EditPersonDialogHandle extends GuiHandle {

    public static final String TITLE = "Edit Person";
    private String firstNameFieldId = "#firstNameField";
    private String lastNameFieldId = "#lastNameField";
    private String streetFieldId = "#streetField";
    private String cityFieldId = "#cityField";
    private String postalCodeFieldId = "#postalCodeField";
    private String birthdayFieldId = "#birthdayField";
    private String githubUserNameFieldId = "#githubUserNameField";
    private String tagSearchFieldId = "#tagList";
    private String cancelButtonText = "Cancel";

    public EditPersonDialogHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage);
    }

    public String getFirstName(){
        return getTextFieldText(firstNameFieldId);
    }

    public String getLastName(){
        return getTextFieldText(lastNameFieldId);
    }

    public void clickCancel(){
        guiRobot.clickOn(cancelButtonText);
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public String getCity() {
        return getTextFieldText(cityFieldId);
    }

    public String getGithubUserName() {
        return getTextFieldText(githubUserNameFieldId);
    }

    public EditPersonDialogHandle enterFirstName(String firstName) {
        typeTextField(firstNameFieldId, firstName);
        return this;
    }

    public EditPersonDialogHandle enterLastName(String lastName) {

        typeTextField(lastNameFieldId, lastName);
        return this;
    }

    public EditPersonDialogHandle enterCity(String city) {
        typeTextField(cityFieldId, city);
        return this;
    }

    public void enterStreet(String street) {
        typeTextField(streetFieldId, street);
    }

    public void enterPostalCode(String postalCode) {
        typeTextField(postalCodeFieldId, postalCode);
    }

    public void enterBirthday(LocalDate birthday) {
        typeTextField(birthdayFieldId, DateTimeUtil.format(birthday));
    }

    public EditPersonDialogHandle enterGithubId(String githubId) {
        typeTextField(githubUserNameFieldId, githubId);
        return this;
    }

    public TagPersonDialogHandle openTagPersonDialog() {
        guiRobot.clickOn(tagSearchFieldId)
                .sleep(200); // wait for opening animation
        return new TagPersonDialogHandle(guiRobot, primaryStage);
    }

    public void enterNewValues(Person newValues) {
        enterFirstName(newValues.getFirstName());
        enterLastName(newValues.getLastName());
        enterStreet(newValues.getStreet());
        enterCity(newValues.getCity());
        enterPostalCode(newValues.getPostalCode());
        enterBirthday(newValues.getBirthday());
        enterGithubId(newValues.getGithubUsername());
        TagPersonDialogHandle tagPersonDialog = openTagPersonDialog();
        newValues.getTagList().stream()
                .forEach( (t) -> tagPersonDialog.enterSearchQuery(t.getName()).acceptSuggestedTag());
        tagPersonDialog.close();
    }


}
