package address.controller;

import address.model.*;
import address.events.EventManager;
import address.events.TagSearchResultsChangedEvent;
import address.events.TagsChangedEvent;
import address.model.datatypes.Person;
import address.model.datatypes.SelectableTag;
import address.model.datatypes.Tag;
import address.util.DateTimeUtil;

import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
    private ScrollPane tagList;
    @FXML
    private TextField tagSearch;
    @FXML
    private ScrollPane tagResults;
    @FXML
    private TextField githubUserNameField;

    private PersonEditDialogTagsModel model;
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
        tagSearch.textProperty().addListener((observableValue, oldValue, newValue) -> {
                handleTagInput(newValue);
            });
        tagSearch.setOnKeyTyped(e -> {
                switch (e.getCharacter()) {
                case " ":
                    e.consume();
                    model.toggleSelection();
                    tagSearch.clear();
                    break;
                default:
                    break;
                }
            });
        tagSearch.setOnKeyPressed(e -> {
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
        postalCodeField.setText(person.getPostalCode());
        cityField.setText(person.getCity());
        birthdayField.setText(person.birthdayString());
        birthdayField.setPromptText("dd.mm.yyyy");
        githubUserNameField.setText(person.getGithubUserName());
    }

    public void setTagsModel(List<Tag> tags, List<Tag> assignedTags) {
        model = new PersonEditDialogTagsModel(tags, assignedTags);
    }

    /**
     * Called when the user clicks ok.
     * Stores input as a Person object into finalData and isOkClicked flag to true
     */
    @FXML
    protected void handleOk() {
        if (!isInputValid()) return;
        finalPerson = new Person();
        finalPerson.setFirstName(firstNameField.getText());
        finalPerson.setLastName(lastNameField.getText());
        finalPerson.setStreet(streetField.getText());
        finalPerson.setPostalCode(postalCodeField.getText());
        finalPerson.setCity(cityField.getText());
        finalPerson.setBirthday(DateTimeUtil.parse(birthdayField.getText()));
        finalPerson.setTags(model.getAssignedTagss());
        finalPerson.setGithubUserName(githubUserNameField.getText());
        isOkClicked = true;
        dialogStage.close();
    }

    public Person getFinalInput() {
        return finalPerson;
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

        if (!isFilled(firstNameField)) {
            errorMessage += "First name must be filled!\n";
        }
        if (!isFilled(lastNameField)) {
            errorMessage += "Last name must be filled!\n";
        }

        if (isFilled(birthdayField) && birthdayField.getText().length() != 0
                && !DateTimeUtil.validDate(birthdayField.getText())) {
            errorMessage += "No valid birthday. Use the format dd.mm.yyyy!\n";
        }

        if (isFilled(githubUserNameField)) {
            try {
                URL url = new URL("https://www.github.com/" + githubUserNameField.getText());
            } catch (MalformedURLException e) {
                errorMessage += "Invalid github username.\n";
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

    private boolean isFilled(TextField textField) {
        return textField.getText() != null && textField.getText().length() != 0;
    }

    @Subscribe
    public void handleTagSearchResultsChangedEvent(TagSearchResultsChangedEvent e) {
        tagResults.setContent(getTagsVBox(e.getSelectableTags(), true));
    }

    @Subscribe
    public void handleTagsChangedEvent(TagsChangedEvent e) {
        tagList.setContent(getTagsVBox(e.getResultTag(), false));
    }

    private VBox getTagsVBox(List<SelectableTag> contactTagList, boolean isSelectable) {
        VBox content = new VBox();
        contactTagList.stream()
                .forEach(contactTag -> {
                        Label newLabel = new Label(contactTag.getName());
                        if (isSelectable && contactTag.isSelected()) {
                            newLabel.setStyle("-fx-background-color: blue;");
                        }
                        newLabel.setPrefWidth(261);
                        content.getChildren().add(newLabel);
                    });

        return content;
    }

    @FXML
    protected void handleTagInput(String newTags) {
        model.setFilter(newTags);
    }


}
