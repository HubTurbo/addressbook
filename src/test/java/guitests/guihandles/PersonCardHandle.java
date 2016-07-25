package guitests.guihandles;

import address.TestApp;
import address.controller.PersonCardController;
import address.model.datatypes.person.Person;
import guitests.GuiRobot;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Provides a handle to a person card in the person list panel.
 */
public class PersonCardHandle extends GuiHandle {
    private static final String FIRST_NAME_FIELD_ID = "#firstName";
    private static final String LAST_NAME_FIELD_ID = "#lastName";
    private static final String ADDRESS_FIELD_ID = "#address";
    private static final String BIRTHDAY_FIELD_ID = "#birthday";
    private static final String PENDING_STATE_LABEL_FIELD_ID = "#commandTypeLabel";
    private static final String PENDING_STATE_PROGRESS_INDICATOR_FIELD_ID = "#remoteRequestOngoingIndicator";
    private static final String PENDING_STATE_ROOT_FIELD_ID = "#commandStateDisplayRootNode";

    private Node node;

    public PersonCardHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage, TestApp.APP_TITLE);
    }

    public PersonCardHandle(GuiRobot guiRobot, Stage primaryStage, Node node){
        super(guiRobot, primaryStage, TestApp.APP_TITLE);
        this.node = node;
    }

    private boolean isPendingStateLabelVisible() {
        return guiRobot.lookup(PENDING_STATE_LABEL_FIELD_ID).query().isVisible();
    }

    private boolean isPendingStateProgressIndicatorVisible() {
        return guiRobot.lookup(PENDING_STATE_PROGRESS_INDICATOR_FIELD_ID).query().isVisible();
    }

    private boolean isPendingStateRootVisible() {
        return guiRobot.lookup(PENDING_STATE_ROOT_FIELD_ID).query().isVisible();
    }

    public boolean isShowingGracePeriod(String displayText) {
        return this.isPendingStateRootVisible() && this.isPendingStateLabelVisible()
               && !this.isPendingStateProgressIndicatorVisible() && this.getPendingStateLabel().equals(displayText);
    }

    public String getPendingStateLabel() {
        return getTextFromLabel(PENDING_STATE_LABEL_FIELD_ID);
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
    public boolean equals(Object obj) {
        if (obj instanceof Person) {
            Person person = (Person) obj;
            return getFirstName().equals(person.getFirstName()) && getLastName().equals(person.getLastName())
                   && getAddress().equals(PersonCardController.getAddressString(person.getStreet(), person.getCity(),
                                                                                person.getPostalCode()));
        }

        if(obj instanceof PersonCardHandle) {
            PersonCardHandle handle = (PersonCardHandle) obj;
            return getFirstName().equals(handle.getFirstName()) && getLastName().equals(handle.getLastName())
                   && getAddress().equals(handle.getAddress()) && getBirthday().equals(handle.getBirthday());
        }

        return super.equals(obj);
    }

    @Override
    public String toString() {
        return getFirstName() + " " + getLastName() + " " + getAddress() + " " + getBirthday();
    }
}
