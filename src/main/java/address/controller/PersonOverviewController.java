package address.controller;

import address.events.*;
import address.exceptions.DuplicatePersonException;
import address.model.ModelManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.parser.ParseException;
import address.parser.Parser;
import address.parser.expr.Expr;
import address.parser.expr.PredExpr;
import address.keybindings.KeyBindingsManager;
import address.status.PersonCreatedStatus;
import address.status.PersonDeletedStatus;
import address.status.PersonEditedStatus;
import address.ui.PersonListViewCell;
import address.util.OrderedList;
import address.util.AppLogger;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.Optional;
import java.util.concurrent.*;

public class PersonOverviewController {
    private static AppLogger logger = LoggerManager.getLogger(PersonOverviewController.class);

    @FXML
    private ListView<ReadOnlyViewablePerson> personListView;

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
        personListView.setContextMenu(createContextMenu());
    }

    public void setConnections(MainController mainController, ModelManager modelManager, OrderedList<ReadOnlyViewablePerson> orderedList) {
        this.mainController = mainController;
        this.modelManager = modelManager;

        // Add observable list data to the list
        personListView.setItems(orderedList);
        personListView.setCellFactory(listView -> new PersonListViewCell(orderedList));

        personListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    logger.debug("Person in list view clicked. Loading GitHub profile page: '{}'", newValue);
                    mainController.loadGithubProfilePage(newValue);
                }
            });
    }

    /**
     * Called when the user clicks on the delete button.
     */
    @FXML
    private void handleDeletePerson() {
        int selectedIndex = personListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            final ReadOnlyPerson deleteTarget = personListView.getItems().get(selectedIndex);
            mainController.getStatusBarHeaderController().postStatus(new PersonDeletedStatus(deleteTarget));

            modelManager.delayedDeletePerson(deleteTarget, 1, TimeUnit.SECONDS);
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
        Optional<ReadOnlyPerson> prevInputData = Optional.of(Person.createPersonDataContainer());
        while (true) { // keep re-asking until user provides valid input or cancels operation.
            prevInputData = mainController.getPersonDataInput(prevInputData.get());

            if (!prevInputData.isPresent()) break;
            try {
                modelManager.addPerson(new Person(prevInputData.get()));
                mainController.getStatusBarHeaderController().postStatus(new PersonCreatedStatus(prevInputData.get()));
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
        final ReadOnlyPerson editTarget = personListView.getSelectionModel().getSelectedItem();
        if (editTarget == null) { // no selection
            mainController.showAlertDialogAndWait(AlertType.WARNING, "No Selection",
                "No Person Selected", "Please select a person in the list.");
            return;
        }

        Optional<ReadOnlyPerson> prevInputData = Optional.of(new Person(editTarget));
        while (true) { // keep re-asking until user provides valid input or cancels operation.
            prevInputData = mainController.getPersonDataInput(prevInputData.get());
            if (!prevInputData.isPresent()) break;
            try {
                modelManager.updatePerson(editTarget, prevInputData.get());
                mainController.getStatusBarHeaderController().postStatus(
                        new PersonEditedStatus(new Person(editTarget), prevInputData.get()));
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
        editMenuItem.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("PERSON_EDIT_ACCELERATOR").get());
        editMenuItem.setOnAction(e -> handleEditPerson());
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("PERSON_DELETE_ACCELERATOR").get());
        deleteMenuItem.setOnAction(e -> handleDeletePerson());
        contextMenu.getItems().addAll(editMenuItem, deleteMenuItem);
        contextMenu.setId("personListContextMenu");
        return contextMenu;
    }

    @Subscribe
    private void handleJumpToListRequestEvent(JumpToListRequestEvent event) {
        Platform.runLater(() -> {
            jumpToListItem(event.targetIndex);
        });
    }

    /**
     * Jumps the Nth item of the list if it exists. No action if the Nth item does not exist.
     * Jumps to the bottom if {@code targetIndex = -1}
     *
     * @param targetIndex starts from 1. To jump to 1st item, targetIndex should be 1.
     *                    To jump to bottom, should be -1.
     */

    private void jumpToListItem(int targetIndex) {
        int listSize = personListView.getItems().size();
        if (listSize < targetIndex) {
            return;
        }
        int indexOfItem;
        if (targetIndex == -1){  // if the target is the bottom of the list
            indexOfItem = listSize - 1;
        } else {
            indexOfItem = targetIndex - 1; //to account for list indexes starting from 0
        }

        selectItem(indexOfItem);
    }

    /**
     * Selects the item in the list
     * @param indexOfItem
     */
    private void selectItem(int indexOfItem) {
        personListView.getSelectionModel().select(indexOfItem);
        personListView.getFocusModel().focus(indexOfItem);
        personListView.requestFocus();
    }
}
