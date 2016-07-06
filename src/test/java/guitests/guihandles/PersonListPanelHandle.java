package guitests.guihandles;


import address.keybindings.Bindings;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import guitests.GuiTestBase;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;

/**
 * Provides a handle for the panel containing the person list.
 */
public class PersonListPanelHandle extends GuiHandle {

    private String filterFieldId = "#filterField";
    private String personListViewId = "#personListView";

    public PersonListPanelHandle(GuiTestBase guiTestBase) {
        super(guiTestBase);
    }

    public void use_LIST_ENTER_SHORTCUT() {
        guiTestBase.push(new Bindings().LIST_ENTER_SHORTCUT);
    }

    public boolean contains(String firstName, String lastName) {
        return getList().getItems().stream().anyMatch(p -> p.hasName(firstName, lastName));
    }

    public boolean isSelected(String firstName, String lastName) {
        return getSelectedPerson().hasName(firstName, lastName);
    }

    public ReadOnlyViewablePerson getSelectedPerson() {
        ListView<ReadOnlyViewablePerson> personList = getList();
        return personList.getSelectionModel().getSelectedItems().get(0);
    }

    public ListView<ReadOnlyViewablePerson> getList() {
        return (ListView<ReadOnlyViewablePerson>) getNode(personListViewId);
    }

    public void use_LIST_JUMP_TO_INDEX_SHORTCUT(int index) {
        switch (index) {
            case 1:
                guiTestBase.push(KeyCode.SHORTCUT, KeyCode.DIGIT1);
                break;
            case 2:
                guiTestBase.push(KeyCode.SHORTCUT, KeyCode.DIGIT2);
                break;
            case 3:
                guiTestBase.push(KeyCode.SHORTCUT, KeyCode.DIGIT3);
                break;
            case 4:
                guiTestBase.push(KeyCode.SHORTCUT, KeyCode.DIGIT4);
                break;
            case 5:
                guiTestBase.push(KeyCode.SHORTCUT, KeyCode.DIGIT5);
                break;
            case 6:
                guiTestBase.push(KeyCode.SHORTCUT, KeyCode.DIGIT6);
                break;
            case 7:
                guiTestBase.push(KeyCode.SHORTCUT, KeyCode.DIGIT7);
                break;
            case 8:
                guiTestBase.push(KeyCode.SHORTCUT, KeyCode.DIGIT8);
                break;
            case 9:
                guiTestBase.push(KeyCode.SHORTCUT, KeyCode.DIGIT9);
                break;
            default:
                throw new RuntimeException("Unsupported shortcut");
        }
    }

    public void use_LIST_GOTO_BOTTOM_SEQUENCE() {
        guiTestBase.pushKeySequence(new Bindings().LIST_GOTO_BOTTOM_SEQUENCE);
    }

    public void use_LIST_GOTO_TOP_SEQUENCE() {
        guiTestBase.pushKeySequence(new Bindings().LIST_GOTO_TOP_SEQUENCE);
    }

    public void use_PERSON_DELETE_ACCELERATOR() {
        guiTestBase.push(new Bindings().PERSON_DELETE_ACCELERATOR);
    }

    public FxRobot waitForGracePeriodToExpire() {
        return guiTestBase.sleep(4000);//TODO: tie the sleep duration to the actual grace period and implement a polling wait
    }

    public void navigateUp() {
        guiTestBase.push(KeyCode.UP);
    }

    public void navigateDown() {
        guiTestBase.push(KeyCode.DOWN);
    }

    public EditPersonDialogHandle use_PERSON_EDIT_ACCELERATOR() {
        guiTestBase.push(new Bindings().PERSON_EDIT_ACCELERATOR);
        guiTestBase.sleep(2000);
        guiTestBase.targetWindow(EditPersonDialogHandle.TITLE);
        return new EditPersonDialogHandle(guiTestBase);
    }

    public void clickOnPerson(String personName) {
        guiTestBase.clickOn(personName);
    }

    public void enterFilterAndApply(String filterText) {
        typeTextField(filterFieldId, filterText);
    }

    public String getFilterText() {
        return getTextFieldText(filterFieldId);
    }
}