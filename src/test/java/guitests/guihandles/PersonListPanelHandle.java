package guitests.guihandles;


import address.keybindings.Bindings;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import guitests.GuiRobot;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;

import java.util.List;
import java.util.Set;

/**
 * Provides a handle for the panel containing the person list.
 */
public class PersonListPanelHandle extends GuiHandle {

    public static final int NOT_FOUND = -1;
    public static final String CARD_PANE_ID = "#cardPane";
    private String filterFieldId = "#filterField";
    private String personListViewId = "#personListView";
    String newButtonId = "#newButton"; //TODO: convert to constants

    public PersonListPanelHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage);
    }

    public void use_LIST_ENTER_SHORTCUT() {
        guiRobot.push(new Bindings().LIST_ENTER_SHORTCUT);
    }

    public boolean contains(String firstName, String lastName) {
        return getListView().getItems().stream().anyMatch(p -> p.hasName(firstName, lastName));
    }

    public boolean isSelected(String firstName, String lastName) {
        return getSelectedPerson().hasName(firstName, lastName);
    }

    public boolean isSelected(Person person) {
        return getSelectedPerson().hasName(person.getFirstName(), person.getLastName());
    }

    public ReadOnlyViewablePerson getSelectedPerson() {
        ListView<ReadOnlyViewablePerson> personList = getListView();
        return personList.getSelectionModel().getSelectedItems().get(0);
    }

    public ListView<ReadOnlyViewablePerson> getListView() {
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

    public void clickOnPerson(Person person) {
        guiRobot.clickOn(person.getFirstName());
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
        guiRobot.clickOn(newButtonId);
        guiRobot.sleep(500);
        return new EditPersonDialogHandle(guiRobot, primaryStage);
    }

    public void dragAndDrop(String firstNameOfPersonToDrag, String firstNameOfPersonToDropOn) {
        guiRobot.drag(firstNameOfPersonToDrag).dropTo(firstNameOfPersonToDropOn);
    }

    /**
     * Returns true if the {@code persons} appear as a sub list (in that order) in the panel.
     */
    public boolean containsInOrder(Person... persons) {
        assert persons.length >= 2;
        int indexOfFirstPerson = getPersonIndex(persons[0]);
        if(indexOfFirstPerson == NOT_FOUND) return false;
        return containsInOrder(indexOfFirstPerson, persons);
    }

    /**
     * Returns true if the {@code persons} appear as the sub list (in that order) at position {@code startPosition}.
     */
    public boolean containsInOrder(int startPosition, Person... persons) {
        List<ReadOnlyViewablePerson> personsInList = getListView().getItems();

        //Return false if the list in panel is too short to contain the given list
        if (startPosition + persons.length > personsInList.size()){
            return false;
        }

        //Return false if any of the persons doesn't match
        for (int i = 0; i < persons.length; i++) {
            if (!personsInList.get(startPosition + i).getFirstName().equals(persons[i].getFirstName())){
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the position of the person given, {@code NOT_FOUND} if not found in the list.
     */
    public int getPersonIndex(Person targetPerson) {
        List<ReadOnlyViewablePerson> personsInList = getListView().getItems();
        for (int i = 0; i < personsInList.size(); i++) {
            if(personsInList.get(i).getFirstName().equals(targetPerson.getFirstName())){
                return i;
            }
        }
        return NOT_FOUND;
    }

    public PersonCardHandle getPersonCardHandle(Person person){
        Set<Node> nodes = guiRobot.lookup(CARD_PANE_ID).queryAll();
        Node personCardNode = nodes.stream()
                .filter( (n) -> new PersonCardHandle(guiRobot, primaryStage, n).isSamePerson(person))
                .findFirst().get();
        return new PersonCardHandle(guiRobot, primaryStage, personCardNode);
    }

    
}
