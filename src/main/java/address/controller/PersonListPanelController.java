package address.controller;

import static address.keybindings.KeyBindingsManager.*;
import static address.model.datatypes.person.ReadOnlyViewablePerson.ongoingCommandState.*;

import address.events.controller.JumpToListRequestEvent;
import address.events.parser.FilterCommittedEvent;
import address.model.ModelManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.parser.ParseException;
import address.parser.Parser;
import address.parser.expr.Expr;
import address.parser.expr.PredExpr;
import address.parser.qualifier.TrueQualifier;
import address.ui.PersonListViewCell;
import address.util.collections.FilteredList;
import address.util.AppLogger;
import address.util.LoggerManager;
import address.util.collections.ReorderedList;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCombination;

import java.util.List;
import java.util.Objects;

/**
 * Dialog to view the list of persons and their details
 *
 * setConnections should be set before showing stage
 */
public class PersonListPanelController extends UiController {
    private static AppLogger logger = LoggerManager.getLogger(PersonListPanelController.class);
    private final BooleanProperty shouldDisableEdit = new SimpleBooleanProperty(false);
    private final BooleanProperty shouldAllowRetry = new SimpleBooleanProperty(false);

    @FXML
    private Button newButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private ListView<ReadOnlyViewablePerson> personListView;
    @FXML
    private TextField filterField;

    private MainController mainController;
    private ModelManager modelManager;
    private FilteredList<ReadOnlyViewablePerson> filteredPersonList;
    private Parser parser;

    public PersonListPanelController() {
        super();
        parser = new Parser();
    }

    public void setConnections(MainController mainController, ModelManager modelManager,
                               ObservableList<ReadOnlyViewablePerson> personList) {
        this.mainController = mainController;
        this.modelManager = modelManager;
        filteredPersonList = new FilteredList<>(personList, new PredExpr(new TrueQualifier())::satisfies);

        ReorderedList<ReadOnlyViewablePerson> orderedList = new ReorderedList<>(filteredPersonList);
        personListView.setItems(orderedList);
        personListView.setCellFactory(listView -> new PersonListViewCell(orderedList));
        loadGithubProfilePageWhenPersonIsSelected(mainController);
        setupListviewSelectionModelSettings();
        disableEditCommandForMultipleSelection();
        enableRetryCommandOnlyIfSelectionContainsFailedRequests();
    }

    private void setupListviewSelectionModelSettings() {
        personListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        personListView.getItems().addListener((ListChangeListener<ReadOnlyViewablePerson>) c -> {
            while(c.next()) {
                if (c.wasRemoved()) {
                    ObservableList<Integer> currentIndices = personListView.getSelectionModel().getSelectedIndices();
                    if (currentIndices.size() > 1) {
                        personListView.getSelectionModel().clearAndSelect(currentIndices.get(0));
                    }
                }
            }
        });
    }
    
    @Subscribe
    private void handleFilterCommittedEvent(FilterCommittedEvent fce) {
        filteredPersonList.setPredicate(fce.filterExpression::satisfies);
    }

    private void loadGithubProfilePageWhenPersonIsSelected(MainController mainController) {
        personListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                logger.debug("Person in list view clicked. Loading GitHub profile page: '{}'", newValue);
                mainController.loadGithubProfilePage(newValue);
            }
        });
    }

    private void disableEditCommandForMultipleSelection() {
        final ListChangeListener<Integer> listener = change -> shouldDisableEdit.set(change.getList().size() > 1);
        personListView.getSelectionModel().getSelectedIndices().addListener(listener);
    }

    private void enableRetryCommandOnlyIfSelectionContainsFailedRequests() {
        final ListChangeListener<ReadOnlyViewablePerson> listener = change ->  shouldAllowRetry.set(
                change.getList().stream().anyMatch(p -> p.getOngoingCommandState() == REQUEST_FAILED));
        personListView.getSelectionModel().getSelectedItems().addListener(listener);
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        personListView.setContextMenu(createContextMenu());
        editButton.disableProperty().bind(shouldDisableEdit);
    }

    /**
     * Informs user if selection is invalid
     * @return true if selection is valid, false otherwise
     */
    private boolean checkAndHandleInvalidSelection() {
        final List<?> selected = personListView.getSelectionModel().getSelectedItems();
        if (selected.isEmpty() || selected.stream().anyMatch(Objects::isNull)) {
            showNoValidSelectionAlert();
            return false;
        }
        return true;
    }

    /**
     * Opens a dialog to edit details for a new person.
     */
    @FXML
    private void handleNewPerson() {
        modelManager.createPersonThroughUI(() ->
                mainController.getPersonDataInput(Person.createPersonDataContainer(), "New Person"));
    }

    /**
     * Opens a dialog to edit details for the selected person.
     */
    @FXML
    private void handleEditPerson() {
        if (checkAndHandleInvalidSelection()) {
            final ReadOnlyPerson editTarget = personListView.getSelectionModel().getSelectedItem();
            modelManager.editPersonThroughUI(editTarget,
                    () -> mainController.getPersonDataInput(editTarget, "Edit Person"));
        }
    }

    /**
     * Deletes selected persons
     */
    @FXML
    private void handleDeletePersons() {
        if (checkAndHandleInvalidSelection()) {
            final List<ReadOnlyViewablePerson> selected = personListView.getSelectionModel().getSelectedItems();
            selected.forEach(modelManager::deletePersonThroughUI);
        }
    }

    /**
     * Retags all selected persons
     */
    private void handleRetagPersons() {
        if (checkAndHandleInvalidSelection()) {
            final List<ReadOnlyViewablePerson> selected = personListView.getSelectionModel().getSelectedItems();
            modelManager.retagPersonsThroughUI(selected, () -> mainController.getPersonsTagsInput(selected));
        }
    }

    /**
     * Cancels all ongoing commands for selected persons
     */
    private void handleCancelCommands() {
        if (checkAndHandleInvalidSelection()) {
            final List<ReadOnlyViewablePerson> selected = personListView.getSelectionModel().getSelectedItems();
            selected.forEach(modelManager::cancelPersonCommand);
        }
    }

    /**
     * Retries all currently failed commands for selected persons
     */
    private void handleRetryFailedCommands() {
        if (checkAndHandleInvalidSelection()) {
            final List<ReadOnlyViewablePerson> selected = personListView.getSelectionModel().getSelectedItems();
            selected.forEach(modelManager::retryFailedPersonCommand);
        }
    }

    @FXML
    private void handleFilterChanged() {
        Expr filterExpression;
        try {
            filterExpression = parser.parse(filterField.getText());
            if (filterField.getStyleClass().contains("error")) filterField.getStyleClass().remove("error");
        } catch (ParseException e) {
            logger.debug("Invalid filter found: {}", e);
            filterExpression = PredExpr.TRUE;
            if (!filterField.getStyleClass().contains("error")) filterField.getStyleClass().add("error");
        }

        raise(new FilterCommittedEvent(filterExpression));
    }

    private ContextMenu createContextMenu() {
        logger.debug("Creating context menu for listview card");
        final ContextMenu contextMenu = new ContextMenu();

        final MenuItem editMenuItem = initContextMenuItem("Edit",
                getAcceleratorKeyCombo("PERSON_EDIT_ACCELERATOR").get(), this::handleEditPerson);
        editMenuItem.setId(generateMenuItemId("edit"));
        editMenuItem.disableProperty().bind(shouldDisableEdit); // disable if multiple selected

        final MenuItem retryFailedMenuItem = initContextMenuItem("Retry",
                getAcceleratorKeyCombo("PERSON_RETRY_FAILED_COMMAND_ACCELERATOR").get(),
                this::handleRetryFailedCommands);
        retryFailedMenuItem.setId(generateMenuItemId("retryFailed"));
        retryFailedMenuItem.visibleProperty().bind(shouldAllowRetry);

        contextMenu.getItems().addAll(
                editMenuItem,
                initContextMenuItem("Delete",
                        getAcceleratorKeyCombo("PERSON_DELETE_ACCELERATOR").get(), this::handleDeletePersons),
                initContextMenuItem("Tag",
                        getAcceleratorKeyCombo("PERSON_TAG_ACCELERATOR").get(), this::handleRetagPersons),
                initContextMenuItem("Cancel",
                        getAcceleratorKeyCombo("PERSON_CANCEL_COMMAND_ACCELERATOR").get(), this::handleCancelCommands),
                retryFailedMenuItem
        );
        contextMenu.setId("personListContextMenu");
        logger.debug("Context menu for listview card created: " + contextMenu.toString());
        return contextMenu;
    }

    private MenuItem initContextMenuItem(String name, KeyCombination accel, Runnable action) {
        final MenuItem menuItem = new MenuItem(name);
        menuItem.setId(generateMenuItemId(name));
        menuItem.setAccelerator(accel);
        menuItem.setOnAction(e -> action.run());
        return menuItem;
    }

    private String generateMenuItemId(String menuItemName) {
        return menuItemName.toLowerCase() + "MenuItem";
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
        if (targetIndex == -1) {  // if the target is the bottom of the list
            indexOfItem = listSize - 1;
        } else {
            indexOfItem = targetIndex - 1; //to account for list indexes starting from 0
        }

        selectItem(indexOfItem);
    }

    private void showNoValidSelectionAlert() {
        mainController.showAlertDialogAndWait(AlertType.WARNING,
                "Invalid Selection", "No Person Selected", "Please select a person in the list.");
    }

    /**
     * Selects the item in the list and scrolls to it if it is out of view.
     * @param indexOfItem
     */
    private void selectItem(int indexOfItem) {
        personListView.getSelectionModel().clearAndSelect(indexOfItem);
        personListView.getFocusModel().focus(indexOfItem);
        personListView.requestFocus();
        personListView.scrollTo(indexOfItem);
    }
}
