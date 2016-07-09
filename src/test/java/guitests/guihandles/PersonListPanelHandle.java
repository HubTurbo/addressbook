package guitests.guihandles;


import address.keybindings.Bindings;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import guitests.GuiRobot;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;

import java.util.List;

/**
 * Provides a handle for the panel containing the person list.
 */
public class PersonListPanelHandle extends GuiHandle {

    private String filterFieldId = "#filterField";
    private String personListViewId = "#personListView";

    public PersonListPanelHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage);
    }

    public void use_LIST_ENTER_SHORTCUT() {
        guiRobot.push(new Bindings().LIST_ENTER_SHORTCUT);
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

    public void use_PERSON_CHANGE_CANCEL_ACCELERATOR() {
        guiRobot.push(new Bindings().PERSON_CHANGE_CANCEL_ACCELERATOR);
    }

    public void use_LIST_JUMP_TO_INDEX_SHORTCUT(int index) {
        switch (index) {
            case 1:
                guiRobot.push(KeyCode.SHORTCUT, KeyCode.DIGIT1);
                break;
            case 2:
                guiRobot.push(KeyCode.SHORTCUT, KeyCode.DIGIT2);
                break;
            case 3:
                guiRobot.push(KeyCode.SHORTCUT, KeyCode.DIGIT3);
                break;
            case 4:
                guiRobot.push(KeyCode.SHORTCUT, KeyCode.DIGIT4);
                break;
            case 5:
                guiRobot.push(KeyCode.SHORTCUT, KeyCode.DIGIT5);
                break;
            case 6:
                guiRobot.push(KeyCode.SHORTCUT, KeyCode.DIGIT6);
                break;
            case 7:
                guiRobot.push(KeyCode.SHORTCUT, KeyCode.DIGIT7);
                break;
            case 8:
                guiRobot.push(KeyCode.SHORTCUT, KeyCode.DIGIT8);
                break;
            case 9:
                guiRobot.push(KeyCode.SHORTCUT, KeyCode.DIGIT9);
                break;
            default:
                throw new RuntimeException("Unsupported shortcut");
        }
    }

    public void use_LIST_GOTO_BOTTOM_SEQUENCE() {
        guiRobot.pushKeySequence(new Bindings().LIST_GOTO_BOTTOM_SEQUENCE);
    }

    public void use_LIST_GOTO_TOP_SEQUENCE() {
        guiRobot.pushKeySequence(new Bindings().LIST_GOTO_TOP_SEQUENCE);
    }

    public void use_PERSON_DELETE_ACCELERATOR() {
        guiRobot.push(new Bindings().PERSON_DELETE_ACCELERATOR);
    }

    public FxRobot waitForGracePeriodToExpire() {
        return guiRobot.sleep(4000);//TODO: tie the sleep duration to the actual grace period and implement a polling wait
    }

    public void navigateUp() {
        guiRobot.push(KeyCode.UP);
    }

    public void navigateDown() {
        guiRobot.push(KeyCode.DOWN);
    }

    public EditPersonDialogHandle use_PERSON_EDIT_ACCELERATOR() {
        guiRobot.push(new Bindings().PERSON_EDIT_ACCELERATOR);
        guiRobot.sleep(500);
        guiRobot.targetWindow(EditPersonDialogHandle.TITLE);
        return new EditPersonDialogHandle(guiRobot, primaryStage);
    }

    public TagPersonDialogHandle use_PERSON_TAG_ACCELERATOR() {
        guiRobot.push(new Bindings().PERSON_TAG_ACCELERATOR);
        guiRobot.sleep(500);
        return new TagPersonDialogHandle(guiRobot, primaryStage);
    }

    public void clickOnPerson(String personName) {
        guiRobot.clickOn(personName);
    }

    public void enterFilterAndApply(String filterText) {
        typeTextField(filterFieldId, filterText);
    }

    public String getFilterText() {
        return getTextFieldText(filterFieldId);
    }

    public EditPersonDialogHandle clickNew() {
        guiRobot.clickOn("#newButton");
        guiRobot.sleep(500);
        return new EditPersonDialogHandle(guiRobot, primaryStage);
    }

    public void dragAndDrop(String firstNameOfPersonToDrag, String firstNameOfPersonToDropOn) {
        guiRobot.drag(firstNameOfPersonToDrag).dropTo(firstNameOfPersonToDropOn);
    }

    public boolean containsInOrder(Person... persons) {
        assert persons.length >= 2;
        List<ReadOnlyViewablePerson> personsInList = getList().getItems();
        int indexOfFirstPerson = getPersonIndex(personsInList, persons[0]);
        if(indexOfFirstPerson < 0) return false;
        return containsInOrder(personsInList, indexOfFirstPerson, persons);
    }

    private boolean containsInOrder(List<ReadOnlyViewablePerson> fullList, int startPosition, Person[] targetPersons) {

        if (startPosition + targetPersons.length > fullList.size()){
            return false;
        }

        for (int i = 0; i < targetPersons.length; i++) {
            if (!fullList.get(startPosition + i).getFirstName().equals(targetPersons[i].getFirstName())){
                return false;
            }
        }

        return true;
    }

    private int getPersonIndex(List<ReadOnlyViewablePerson> fullList, Person targetPerson) {
        for (int i = 0; i < fullList.size(); i++) {
            if(fullList.get(i).getFirstName().equals(targetPerson.getFirstName())){
                return i;
            }
        }
        return -1;
    }
}
