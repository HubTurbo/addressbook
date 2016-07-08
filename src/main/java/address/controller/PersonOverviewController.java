package address.controller;

import address.events.*;
import address.model.ModelManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.parser.ParseException;
import address.parser.Parser;
import address.parser.expr.Expr;
import address.parser.expr.PredExpr;
import address.keybindings.KeyBindingsManager;
import address.parser.qualifier.TrueQualifier;
import address.status.PersonDeletedStatus;
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

import java.util.List;
import java.util.Objects;

/**
 * Dialog to view the list of persons and their details
 *
 * setConnections should be set before showing stage
 */
public class PersonOverviewController extends UiController{
    private static AppLogger logger = LoggerManager.getLogger(PersonOverviewController.class);

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

    /**
     * When the user selected multiple item in the listview. The edit feature will be
     * disabled. Features related to edit will be bind to this property.
     */
    private BooleanProperty isEditDisabled = new SimpleBooleanProperty(false);

    private ListChangeListener<Integer> multipleSelectListener = c -> {
        if (c.getList().size() > 1) {
            isEditDisabled.set(true);
        } else {
            isEditDisabled.set(false);
        }
    };

    public PersonOverviewController() {
        super();
        parser = new Parser();
    }

    @Subscribe
    private void handleFilterCommittedEvent(FilterCommittedEvent fce) {
        filteredPersonList.setPredicate(fce.filterExpression::satisfies);
    }

    public void setConnections(MainController mainController, ModelManager modelManager,
                               ObservableList<ReadOnlyViewablePerson> personList) {
        this.mainController = mainController;
        this.modelManager = modelManager;
        filteredPersonList = new FilteredList<>(personList, new PredExpr(new TrueQualifier())::satisfies);

        ReorderedList<ReadOnlyViewablePerson> orderedList = new ReorderedList<>(filteredPersonList);
        personListView.setItems(orderedList);
        personListView.setCellFactory(listView -> new PersonListViewCell(orderedList));
        personListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    logger.debug("Person in list view clicked. Loading GitHub profile page: '{}'", newValue);
                    mainController.loadGithubProfilePage(newValue);
                }
            });
        personListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        personListView.getSelectionModel().getSelectedIndices().addListener(multipleSelectListener);
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        personListView.setContextMenu(createContextMenu());
        editButton.disableProperty().bind(isEditDisabled);
    }

    /**
     * Called when the user clicks on the delete button.
     */
    @FXML
    private void handleDeletePersons() {
        final List<ReadOnlyViewablePerson> selected = personListView.getSelectionModel().getSelectedItems();
        
        if (!isSelectionValid()) {
            showInvalidSelectionAlert();
        } else {
            selected.stream()
                    .forEach(target -> modelManager.deletePersonThroughUI(target));
        }
    }

    private boolean isSelectionValid() {
        final List<?> selected = personListView.getSelectionModel().getSelectedItems();
        return !selected.isEmpty() && !selected.stream().anyMatch(Objects::isNull);
    }

    /**
     * Called when the user clicks the new button. Opens a dialog to edit
     * details for a new person.
     */
    @FXML
    private void handleNewPerson() {
        modelManager.createPersonThroughUI(() ->
                mainController.getPersonDataInput(Person.createPersonDataContainer(), "New Person"));
    }

    /**
     * Called when the context menu edit is clicked.
     */
    private void handleRetagPersons() {
        List<ReadOnlyViewablePerson> selectedPersons = personListView.getSelectionModel().getSelectedItems();
        if (!isSelectionValid()) {
            showInvalidSelectionAlert();
            return;
        }
        modelManager.retagPersonsThroughUI(selectedPersons, () -> mainController.getPersonsTagsInput(selectedPersons));
    }

    /**
     * Called when the user clicks the edit button. Opens a dialog to edit
     * details for the selected person.
     */
    @FXML
    private void handleEditPerson() {
        final ReadOnlyPerson editTarget = personListView.getSelectionModel().getSelectedItem();
        if (editTarget == null) { // no selection
            showInvalidSelectionAlert();
            return;
        }
        modelManager.editPersonThroughUI(editTarget,
                () -> mainController.getPersonDataInput(editTarget, "Edit Person"));
    }

    private void handleCancelPersonOperations() {
        final List<ReadOnlyViewablePerson> selectedPersons = personListView.getSelectionModel().getSelectedItems();
        if (!isSelectionValid()) {
            showInvalidSelectionAlert();
            return;
        }
        selectedPersons.stream().forEach(selectedPerson -> {
                modelManager.cancelPersonChangeCommand(selectedPerson);
        });
    }

    @FXML
    private void handleFilterChanged() {
        Expr filterExpression;
        try {
            filterExpression = parser.parse(filterField.getText());
        } catch (ParseException e) {
            logger.debug("Invalid filter found: {}", e);
            filterExpression = PredExpr.TRUE;
        }

        if (filterExpression != null) {
            if (filterField.getStyleClass().contains("error")) filterField.getStyleClass().remove("error");
        } else {
            if (!filterField.getStyleClass().contains("error")) filterField.getStyleClass().add("error");
        }
        raise(new FilterCommittedEvent(filterExpression));
    }

    private ContextMenu createContextMenu() {
        final ContextMenu contextMenu = new ContextMenu();

        final MenuItem editMenuItem = new MenuItem("Edit");
        editMenuItem.disableProperty().bind(isEditDisabled);
        editMenuItem.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("PERSON_EDIT_ACCELERATOR").get());
        editMenuItem.setOnAction(e -> handleEditPerson());

        final MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("PERSON_DELETE_ACCELERATOR").get());
        deleteMenuItem.setOnAction(e -> handleDeletePersons());

        final MenuItem tagMenuItem = new MenuItem("Tag");
        tagMenuItem.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("PERSON_TAG_ACCELERATOR").get());
        tagMenuItem.setOnAction(e -> handleRetagPersons());

        final MenuItem cancelOperationMenuItem = new MenuItem("Cancel");
        cancelOperationMenuItem.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("PERSON_CHANGE_CANCEL_ACCELERATOR").get());
        cancelOperationMenuItem.setOnAction(e -> handleCancelPersonOperations());

        contextMenu.getItems().addAll(editMenuItem, deleteMenuItem, tagMenuItem, cancelOperationMenuItem);
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
        if (targetIndex == -1) {  // if the target is the bottom of the list
            indexOfItem = listSize - 1;
        } else {
            indexOfItem = targetIndex - 1; //to account for list indexes starting from 0
        }

        selectItem(indexOfItem);
    }

    private void showInvalidSelectionAlert() {
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
