package address.controller;

import address.MainApp;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import address.util.AppLogger;
import address.util.DateTimeUtil;

import address.util.LoggerManager;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Dialog to edit details of a person.
 */
public class PersonEditDialogController extends EditDialogController {
    private static final AppLogger logger = LoggerManager.getLogger(PersonEditDialogController.class);
    public static final String FXML_TAG_SELECTION_EDIT_DIALOG = "/view/TagSelectionEditDialog.fxml";

    @FXML
    private AnchorPane mainPane;
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
    private TextField githubUserNameField;

    private List<Tag> finalAssignedTags;

    private Person finalPerson;
    private List<Tag> fullTagList;
    private List<Tag> initialAssignedTags;


    public PersonEditDialogController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        addListeners();
        Platform.runLater(() -> firstNameField.requestFocus());
    }

    private void addListeners() {
        tagList.setOnMouseClicked(e -> launchTagSelectionEditDialog());
        mainPane.setOnKeyPressed(e -> {
            if (e.isShortcutDown() && e.getCode() == KeyCode.O) {
                e.consume();
                launchTagSelectionEditDialog();
            }
        });
    }

    private void launchTagSelectionEditDialog() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource(FXML_TAG_SELECTION_EDIT_DIALOG));
        try {
            AnchorPane pane = loader.load();


            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(this.dialogStage);
            dialogStage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(pane, Color.TRANSPARENT);
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    dialogStage.close();
                }
            });
            dialogStage.setScene(scene);

            TagSelectionEditDialogController controller = loader.getController();
            controller.setTags(fullTagList, initialAssignedTags);
            controller.setDialogStage(dialogStage);


            dialogStage.showAndWait();

            if (controller.isOkClicked()) finalAssignedTags = controller.getFinalAssignedTags();
            tagList.setContent(getTagsVBox(finalAssignedTags));
        } catch (IOException e1) {
            logger.warn("Error launching tag selection dialog.");
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("FXML Load Error");
            alert.setHeaderText("Cannot load dialog for tag selection dialog");
            alert.setContentText("IOException when trying to load " + FXML_TAG_SELECTION_EDIT_DIALOG);

            alert.showAndWait();
        }
    }

    /**
     * Sets the initial placeholder data in the dialog fields
     */
    public void setInitialPersonData(ReadOnlyPerson person) {
        firstNameField.setText(person.getFirstName());
        lastNameField.setText(person.getLastName());
        streetField.setText(person.getStreet());
        postalCodeField.setText(person.getPostalCode());
        cityField.setText(person.getCity());
        birthdayField.setText(person.birthdayString());
        birthdayField.setPromptText("dd.mm.yyyy");
        githubUserNameField.setText(person.getGithubUserName());
    }

    public void setTags(List<Tag> tags, List<Tag> assignedTags) {
        this.fullTagList = tags;
        this.initialAssignedTags = assignedTags;
        this.finalAssignedTags = assignedTags;
        tagList.setContent(getTagsVBox(assignedTags));
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
        finalPerson.setTags(finalAssignedTags);
        finalPerson.setGithubUserName(githubUserNameField.getText());
        isOkClicked = true;
        dialogStage.close();
    }

    private VBox getTagsVBox(List<Tag> contactTagList) {
        VBox content = new VBox();
        contactTagList.stream()
                .forEach(contactTag -> {
                    Label newLabel = new Label(contactTag.getName());
                    newLabel.setPrefWidth(261);
                    content.getChildren().add(newLabel);
                });

        return content;
    }

    public Person getFinalInput() {
        return finalPerson;
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

}
