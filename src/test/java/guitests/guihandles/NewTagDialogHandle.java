package guitests.guihandles;

import guitests.GuiTestBase;

/**
 * Provides a handle to the dialog used for adding a new tag.
 */
public class NewTagDialogHandle extends GuiHandle {

    private String tagNameFieldId = "#tagNameField";

    public NewTagDialogHandle(GuiTestBase guiTestBase) {
        super(guiTestBase);
    }

    public String getTagName() {
        return getTextFieldText(tagNameFieldId);
    }

    public void enterTagName(String tagName) {
        typeTextField(tagNameFieldId, tagName);
    }

}
