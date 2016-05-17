package address.controller;

import address.events.EventManager;
import address.events.FilterCommittedEvent;
import address.exceptions.DuplicatePersonException;
import address.model.ModelManager;
import address.model.Person;
import address.parser.ParseException;
import address.parser.Parser;
import address.parser.expr.Expr;
import address.parser.expr.PredExpr;
import address.ui.PersonListViewCell;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.Optional;

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
        personList.setContextMenu(createContextMenu());
    }

    public void setConnections(MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;

        // Add observable list data to the list
        personList.setItems(modelManager.getFilteredPersons());
        personList.setCellFactory(listView -> new PersonListViewCell());
        personList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)
                        -> mainController.loadGithubProfilePage(newValue.getGithubUserName()));
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
            mainController.showAlertDialogAndWait(AlertType.WARNING,
                    "No Selection", "No Person Selected", "Please select a person in the list.");
        }
    }

    /**
     * Called when the user clicks the new button. Opens a dialog to edit
     * details for a new person.
     */
    @FXML
    private void handleNewPerson() {
        Optional<Person> newPerson = Optional.of(new Person());
        while (true) { // keep re-asking until user provides valid input or cancels operation.
            newPerson = mainController.getPersonDataInput(newPerson.get());

            if (!newPerson.isPresent()) break;
            try {
                modelManager.addPerson(newPerson.get());
                break;
            } catch (DuplicatePersonException e) {
                mainController.showAlertDialogAndWait(AlertType.WARNING, "Warning",
                        "Cannot have duplicate person", e.toString());
            }
        }
    }

    /**
     * Called when the user clicks the edit button. Opens a dialog to edit
     * details for the selected person.
     */
    @FXML
    private void handleEditPerson() {
        Person selected = personList.getSelectionModel().getSelectedItem();
        if (selected == null) { // no selection
            mainController.showAlertDialogAndWait(AlertType.WARNING, "No Selection",
                "No Person Selected", "Please select a person in the list.");
            return;
        }

        Optional<Person> updated = Optional.of(new Person(selected));
        while (true) { // keep re-asking until user provides valid input or cancels operation.
            updated = mainController.getPersonDataInput(updated.get());
            if (!updated.isPresent()) break;
            try {
                modelManager.updatePerson(selected, updated.get());
                break;
            } catch (DuplicatePersonException e) {
                mainController.showAlertDialogAndWait(AlertType.WARNING, "Warning", "Cannot have duplicate person",
                                                      e.toString());
            }
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

    private ContextMenu createContextMenu(){
        final ContextMenu contextMenu = new ContextMenu();

        MenuItem editMenuItem = new MenuItem("Edit");
        editMenuItem.setOnAction(e -> handleEditPerson());
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(e -> handleDeletePerson());
        contextMenu.getItems().addAll(editMenuItem, deleteMenuItem);
        return contextMenu;
    }
}
