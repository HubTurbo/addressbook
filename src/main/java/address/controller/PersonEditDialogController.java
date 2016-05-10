package address.controller;

import address.model.ModelManager;
import address.events.EventManager;
import address.events.GroupSearchResultsChangedEvent;
import address.events.GroupsChangedEvent;
import address.model.ContactGroup;
import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import address.model.Person;
import address.util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Dialog to edit details of a person.
 */
public class PersonEditDialogController {
    public class SelectableContactGroup extends ContactGroup {
        private boolean isSelected = false;

        private SelectableContactGroup(ContactGroup contactGroup) {
            super(contactGroup.getName());
        }

        private void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        private boolean isSelected() {
            return this.isSelected;
        }
    }
    private class PersonEditDialogModel {
        List<SelectableContactGroup> groups = new ArrayList<>();
        List<SelectableContactGroup> filteredGroups = new ArrayList<>();
        Optional<Integer> selectedGroupIndex = Optional.empty();

        List<SelectableContactGroup> assignedGroups = new ArrayList<>();

        private PersonEditDialogModel(List<ContactGroup> groups, List<ContactGroup> assignedGroups) {
            List<SelectableContactGroup> selectableContactGroups = groups.stream()
                    .map(SelectableContactGroup::new)
                    .collect(Collectors.toList());
            this.groups.addAll(selectableContactGroups);

            assignedGroups.stream()
                    .forEach(assignedGroup -> {
                        this.assignedGroups.addAll(this.groups.stream()
                                .filter(group -> group.getName().equals(assignedGroup.getName()))
                                .collect(Collectors.toList()));
                    });

            EventManager.getInstance().post(new GroupsChangedEvent(this.assignedGroups));
            setFilter("");
        }

        private Optional<SelectableContactGroup> getSelection() {
            return filteredGroups.stream()
                    .filter(SelectableContactGroup::isSelected)
                    .findFirst();
        }

        private void toggleSelection() {
            Optional<SelectableContactGroup> selection = getSelection();
            if (!selection.isPresent()) return;

            if (assignedGroups.contains(selection.get())) {
                assignedGroups.remove(selection.get());
            } else {
                assignedGroups.add(selection.get());
            }

            EventManager.getInstance().post(new GroupsChangedEvent(assignedGroups));
        }

        private void selectIndex(int index) {
            clearSelection();
            for (int i = 0; i < filteredGroups.size(); i++) {
                if (i == index) {
                    SelectableContactGroup group = filteredGroups.remove(i);
                    group.setSelected(true);
                    filteredGroups.add(i, group);
                }
            }
        }

        private void clearSelection() {
            for (int i = 0; i < filteredGroups.size(); i++) {
                if (filteredGroups.get(i).isSelected()) {
                    SelectableContactGroup group = filteredGroups.remove(i);
                    group.setSelected(false);
                    filteredGroups.add(i, group);
                }
            }
        }

        private void selectNext() {
            if (!canIncreaseIndex()) return;
            selectedGroupIndex = Optional.of(selectedGroupIndex.orElse(-1) + 1);
            updateSelection();
            EventManager.getInstance().post(new GroupSearchResultsChangedEvent(filteredGroups));
        }

        private void updateSelection() {
            if (selectedGroupIndex.isPresent()) {
                selectIndex(selectedGroupIndex.get());
            } else {
                clearSelection();
            }
        }

        private boolean canIncreaseIndex() {
            return !filteredGroups.isEmpty() && (!selectedGroupIndex.isPresent() || selectedGroupIndex.get() < filteredGroups.size() - 1);
        }

        private void selectPrevious() {
            if (!canDecreaseIndex()) return;
            selectedGroupIndex = Optional.of(selectedGroupIndex.get() - 1);
            updateSelection();
            EventManager.getInstance().post(new GroupSearchResultsChangedEvent(filteredGroups));
        }

        private boolean canDecreaseIndex() {
            return selectedGroupIndex.isPresent() && selectedGroupIndex.get() > 0;
        }

        private void setFilter(String filter) {

            List<SelectableContactGroup> newContactGroups = groups.stream()
                    .filter(group -> group.getName().contains(filter))
                    .collect(Collectors.toList());

            List<SelectableContactGroup> toBeAdded = newContactGroups.stream()
                    .filter(newContactGroup -> !filteredGroups.contains(newContactGroup))
                    .collect(Collectors.toList());
            List<SelectableContactGroup> toBeRemoved = filteredGroups.stream()
                    .filter(oldContactGroup -> !newContactGroups.contains(oldContactGroup))
                    .collect(Collectors.toList());

            toBeRemoved.stream()
                    .forEach(toRemove -> filteredGroups.remove(toRemove));
            toBeAdded.stream()
                    .forEach(toAdd -> filteredGroups.add(toAdd));

            if (!filter.isEmpty() && !filteredGroups.isEmpty()) {
                selectedGroupIndex = Optional.of(0);
            } else {
                selectedGroupIndex = Optional.empty();
            }

            updateSelection();

            EventManager.getInstance().post(new GroupSearchResultsChangedEvent(filteredGroups));
        }

        private List<ContactGroup> getAssignedGroups() {
            return assignedGroups.stream()
                    .map(assignedGroup -> new ContactGroup(assignedGroup.getName()))
                    .collect(Collectors.toList());
        }
    }

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

    private PersonEditDialogModel model;
    private Stage dialogStage;
    private Person person;
    private boolean okClicked = false;
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
     * Returns true if the user clicked OK, false otherwise.
     * 
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
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
            modelManager.updatePerson(person, updated);

            okClicked = true;
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
    private void handleCancel() {
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
}