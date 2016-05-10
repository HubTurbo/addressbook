package address.controller;

import java.util.List;

import address.events.EventManager;
import address.events.FilterCommittedEvent;
import address.model.ContactGroup;
import address.model.ModelManager;
import address.model.Person;
import address.parser.ParseException;
import address.parser.Parser;
import address.parser.expr.Expr;
import address.parser.expr.PredExpr;
import address.util.DateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class PersonOverviewController {
    @FXML
    private TableView<Person> personTable;
    @FXML
    private TableColumn<Person, String> firstNameColumn;
    @FXML
    private TableColumn<Person, String> lastNameColumn;

    @FXML
    private TextField filterField;

    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label streetLabel;
    @FXML
    private Label postalCodeLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label birthdayLabel;
    @FXML
    private Label contactGroupLabel;

    private MainController mainController;
    private ModelManager modelManager;

    public PersonOverviewController() {
        EventManager.getInstance().registerHandler(this);
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize the person table with the two columns.
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        
        // Clear person details.
        showPersonDetails(null);

        // Listen for selection changes and show the person details when changed.
        personTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showPersonDetails(newValue));
    }

   public void setConnections(MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;
        // Add observable list data to the table
        personTable.setItems(modelManager.getPersonData());
    }

    private String getContactGroupsString(List<ContactGroup> contactGroups) {
        String contactGroupsString = "";
        for (int i = 0; i < contactGroups.size(); i++) {
            if (i > 0) {
                contactGroupsString += ", ";
            }
            contactGroupsString += contactGroups.get(i).getName();
        }
        return contactGroupsString;
    }
    
    /**
     * Fills all text fields to show details about the person.
     * If the specified person is null, all text fields are cleared.
     * 
     * @param person the person or null
     */
    private void showPersonDetails(Person person) {
        if (person != null) {
            // Fill the labels with info from the person object.
            firstNameLabel.setText(person.getFirstName());
            lastNameLabel.setText(person.getLastName());
            streetLabel.setText(person.getStreet());
            postalCodeLabel.setText(Integer.toString(person.getPostalCode()));
            cityLabel.setText(person.getCity());
            birthdayLabel.setText(DateUtil.format(person.getBirthday()));
            List<ContactGroup> contactGroups = person.getContactGroups();
            contactGroupLabel.setText(getContactGroupsString(contactGroups));
        } else {
            // Person is null, remove all the text.
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            streetLabel.setText("");
            postalCodeLabel.setText("");
            cityLabel.setText("");
            birthdayLabel.setText("");
            contactGroupLabel.setText("");
        }
    }
    
    /**
     * Called when the user clicks on the delete button.
     */
    @FXML
    private void handleDeletePerson() {
        int selectedIndex = personTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            modelManager.deletePerson(personTable.getItems().get(selectedIndex));
        } else {
            // Nothing selected.
            mainController.showWarningDialogAndWait("No Selection",
                    "No Person Selected", "Please select a person in the table.");
        }
    }
    
    /**
     * Called when the user clicks the new button. Opens a dialog to edit
     * details for a new person.
     */
    @FXML
    private void handleNewPerson() {
        Person tempPerson = new Person();
        boolean okClicked = mainController.showPersonEditDialog(tempPerson);
        if (okClicked) {
            modelManager.addPerson(tempPerson);
        }
    }

    /**
     * Called when the user clicks the edit button. Opens a dialog to edit
     * details for the selected person.
     */
    @FXML
    private void handleEditPerson() {
        Person selectedPerson = personTable.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {
            boolean okClicked = mainController.showPersonEditDialog(selectedPerson);
            if (okClicked) {
                showPersonDetails(selectedPerson);
            }

        } else {
            // Nothing selected.
            mainController.showWarningDialogAndWait("No Selection",
                    "No Person Selected", "Please select a person in the table.");
        }
    }

    @FXML
    private void handleFilterChanged() {
        Expr filterExpression = PredExpr.TRUE;
        boolean isFilterValid = true;
        try {
            filterExpression = Parser.parse(filterField.getText());
        } catch (ParseException ignored) {
            isFilterValid = false;
        }

        if (isFilterValid || filterField.getText().isEmpty()) {
            filterField.getStyleClass().remove("error");
        } else {
            filterField.getStyleClass().add("error");
        }
        EventManager.getInstance().post(new FilterCommittedEvent(filterExpression));
    }
}