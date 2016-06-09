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
import address.shortcuts.ShortcutsManager;
import address.status.PersonCreatedStatus;
import address.status.PersonDeletedStatus;
import address.status.PersonEditedStatus;
import address.ui.PersonListViewCell;
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

    @FXML
    private ListView<ReadOnlyViewablePerson> personListView;

    @FXML
    private TextField filterField;

    private MainController mainController;
    private ModelManager modelManager;

    private final ScheduledExecutorService requestExecutor = Executors.newScheduledThreadPool(1);

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

    public void setConnections(MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;

        // Add observable list data to the list
        personListView.setItems(modelManager.getAllViewablePersonsReadOnly());
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
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
            ReadOnlyViewablePerson target = personListView.getItems().get(selectedIndex);
            mainController.getStatusBarHeaderController().postStatus(new PersonDeletedStatus(target));

            personListView.getItems().get(selectedIndex).isDeletedProperty().set(true);
            requestExecutor.schedule(()
                    -> Platform.runLater(() -> modelManager.deletePerson(target)), 3, TimeUnit.SECONDS);
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
        Optional<ReadOnlyPerson> prevInput = Optional.of(new Person());
        while (true) { // keep re-asking until user provides valid input or cancels operation.
            prevInput = mainController.getPersonDataInput(prevInput.get());

            if (!prevInput.isPresent()) break;
            try {
                modelManager.addPerson(new Person(prevInput.get()));
                mainController.getStatusBarHeaderController().postStatus(new PersonCreatedStatus(prevInput.get()));
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
        ReadOnlyPerson target = personListView.getSelectionModel().getSelectedItem();
        if (target == null) { // no selection
            mainController.showAlertDialogAndWait(AlertType.WARNING, "No Selection",
                "No Person Selected", "Please select a person in the list.");
            return;
        }

        Optional<ReadOnlyPerson> prevInput = Optional.of(new Person(target));
        while (true) { // keep re-asking until user provides valid input or cancels operation.
            prevInput = mainController.getPersonDataInput(prevInput.get());
            if (!prevInput.isPresent()) break;
            try {
                modelManager.updatePerson(target, prevInput.get());
                mainController.getStatusBarHeaderController().postStatus(new PersonEditedStatus(new Person(target),
                        prevInput.get()));
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
        editMenuItem.setAccelerator(ShortcutsManager.SHORTCUT_PERSON_EDIT);
        editMenuItem.setOnAction(e -> handleEditPerson());
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setAccelerator(ShortcutsManager.SHORTCUT_PERSON_DELETE);
        deleteMenuItem.setOnAction(e -> handleDeletePerson());
        contextMenu.getItems().addAll(editMenuItem, deleteMenuItem);
        contextMenu.setId("personListContextMenu");
        return contextMenu;
    }

    @Subscribe
    private void handleJumpToListRequestEvent(JumpToListRequestEvent event) {
        jumpToList(event.targetIndex);
    }

    /**
     * Jumps the Nth item of the list if it exists. No action if the Nth item does not exist.
     *
     * @param targetIndex starts from 1. To jump to 1st item, targetIndex should be 1.
     */
    private void jumpToList(int targetIndex) {
        Platform.runLater(() -> {
                if (personListView.getItems().size() < targetIndex) {
                    return;
                }
                int indexOfItem = targetIndex - 1; //to account for list indexes starting from 0
                personListView.getSelectionModel().select(indexOfItem);
                personListView.getFocusModel().focus(indexOfItem);
                personListView.requestFocus();
            });
    }
}
