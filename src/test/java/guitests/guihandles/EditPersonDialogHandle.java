package guitests.guihandles;

import guitests.GuiTestBase;

public class EditPersonDialogHandle extends GuiHandle {

    public static final String TITLE = "Edit Person";
    private String githubUserNameFieldId = "#githubUserNameField";
    private String firstNameFieldId = "#firstNameField";
    private String lastNameFieldId = "#lastNameField";
    private String cityFieldId = "#cityField";
    private String tagSearchFieldId = "#tagList";
    private String cancelButtonText = "Cancel";

    public EditPersonDialogHandle(GuiTestBase guiTestBase) {
        super(guiTestBase);
    }

    public String getFirstName(){
        return getTextFieldText(firstNameFieldId);
    }

    public String getLastName(){
        return getTextFieldText(lastNameFieldId);
    }

    public void clickCancel(){
        guiTestBase.clickOn(cancelButtonText);
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

    public EditPersonDialogHandle enterGithubId(String githubId) {
        typeTextField(githubUserNameFieldId, githubId);
        return this;
    }

    public TagPersonDialogHandle openTagPersonDialog() {
        guiTestBase.clickOn(tagSearchFieldId)
                .sleep(200); // wait for opening animation
        return new TagPersonDialogHandle(guiTestBase);
    }

}