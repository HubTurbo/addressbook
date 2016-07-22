package guitests.guihandles;


import address.TestApp;
import address.keybindings.Bindings;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.testutil.TestUtil;
import address.ui.PersonListViewCell;
import guitests.GuiRobot;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.PickResult;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Provides a handle for the panel containing the person list.
 */
public class PersonListPanelHandle extends GuiHandle {

    public static final int NOT_FOUND = -1;
    public static final String CARD_PANE_ID = "#cardPane";
    private static final String FILTER_FIELD_ID = "#filterField";
    private static final String PERSON_LIST_VIEW_ID = "#personListView";
    private static final String NEW_BUTTON_ID = "#newButton"; //TODO: convert to constants
    private static final String EDIT_BUTTON_ID = "#editButton";

    public enum ContextMenuChoice {
        EDIT, TAG, DELETE, CANCEL;
    }

    public PersonListPanelHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage, TestApp.APP_TITLE);
    }

    public void use_LIST_ENTER_SHORTCUT() {
        guiRobot.push(new Bindings().LIST_ENTER_SHORTCUT);
    }

    public boolean contains(String firstName, String lastName) {
        return getListView().getItems().stream().anyMatch(p -> p.hasName(firstName, lastName));
    }

    public boolean isSelected(String firstName, String lastName) {
        int count = 0;
        while(count < 10) {
            if (getSelectedPerson().hasName(firstName, lastName)){
                return true;
            }
            count ++;
            guiRobot.sleep(500);
        }
        return false;
    }

    public boolean isSelected(Person person) {
        return getSelectedPerson().hasName(person.getFirstName(), person.getLastName());
    }

    public ReadOnlyViewablePerson getSelectedPerson() {
        ListView<ReadOnlyViewablePerson> personList = getListView();
        return personList.getSelectionModel().getSelectedItems().get(0);
    }

    public ListView<ReadOnlyViewablePerson> getListView() {
        return (ListView<ReadOnlyViewablePerson>) getNode(PERSON_LIST_VIEW_ID);
    }

    /**
     * Clicks on the middle of the Listview.
     * In order for headfull testing to work in travis ci, listview needs to be clicked before firing hot keys.
     */
    public void clickOnListView() {
        double x = getListView().localToScreen(getListView().getLayoutBounds()).getMinX() + getListView().getWidth() / 2;
        double y = getListView().localToScreen(getListView().getLayoutBounds()).getMinY() + getListView().getHeight() / 2;
        guiRobot.clickOn(x, y);
    }

    /**
     * Fires ContextMenuEvent which shows a contextmenu in the middle of the Listview.
     */
    private void fireContextMenuEvent() {
        double x = getListView().localToScreen(getListView().getLayoutBounds()).getMinX() + getListView().getWidth() / 2;
        double y = getListView().localToScreen(getListView().getLayoutBounds()).getMinY() + getListView().getHeight() / 2;
        double sceneX = getListView().localToScene(getListView().getLayoutBounds()).getMinX() + getListView().getWidth() / 2;
        double sceneY = getListView().localToScene(getListView().getLayoutBounds()).getMinY() + getListView().getHeight() / 2;
        Event event = new ContextMenuEvent(ContextMenuEvent.CONTEXT_MENU_REQUESTED, sceneX, sceneY, x, y, false, new PickResult(getListView(), x, y));
        guiRobot.interact(() -> Event.fireEvent(getListView(), event));
    }

    public void use_PERSON_CHANGE_CANCEL_ACCELERATOR() {
        guiRobot.push(new Bindings().PERSON_CANCEL_COMMAND_ACCELERATOR);
    }

    public void use_LIST_JUMP_TO_INDEX_SHORTCUT(int index) {
        switch (index) {
            case 1:
                guiRobot.push(TestUtil.scrub(new KeyCode[]{KeyCode.SHORTCUT, KeyCode.DIGIT1}));
                break;
            case 2:
                guiRobot.push(TestUtil.scrub(new KeyCode[]{KeyCode.SHORTCUT, KeyCode.DIGIT2}));
                break;
            case 3:
                guiRobot.push(TestUtil.scrub(new KeyCode[]{KeyCode.SHORTCUT, KeyCode.DIGIT3}));
                break;
            case 4:
                guiRobot.push(TestUtil.scrub(new KeyCode[]{KeyCode.SHORTCUT, KeyCode.DIGIT4}));
                break;
            case 5:
                guiRobot.push(TestUtil.scrub(new KeyCode[]{KeyCode.SHORTCUT, KeyCode.DIGIT5}));
                break;
            case 6:
                guiRobot.push(TestUtil.scrub(new KeyCode[]{KeyCode.SHORTCUT, KeyCode.DIGIT6}));
                break;
            case 7:
                guiRobot.push(TestUtil.scrub(new KeyCode[]{KeyCode.SHORTCUT, KeyCode.DIGIT7}));
                break;
            case 8:
                guiRobot.push(TestUtil.scrub(new KeyCode[]{KeyCode.SHORTCUT, KeyCode.DIGIT8}));
                break;
            case 9:
                guiRobot.push(TestUtil.scrub(new KeyCode[]{KeyCode.SHORTCUT, KeyCode.DIGIT9}));
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

    public void navigateUp() {
        guiRobot.push(KeyCode.UP);
    }

    public void navigateDown() {
        guiRobot.push(KeyCode.DOWN);
    }

    public EditPersonDialogHandle use_PERSON_EDIT_ACCELERATOR() {
        guiRobot.push(new Bindings().PERSON_EDIT_ACCELERATOR);
        guiRobot.sleep(500);
        return new EditPersonDialogHandle(guiRobot, primaryStage, EditPersonDialogHandle.EDIT_TITLE);
    }

    public TagPersonDialogHandle use_PERSON_TAG_ACCELERATOR() {
        guiRobot.push(new Bindings().PERSON_TAG_ACCELERATOR);
        guiRobot.sleep(500);

        return new TagPersonDialogHandle(guiRobot, primaryStage);
    }

    public void clickOnPerson(Person person) {
        guiRobot.clickOn(person.getFirstName());
    }

    /**
     * Right click on Person to show context menu.
     * @param person
     */
    public void rightClickOnPerson(Person person) {
        //Instead of using guiRobot.rightCickOn(), We will be firing off contextmenu request manually.
        //As there is a bug in monocle that doesn't show contextmenu by actual right clicking.
        clickOnPerson(person);
        fireContextMenuEvent();
    }

    public EditPersonDialogHandle clickOnContextMenu(ContextMenuChoice choice) {
        switch (choice) {
            case EDIT:
                clickOn("#editMenuItem");
                guiRobot.sleep(500);
                return new EditPersonDialogHandle(guiRobot, primaryStage, EditPersonDialogHandle.EDIT_TITLE);
            case TAG:
                clickOn("#tagMenuItem");
                break;
            case DELETE:
                clickOn("#deleteMenuItem");
                break;
            case CANCEL:
                clickOn("#cancelOperationMenuItem");
                break;
        }
        return null;
    }


    public void focusListView() {
        guiRobot.interact(() -> this.getListView().requestFocus());
    }

    public void clickOnPerson(String personName) {
        guiRobot.clickOn(personName);
    }

    public void enterFilterAndApply(String filterText) {
        typeTextField(FILTER_FIELD_ID, filterText);
    }

    public String getFilterText() {
        return getTextFieldText(FILTER_FIELD_ID);
    }

    public EditPersonDialogHandle clickNew() {
        guiRobot.clickOn(NEW_BUTTON_ID);
        guiRobot.sleep(500);
        return new EditPersonDialogHandle(guiRobot, primaryStage, EditPersonDialogHandle.ADD_TITLE);
    }

    public EditPersonDialogHandle clickEdit() {
        guiRobot.clickOn(EDIT_BUTTON_ID);
        guiRobot.sleep(500);
        return new EditPersonDialogHandle(guiRobot, primaryStage, EditPersonDialogHandle.EDIT_TITLE);
    }

    public void dragAndDrop(String firstNameOfPersonToDrag, String firstNameOfPersonToDropOn) {
        guiRobot.drag(firstNameOfPersonToDrag).dropTo(firstNameOfPersonToDropOn);
    }


    public void dragOutsideList(List<String> listOfPersonsToDrag) {
        double posY = this.getListView().localToScene(this.getListView().getBoundsInLocal()).getMaxY()
                - 50;
        double posX = this.getListView().localToScene(this.getListView().getBoundsInLocal()).getMaxX()
                + 100;
        guiRobot.press(KeyCode.SHORTCUT);
        listOfPersonsToDrag.stream().forEach(p -> guiRobot.clickOn(p));
        guiRobot.release(KeyCode.SHORTCUT);
        guiRobot.drag(listOfPersonsToDrag.get(listOfPersonsToDrag.size() -1))
                .dropTo(posX, posY);
    }

    private void scrollToPerson(String firstName) {
        Optional<ReadOnlyViewablePerson> person = getListView().getItems()
                                                               .stream()
                                                               .filter(p -> p.getFirstName()
                                                               .equals(firstName)).findAny();
        getListView().scrollTo(getListView().getItems().indexOf(person.get()));
    }

    public void dragOutsideList(String personToDrag) {
        double posY = this.getListView().localToScene(this.getListView().getBoundsInLocal()).getMaxY()
                - 50;
        double posX = this.getListView().localToScene(this.getListView().getBoundsInLocal()).getMaxX()
                + 100;
        guiRobot.drag(personToDrag).dropTo(posX, posY);
    }

    public void dragOutsideApp(String personToDrag) {
        double x = this.primaryStage.getScene().getX() + this.primaryStage.getScene().getWidth() + 10;
        double y = this.primaryStage.getScene().getY() + this.primaryStage.getScene().getHeight() + 10;
        guiRobot.drag(personToDrag).dropTo(x, y);
    }

    public void dragOutsideApp(List<String> listOfPersonsToDrag) {
        double x = this.primaryStage.getScene().getX() + this.primaryStage.getScene().getWidth() + 10;
        double y = this.primaryStage.getScene().getY() + this.primaryStage.getScene().getHeight() + 10;
        guiRobot.press(KeyCode.SHORTCUT);
        listOfPersonsToDrag.stream().forEach(p -> guiRobot.clickOn(p));
        guiRobot.release(KeyCode.SHORTCUT);
        guiRobot.drag(listOfPersonsToDrag.get(listOfPersonsToDrag.size() -1))
                .dropTo(x, y);
    }

    public void edgeDrag(List<String> dragFrom, VerticalDirection direction, long dragDuration, TimeUnit timeunit) {
        switch (direction) {
            case UP:
                double edgeMinY = this.getListView().localToScreen(this.getListView().getBoundsInLocal()).getMinY()
                                                                  + PersonListViewCell.SCROLL_AREA / 2 - 3;
                double edgeMinX = this.getListView().localToScreen(this.getListView().getBoundsInLocal()).getMinX()
                                                                  + this.getListView().getWidth() / 2;
                guiRobot.press(TestUtil.scrub(new KeyCode[] {KeyCode.SHORTCUT})[0]);
                dragFrom.stream().forEach(p -> guiRobot.clickOn(p));
                guiRobot.release(TestUtil.scrub(new KeyCode[] {KeyCode.SHORTCUT})[0]);
                guiRobot.drag(dragFrom.get(dragFrom.size() - 1)).drag(edgeMinX, edgeMinY).sleep(dragDuration, timeunit).drop();
                break;
            case DOWN:
                double edgeMaxY = this.getListView().localToScreen(this.getListView().getBoundsInLocal()).getMaxY()
                                                                  - PersonListViewCell.SCROLL_AREA / 2;
                double edgeMaxX = this.getListView().localToScreen(this.getListView().getBoundsInLocal()).getMinX()
                                                                  + this.getListView().getWidth() / 2;
                guiRobot.press(TestUtil.scrub(new KeyCode[] {KeyCode.SHORTCUT})[0]);
                dragFrom.stream().forEach(p -> guiRobot.clickOn(p));
                guiRobot.release(TestUtil.scrub(new KeyCode[] {KeyCode.SHORTCUT})[0]);
                guiRobot.drag(dragFrom.get(dragFrom.size() - 1)).drag(edgeMaxX, edgeMaxY).sleep(dragDuration, timeunit).drop();
                break;
        }
    }

    public void edgeDrag(String dragFrom, VerticalDirection direction, long dragDuration, TimeUnit timeunit) {
        switch (direction) {
            case UP:
                double edgeMinY = this.getListView().localToScreen(this.getListView().getBoundsInLocal()).getMinY()
                        + PersonListViewCell.SCROLL_AREA / 2 - 3;
                double edgeMinX = this.getListView().localToScreen(this.getListView().getBoundsInLocal()).getMinX()
                        + this.getListView().getWidth() / 2;
                guiRobot.drag(dragFrom).drag(edgeMinX, edgeMinY).sleep(dragDuration, timeunit).drop();
                break;
            case DOWN:
                double edgeMaxY = this.getListView().localToScreen(this.getListView().getBoundsInLocal()).getMaxY()
                        - PersonListViewCell.SCROLL_AREA / 2;
                double edgeMaxX = this.getListView().localToScreen(this.getListView().getBoundsInLocal()).getMinX()
                        + this.getListView().getWidth() / 2;
                guiRobot.drag(dragFrom).drag(edgeMaxX, edgeMaxY).sleep(dragDuration, timeunit).drop();
                break;
        }
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

    public void clearSelection() {
        getListView().getSelectionModel().clearSelection();
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
            if (!personsInList.get(startPosition + i).fullName().equals(persons[i].fullName())){
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

    public PersonCardHandle getPersonCardHandle(int index) {
        return getPersonCardHandle(new Person(getListView().getItems().get(index)));
    }

    public PersonCardHandle getPersonCardHandle(Person person){
        Set<Node> nodes = getAllCardNodes();
        Optional<Node> personCardNode = nodes.stream()
                .filter( (n) -> new PersonCardHandle(guiRobot, primaryStage, n).isSamePerson(person))
                .findFirst();
        if (personCardNode.isPresent()) {
            return new PersonCardHandle(guiRobot, primaryStage, personCardNode.get());
        } else {
            return null;
        }

    }

    protected Set<Node> getAllCardNodes() {
        return guiRobot.lookup(CARD_PANE_ID).queryAll();
    }

    public List<PersonCardHandle> getSelectedCards() {
        ObservableList<ReadOnlyViewablePerson> persons = getListView().getSelectionModel().getSelectedItems();
        return persons.stream()
                      .map(p -> getPersonCardHandle(new Person(p)))
                      .collect(Collectors.toCollection(ArrayList::new));
    }
}
