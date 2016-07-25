package address.controller;

import address.model.ModelManager;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import address.util.AppLogger;
import address.util.LoggerManager;
import commons.DateTimeUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Dialog to edit details of a person.
 *
 * Stage, initial person and available & assigned tags should be set before showing stage
 */
public class PersonEditDialog extends BaseUiPart {
    private static final AppLogger logger = LoggerManager.getLogger(PersonEditDialog.class);
    private static final String ICON = "/images/edit.png";
    public static final String TITLE = "Edit Person";
    public static final String FXML = "PersonEditDialog.fxml";
    private static final String TOOLTIP_TAG_SELECTOR_SHORTCUT = "Shortcut + O";
    private static final String TOOLTIP_LAUNCH_TAG_SELECTOR = "Click to launch tag selector";
    AnchorPane pane;
    Stage dialogStage;
    private boolean isOkClicked = false;

    @FXML
    private Label idLabel;
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

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        addListeners();
        modifyInteractionsWithComponents();
        Platform.runLater(() -> firstNameField.requestFocus());
    }


    @Override
    public void setNode(Node node) {
        pane = (AnchorPane)node;
    }

    @Override
    public String getFxmlPath() {
        return FXML;
    }

    public static PersonEditDialog load(Stage primaryStage, ReadOnlyPerson initialData, ModelManager modelManager) {
        logger.debug("Loading dialog for person edit.");
        PersonEditDialog editDialog = UiPartLoader.loadUiPart(primaryStage, new PersonEditDialog());
        editDialog.configure(initialData, modelManager.getTagsAsReadOnlyObservableList());
        return editDialog;
    }

    private void configure(ReadOnlyPerson initialData, List<Tag> tags) {
        Scene scene = new Scene(pane);
        dialogStage = createDialogStage(TITLE, primaryStage, scene);
        setIcon(dialogStage, ICON);
        setEscKeyToDismiss(scene);
        setInitialPersonData(initialData);
        setTags(tags, new ArrayList<>(initialData.getObservableTagList()));
    }

    private void setEscKeyToDismiss(Scene scene) { //TODO: move to a new parent class BaseDialogUiPart
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                dialogStage.close();
            }
        });
    }

    public void showAndWait(){
        dialogStage.showAndWait();
    }


    public Optional<ReadOnlyPerson> getUserInput() {
        showAndWait();
        if (isOkClicked()) {
            logger.debug("Person collected: " + getEditedPerson().toString());
            return Optional.of(getEditedPerson());
        } else {
            return Optional.empty();
        }
    }

    public void close() {
        dialogStage.close();
    }

    private void modifyInteractionsWithComponents() {
        tagList.setFocusTraversable(true);
        tagList.setTooltip(getTooltip());
    }

    private void addListeners() {
        tagList.setOnMouseClicked(e -> performTagSelectionEditDialog());
        tagList.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                e.consume();
                performTagSelectionEditDialog();
            }
        });
        mainPane.setOnKeyPressed(e -> {
            if (e.isShortcutDown() && e.getCode() == KeyCode.O) {
                e.consume();
                performTagSelectionEditDialog();
            }
        });
    }

    private Tooltip getTooltip() {
        Tooltip tooltip = new Tooltip(TOOLTIP_TAG_SELECTOR_SHORTCUT);
        tooltip.setGraphic(getTooltipLabel());
        tooltip.setContentDisplay(ContentDisplay.TOP);
        return tooltip;
    }

    private Label getTooltipLabel() {
        Label label = new Label(TOOLTIP_LAUNCH_TAG_SELECTOR);
        label.getStyleClass().add("tooltip-text");
        return label;
    }

    private void performTagSelectionEditDialog() {
        TagSelectionEditDialog tagEditDialog = TagSelectionEditDialog.load(dialogStage, fullTagList, finalAssignedTags);
        tagEditDialog.showAndWait();

        if (tagEditDialog.isOkClicked()) finalAssignedTags = tagEditDialog.getFinalAssignedTags();
        tagList.setContent(getTagsVBox(finalAssignedTags));

    }

    public void setTags(List<Tag> tags, List<Tag> assignedTags) {
        this.fullTagList = tags;
        this.finalAssignedTags = assignedTags;
        tagList.setContent(getTagsVBox(assignedTags));
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

    /**
     * Sets the initial placeholder data in the dialog fields
     */
    public void setInitialPersonData(ReadOnlyPerson person) {
        idLabel.setText("Person " + person.idString());
        firstNameField.setText(person.getFirstName());
        lastNameField.setText(person.getLastName());
        streetField.setText(person.getStreet());
        postalCodeField.setText(person.getPostalCode());
        cityField.setText(person.getCity());
        birthdayField.setText(person.birthdayString());
        birthdayField.setPromptText("dd.mm.yyyy");
        githubUserNameField.setText(person.getGithubUsername());
    }

    /**
     * Called when the user clicks ok.
     * Stores input as a Person object into finalData and isOkClicked flag to true
     */
    @FXML
    protected void handleOk() {

        Optional<String> invalidityInfo = getInvalidityInfo();
        if (invalidityInfo.isPresent()) {
            showInvalidInputMessage(invalidityInfo.get());
            return;
        }

        finalPerson = Person.createPersonDataContainer();
        finalPerson.setFirstName(firstNameField.getText());
        finalPerson.setLastName(lastNameField.getText());
        finalPerson.setStreet(streetField.getText());
        finalPerson.setPostalCode(postalCodeField.getText());
        finalPerson.setCity(cityField.getText());
        finalPerson.setBirthday(DateTimeUtil.parse(birthdayField.getText()));
        finalPerson.setTags(finalAssignedTags);
        finalPerson.setGithubUsername(githubUserNameField.getText());
        isOkClicked = true;
        close();
    }

    private void showInvalidInputMessage(String errorMessage) {
        // Show the error message.
        Alert alert = new Alert(AlertType.ERROR);
        alert.initOwner(dialogStage);
        alert.setTitle("Invalid Fields");
        alert.setHeaderText("Please correct invalid fields");
        alert.setContentText(errorMessage);

        alert.showAndWait();
    }


    public Person getEditedPerson() {
        return finalPerson;
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    protected void handleCancel() {
        close();
    }

    /**
     * Returns a string describing invalid data.
     * Returns Optional.empty() if all data are valid.
     */
    private Optional<String> getInvalidityInfo() {
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
                new URL("https://www.github.com/" + githubUserNameField.getText());
            } catch (MalformedURLException e) {
                errorMessage += "Invalid github username.\n";
            }
        }

        return errorMessage.length() == 0 ? Optional.empty(): Optional.of(errorMessage);
    }

    private boolean isFilled(TextField textField) {
        return textField.getText() != null && textField.getText().length() != 0;
    }

    public boolean isOkClicked() {
        return isOkClicked;
    }


}