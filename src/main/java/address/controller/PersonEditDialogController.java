package address.controller;

import address.model.*;
import address.events.EventManager;
import address.events.GroupSearchResultsChangedEvent;
import address.events.GroupsChangedEvent;
import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import address.util.DateUtil;

import java.net.MalformedURLException;
import java.net.URL;
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
    @FXML
    private TextField webPageField;

    private PersonEditDialogModel model;
    private Person person;
    private ModelManager modelManager;

    public PersonEditDialogController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        addListener();
        EventManager.getInstance().registerHandler(this);
    }

    private void addListener() {
        groupSearch.textProperty().addListener((observableValue, oldValue, newValue) -> {
                handleInput(newValue);
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

    public void setModelManager(ModelManager modelManager){
        this.modelManager = modelManager;
    }

    /**
     * Sets the person to be edited in the dialog.
     *
     * @param person
     */
    public void setPerson(Person person) {
        this.person = person;

        firstNameField.setText(person.getFirstName());
        lastNameField.setText(person.getLastName());
        streetField.setText(person.getStreet());
        postalCodeField.setText(Integer.toString(person.getPostalCode()));
        cityField.setText(person.getCity());
        birthdayField.setText(DateUtil.format(person.getBirthday()));
        birthdayField.setPromptText("dd.mm.yyyy");
        webPageField.setText(person.getWebPageUrl().toExternalForm());
    }

    public void setModel(List<ContactGroup> contactGroups, List<ContactGroup> assignedGroups) {
        model = new PersonEditDialogModel(contactGroups, assignedGroups);
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


    /**
     * Called when the user clicks ok.
     */
    @FXML
    protected void handleOk() {
        if (isInputValid()) {
            //Call the update method instead of updating the Person object directly
            //  to ensure proper event handling for model update.
            Person updated = new Person();
            updated.setFirstName(firstNameField.getText());
            updated.setLastName(lastNameField.getText());
            updated.setStreet(streetField.getText());
            updated.setPostalCode(Integer.parseInt(postalCodeField.getText()));
            updated.setCity(cityField.getText());
            updated.setBirthday(DateUtil.parse(birthdayField.getText()));
            updated.setContactGroups(model.getAssignedGroups());
            try {
                updated.setWebPageUrl(new URL(webPageField.getText()));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Error parsing an parsed parsable URL");
            }
            modelManager.updatePerson(person, updated);

            isOkClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleInput(String newInput) {
        model.setFilter(newInput);
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

        try {
            URL url = new URL(webPageField.getText());
        } catch (MalformedURLException e) {
            errorMessage += "Invalid web page link.\n";
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
}
