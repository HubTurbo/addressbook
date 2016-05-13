package address.controller;

import address.events.EventManager;
import address.events.FilterCommittedEvent;
import address.model.ModelManager;
import address.model.Person;
import address.parser.ParseException;
import address.parser.Parser;
import address.parser.expr.Expr;
import address.parser.expr.PredExpr;
import address.ui.PersonListViewCell;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class PersonOverviewController {

    @FXML
    private ListView<Person> personList;

    @FXML
    private TextField filterField;

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
    }

    public void setConnections(MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;

        // Add observable list data to the list
        personList.setItems(modelManager.getFilteredPersons());
        personList.setCellFactory(listView -> new PersonListViewCell());
    }


    /**
     * Called when the user clicks on the delete button.
     */
    @FXML
    private void handleDeletePerson() {
        int selectedIndex = personList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            modelManager.deletePerson(personList.getItems().get(selectedIndex));
        } else {
            // Nothing selected.
            mainController.showWarningDialogAndWait("No Selection",
                    "No Person Selected", "Please select a person in the list.");
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
        Person selectedPerson = personList.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {
            boolean okClicked = mainController.showPersonEditDialog(selectedPerson);
        } else {
            // Nothing selected.
            mainController.showWarningDialogAndWait("No Selection",
                    "No Person Selected", "Please select a person in the list.");
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
