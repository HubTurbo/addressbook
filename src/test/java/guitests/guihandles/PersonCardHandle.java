package guitests.guihandles;

import address.controller.PersonCardController;
import address.model.datatypes.person.Person;
import guitests.GuiRobot;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Provides a handle to a person card in the person list panel.
 */
public class PersonCardHandle extends GuiHandle {
    private static final String FIRST_NAME_FIELD_ID = "#firstName";
    private static final String LAST_NAME_FIELD_ID = "#lastName";
    private static final String ADDRESS_FIELD_ID = "#address";
    private static final String BIRTHDAY_FIELD_ID = "#birthday";
    private Node node;

    public PersonCardHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage);
    }

    public PersonCardHandle(GuiRobot guiRobot, Stage primaryStage, Node node){
        super(guiRobot, primaryStage);
        this.node = node;
    }

    public String getLastName(){
        return getTextFromLabel(LAST_NAME_FIELD_ID);
    }

    protected String getTextFromLabel(String fieldId) {
        return getTextFromLabel(fieldId, node);
    }

    public String getFirstName() {
        return getTextFromLabel(FIRST_NAME_FIELD_ID);
    }

    public String getAddress() {
        return getTextFromLabel(ADDRESS_FIELD_ID);
    }

    public String getBirthday() {
        return getTextFromLabel(BIRTHDAY_FIELD_ID);
    }

    public boolean isSamePerson(Person person){
        return getFirstName().equals(person.getFirstName())
                && getLastName().equals(person.getLastName())
                && getAddress().equals(PersonCardController.getAddressString(person.getStreet(),
                                                                person.getCity(), person.getPostalCode()));
    }

    @Override
    public String toString() {
        return getFirstName() + " " + getLastName() + " " + getAddress() + " " + getBirthday();
    }
}
