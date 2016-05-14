package address.controller;

import address.model.*;
import address.events.EventManager;
import address.events.GroupSearchResultsChangedEvent;
import address.events.GroupsChangedEvent;
import address.util.DateUtil;

import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Dialog to edit details of a person.
 */
public class PersonEditDialogController extends EditDialogController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField streetField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField birthdayField;

    @FXML
    private ScrollPane groupList;
    @FXML
    private TextField groupSearch;
    @FXML
    private ScrollPane groupResults;

    private PersonEditDialogGroupsModel model;
    private Person finalPerson;

    public PersonEditDialogController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        addListeners();
        EventManager.getInstance().registerHandler(this);
    }

    private void addListeners() {
        groupSearch.textProperty().addListener((observableValue, oldValue, newValue) -> {
                handleGroupInput(newValue);
            });
        groupSearch.setOnKeyTyped(e -> {
                switch (e.getCharacter()) {
                case " ":
                    e.consume();
                    model.toggleSelection();
                    groupSearch.clear();
                    break;
                default:
                    break;
                }
            });
        groupSearch.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                case DOWN:
                    e.consume();
                    model.selectNext();
                    break;
                case UP:
                    e.consume();
                    model.selectPrevious();
                    break;
                default:
                    break;
                }
            });
    }

    /**
     * Sets the stage of this dialog.
     * 
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the initial placeholder data in the dialog fields
     */
    public void setInitialPersonData(Person person) {
        firstNameField.setText(person.getFirstName());
        lastNameField.setText(person.getLastName());
        streetField.setText(person.getStreet());
        postalCodeField.setText(Integer.toString(person.getPostalCode()));
        cityField.setText(person.getCity());
        birthdayField.setText(DateUtil.format(person.getBirthday()));
        birthdayField.setPromptText("dd.mm.yyyy");
    }

    public void setGroupsModel(List<ContactGroup> contactGroups, List<ContactGroup> assignedGroups) {
        model = new PersonEditDialogGroupsModel(contactGroups, assignedGroups);
    }

    /**
     * Called when the user clicks ok.
     * Stores input as a Person object into finalData and isOkClicked flag to true
     */
    @FXML
    protected void handleOk() {
        if (isInputValid()) {
            finalPerson = new Person();
            finalPerson.setFirstName(firstNameField.getText());
            finalPerson.setLastName(lastNameField.getText());
            finalPerson.setStreet(streetField.getText());
            finalPerson.setPostalCode(Integer.parseInt(postalCodeField.getText()));
            finalPerson.setCity(cityField.getText());
            finalPerson.setBirthday(DateUtil.parse(birthdayField.getText()));
            finalPerson.setContactGroups(model.getAssignedGroups());

            isOkClicked = true;
            dialogStage.close();
        }
    }

    public Person getPersonData() {
        return finalPerson;
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    protected void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates the user input in the text fields.
     * 
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (firstNameField.getText() == null || firstNameField.getText().length() == 0) {
            errorMessage += "No valid first name!\n"; 
        }
        if (lastNameField.getText() == null || lastNameField.getText().length() == 0) {
            errorMessage += "No valid last name!\n"; 
        }
        if (streetField.getText() == null || streetField.getText().length() == 0) {
            errorMessage += "No valid street!\n"; 
        }

        if (postalCodeField.getText() == null || postalCodeField.getText().length() == 0) {
            errorMessage += "No valid postal code!\n"; 
        } else {
            // try to parse the postal code into an int.
            try {
                Integer.parseInt(postalCodeField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid postal code (must be an integer)!\n"; 
            }
        }

        if (cityField.getText() == null || cityField.getText().length() == 0) {
            errorMessage += "No valid city!\n"; 
        }

        if (birthdayField.getText() == null || birthdayField.getText().length() == 0) {
            errorMessage += "No valid birthday!\n";
        } else {
            if (!DateUtil.validDate(birthdayField.getText())) {
                errorMessage += "No valid birthday. Use the format dd.mm.yyyy!\n";
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            
            alert.showAndWait();
            
            return false;
        }
    }

    @Subscribe
    public void handleGroupSearchResultsChangedEvent(GroupSearchResultsChangedEvent e) {
        groupResults.setContent(getContactGroupsVBox(e.getSelectableContactGroups(), true));
    }

    @Subscribe
    public void handleGroupsChangedEvent(GroupsChangedEvent e) {
        groupList.setContent(getContactGroupsVBox(e.getResultGroup(), false));
    }

    private VBox getContactGroupsVBox(List<SelectableContactGroup> contactGroupList, boolean isSelectable) {
        VBox content = new VBox();
        contactGroupList.stream()
                .forEach(contactGroup -> {
                    Label newLabel = new Label(contactGroup.getName());
                    if (isSelectable && contactGroup.isSelected()) {
                        newLabel.setStyle("-fx-background-color: blue;");
                    }
                    newLabel.setPrefWidth(261);
                    content.getChildren().add(newLabel);
                });

        return content;
    }

    @FXML
    protected void handleGroupInput(String newGroups) {
        model.setFilter(newGroups);
    }


}
