package address.sync.cloud;

import address.exceptions.DataConversionException;
import address.sync.cloud.model.CloudAddressBook;
import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;
import address.util.AppLogger;
import address.util.Config;
import address.util.LoggerManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Adds data/response manipulation features to the CloudSimulator
 *
 * Launches a GUI for the tester to simulate errors and data modifications
 * By default, the responses returned should be same as the ones returned from CloudSimulator, with little to no delay
 *
 * The CloudManipulator will attempt to initialize a cloud file with cloud address book data.
 * This data can be provided through the following means:
 *  - A cloud address book can be provided for initialization.
 *  - A cloud data file path can be provided in config. If reading fails, it will initialize an empty cloud file
 *  with the given address book name in config.
 */
public class CloudManipulator extends CloudSimulator {
    private static final AppLogger logger = LoggerManager.getLogger(CloudManipulator.class);
    private static final String DELAY_BUTTON_TEXT = "Delay next response";
    private static final String FAIL_BUTTON_TEXT = "Fail next response";
    private static final String DELAY_BUTTON_ICON_PATH = "/images/clock.png";
    private static final String FAIL_BUTTON_ICON_PATH = "/images/fail.png";
    private static final String ADD_PERSON_TEXT = "Add person";
    private static final String MODIFY_PERSON_TEXT = "Modify person";
    private static final String DELETE_PERSON_TEXT = "Delete person";
    private static final String ADD_TAG_TEXT = "Add tag";
    private static final String MODIFY_TAG_TEXT = "Modify tag";
    private static final String DELETE_TAG_TEXT = "Delete tag";
    private static final String CLOUD_MANIPULATOR_TITLE = "Cloud Manipulator";
    private static final String ADDRESS_BOOK_FIELD_TOOLTIP_TEXT = "Enter address book to target.";
    private static final String ADDRESS_BOOK_FIELD_PROMPT_TEXT = "Address Book";
    private static final int CONSOLE_WIDTH = 300;
    private static final int CONSOLE_HEIGHT = 600;

    private static final Random RANDOM_GENERATOR = new Random();
    private static final int MIN_DELAY_IN_SEC = 1;
    private static final int MAX_DELAY_IN_SEC = 5;

    private boolean shouldDelayNext = false;
    private boolean shouldFailNext = false;

    private String addressBookName;
    private TextArea statusArea;
    private TextField addressBookField;
    private CheckBox failSyncUpsCheckBox;

    /**
     * Initializes CloudManipulator with data found in config's cloudDataFilePath
     * Upon failure, it will initialize an empty address book with config's addressBookName
     *
     * @param config
     */
    public CloudManipulator(Config config) {
        super(config);
        if (config.getCloudDataFilePath() != null) {
            initializeCloudFile(config.getCloudDataFilePath(), config.getAddressBookName());
        } else {
            initializeCloudFile(new CloudAddressBook(config.getAddressBookName()));
        }
    }

    /**
     * Initializes CloudManipulator with the provided cloud address book
     *
     * @param config
     */
    public CloudManipulator(Config config, CloudAddressBook cloudAddressBook) {
        super(config);
        initializeCloudFile(cloudAddressBook);
    }

    /**
     * Attempts to read a CloudAddressBook from the given cloudDataFilePath,
     * then initializes a cloud file with the read cloudAddressBook
     *
     * Initializes an empty cloud file with addressBookName if read from cloudDataFilePath fails
     *
     * @param cloudDataFilePath
     * @param addressBookName
     */
    private void initializeCloudFile(String cloudDataFilePath, String addressBookName) {
        CloudAddressBook cloudAddressBook;
        try {
            cloudAddressBook = fileHandler.readCloudAddressBookFromFile(cloudDataFilePath);
            initializeCloudFile(cloudAddressBook);
        } catch (DataConversionException e) {
            logger.fatal("Error reading from cloud data file: {}", cloudDataFilePath);
            assert false : "Error initializing cloud file: data conversion error during file reading";
        } catch (FileNotFoundException e) {
            logger.warn("Invalid cloud data file path provided: {}. Using empty address book for cloud", cloudDataFilePath);
            initializeCloudFile(new CloudAddressBook(addressBookName));
        }
    }

    /**
     * Initializes a cloud file with the cloudAddressBook
     *
     * @param cloudAddressBook
     */
    private void initializeCloudFile(CloudAddressBook cloudAddressBook) {
        try {
            this.addressBookName = cloudAddressBook.getName();
            fileHandler.initializeAddressBook(cloudAddressBook.getName());
            fileHandler.writeCloudAddressBookToCloudFile(cloudAddressBook);
        } catch (FileNotFoundException e) {
            logger.fatal("Cloud file cannot be found for: {}", cloudAddressBook.getName());
            assert false : "Error initializing cloud file: cloud file cannot be found.";
        } catch (DataConversionException | IOException e) {
            logger.fatal("Error initializing cloud file for: {}", cloudAddressBook.getName());
            assert false : "Error initializing cloud file: data conversion error";
        }
    }

    public void start(Stage stage) {
        VBox consoleBox = getConsoleBox();

        addressBookField = getAddressBookNameField(addressBookName);
        failSyncUpsCheckBox = getFailSyncUpsCheckBox();
        statusArea = getStatusArea();

        Button delayButton = getButton(DELAY_BUTTON_TEXT, getIcon(DELAY_BUTTON_ICON_PATH), actionEvent -> shouldDelayNext = true);
        Button failButton = getButton(FAIL_BUTTON_TEXT, getIcon(FAIL_BUTTON_ICON_PATH), actionEvent -> shouldFailNext = true);
        Button simulatePersonAdditionButton = getButton(ADD_PERSON_TEXT, null, actionEvent -> addRandomPersonToAddressBookFile(addressBookField::getText));
        Button simulatePersonModificationButton = getButton(MODIFY_PERSON_TEXT, null, actionEvent -> modifyRandomPersonInAddressBookFile(addressBookField::getText));
        Button simulatePersonDeletionButton = getButton(DELETE_PERSON_TEXT, null, actionEvent -> deleteRandomPersonInAddressBookFile(addressBookField::getText));
        Button simulateTagAdditionButton = getButton(ADD_TAG_TEXT, null, actionEvent -> addRandomTagToAddressBookFile(addressBookField::getText));
        Button simulateTagModificationButton = getButton(MODIFY_TAG_TEXT, null, actionEvent -> modifyRandomTagInAddressBookFile(addressBookField::getText));
        Button simulateTagDeletionButton = getButton(DELETE_TAG_TEXT, null, actionEvent -> deleteRandomPersonInAddressBookFile(addressBookField::getText));

        consoleBox.getChildren().addAll(failSyncUpsCheckBox, delayButton, failButton, addressBookField, simulatePersonAdditionButton,
                                        simulatePersonModificationButton, simulatePersonDeletionButton, simulateTagAdditionButton,
                                        simulateTagModificationButton, simulateTagDeletionButton, statusArea);

        Dialog<Void> dialog = getConsoleDialog(stage);
        dialog.getDialogPane().getChildren().add(consoleBox);
        dialog.show();
    }

    private Dialog<Void> getConsoleDialog(Stage stage) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initModality(Modality.NONE); // so that the dialog does not prevent interaction with the app
        dialog.setTitle(CLOUD_MANIPULATOR_TITLE);
        dialog.setX(stage.getX() + 740);
        dialog.setY(stage.getY());
        dialog.getDialogPane().setPrefWidth(CONSOLE_WIDTH);
        dialog.getDialogPane().setPrefHeight(CONSOLE_HEIGHT);
        return dialog;
    }

    private CheckBox getFailSyncUpsCheckBox() {
        CheckBox failSyncUpsCheckBox = new CheckBox();
        failSyncUpsCheckBox.setText("Fail all Sync-Up requests.");
        failSyncUpsCheckBox.setMinWidth(CONSOLE_WIDTH);
        return failSyncUpsCheckBox;
    }

    private VBox getConsoleBox() {
        VBox consoleBox = new VBox();
        consoleBox.setMinWidth(CONSOLE_WIDTH);
        return consoleBox;
    }

    private TextField getAddressBookNameField(String startingText) {
        TextField addressBookNameField = new TextField();
        if (startingText != null) {
            addressBookNameField.setText(startingText);
        }
        addressBookNameField.setPromptText(ADDRESS_BOOK_FIELD_PROMPT_TEXT);
        addressBookNameField.setMinWidth(CONSOLE_WIDTH);
        addressBookNameField.setTooltip(new Tooltip(ADDRESS_BOOK_FIELD_TOOLTIP_TEXT));
        return addressBookNameField;
    }

    private TextArea getStatusArea() {
        TextArea statusArea = new TextArea();
        statusArea.setEditable(false);
        statusArea.setMinWidth(CONSOLE_WIDTH);
        statusArea.setMinHeight(300);
        statusArea.setWrapText(true);
        return statusArea;
    }

    private ImageView getIcon(String resourcePath) {
        URL url = getClass().getResource(resourcePath);
        ImageView imageView = new ImageView(new Image(url.toString()));
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);
        return imageView;
    }

    private Button getButton(String text, ImageView graphic, EventHandler<ActionEvent> actionEventHandler) {
        Button button = new Button(text);
        button.setMinWidth(CONSOLE_WIDTH);
        button.setGraphic(graphic);
        button.setOnAction(actionEventHandler);
        return button;
    }

    /**
     * Returns a random element from the list
     * @param list non-empty list
     * @param <T>
     * @return
     */
    private <T> T getRandom(List<T> list) {
        int sizeOfList = list.size();
        return list.get(RANDOM_GENERATOR.nextInt(sizeOfList));
    }

    private void logAndUpdateStatus(String newStatus) {
        logger.debug(newStatus);
        statusArea.appendText(LocalDateTime.now() + ": " + newStatus + "\n");
    }

    private void deleteRandomPersonInAddressBookFile(Supplier<String> addressBookName) {
        String targetAddressBook = addressBookName.get();
        logAndUpdateStatus("Deleting random person in address book " + targetAddressBook);
        try {
            CloudAddressBook cloudAddressBook = fileHandler.readCloudAddressBookFromCloudFile(targetAddressBook);
            List<CloudPerson> allPersons = cloudAddressBook.getAllPersons();
            if (allPersons.isEmpty()) return;
            allPersons.remove(getRandom(allPersons));
            fileHandler.writeCloudAddressBookToCloudFile(cloudAddressBook);
        } catch (FileNotFoundException e) {
            logAndUpdateStatus("Failed to delete person: cloud address book " + targetAddressBook + " not found");
        } catch (DataConversionException e) {
            logAndUpdateStatus("Failed to delete person: error occurred");
        }
    }

    private void deleteRandomTagInAddressBookFile(Supplier<String> addressBookName) {
        String targetAddressBook = addressBookName.get();
        logAndUpdateStatus("Deleting random tag in address book " + targetAddressBook);
        try {
            CloudAddressBook cloudAddressBook = fileHandler.readCloudAddressBookFromCloudFile(targetAddressBook);
            List<CloudTag> allTags = cloudAddressBook.getAllTags();
            if (allTags.isEmpty()) return;
            allTags.remove(getRandom(allTags));
            fileHandler.writeCloudAddressBookToCloudFile(cloudAddressBook);
        } catch (FileNotFoundException e) {
            logAndUpdateStatus("Failed to delete tag: cloud address book " + targetAddressBook + " not found");
        } catch (DataConversionException e) {
            logAndUpdateStatus("Failed to delete tag: error occurred");
        }
    }

    private void modifyRandomPersonInAddressBookFile(Supplier<String> addressBookName) {
        String targetAddressBook = addressBookName.get();
        logAndUpdateStatus("Modifying random person in address book " + targetAddressBook);
        try {
            CloudAddressBook cloudAddressBook = fileHandler.readCloudAddressBookFromCloudFile(targetAddressBook);
            List<CloudPerson> allPersons = cloudAddressBook.getAllPersons();
            if (allPersons.isEmpty()) return;
            modifyCloudPerson(getRandom(allPersons));
            fileHandler.writeCloudAddressBookToCloudFile(cloudAddressBook);
        } catch (FileNotFoundException e) {
            logAndUpdateStatus("Failed to modify person: cloud address book " + targetAddressBook + " not found");
        } catch (DataConversionException e) {
            logAndUpdateStatus("Failed to modify person: error occurred");
        }
    }

    private void modifyRandomTagInAddressBookFile(Supplier<String> addressBookName) {
        String targetAddressBook = addressBookName.get();
        logAndUpdateStatus("Modifying random tag in address book " + targetAddressBook);
        try {
            CloudAddressBook cloudAddressBook = fileHandler.readCloudAddressBookFromCloudFile(targetAddressBook);
            List<CloudTag> allTags = cloudAddressBook.getAllTags();
            if (allTags.isEmpty()) return;
            modifyCloudTag(getRandom(allTags));
            fileHandler.writeCloudAddressBookToCloudFile(cloudAddressBook);
        } catch (FileNotFoundException e) {
            logAndUpdateStatus("Failed to modify tag: cloud address book " + targetAddressBook + " not found");
        } catch (DataConversionException e) {
            logAndUpdateStatus("Failed to modify tag: error occurred");
        }
    }

    private void addRandomPersonToAddressBookFile(Supplier<String> addressBookName) {
        String targetAddressBook = addressBookName.get();
        logAndUpdateStatus("Adding random person to address book " + targetAddressBook);
        try {
            CloudAddressBook cloudAddressBook = fileHandler.readCloudAddressBookFromCloudFile(targetAddressBook);
            addCloudPerson(cloudAddressBook.getAllPersons());
            fileHandler.writeCloudAddressBookToCloudFile(cloudAddressBook);
        } catch (FileNotFoundException e) {
            logAndUpdateStatus("Failed to add person: cloud address book " + targetAddressBook + " not found");
        } catch (DataConversionException e) {
            logAndUpdateStatus("Failed to add person: error occurred");
        }
    }

    private void addRandomTagToAddressBookFile(Supplier<String> addressBookName) {
        String targetAddressBook = addressBookName.get();
        logAndUpdateStatus("Adding random tag to address book " + targetAddressBook);
        try {
            CloudAddressBook cloudAddressBook = fileHandler.readCloudAddressBookFromCloudFile(targetAddressBook);
            addCloudTag(cloudAddressBook.getAllTags());
            fileHandler.writeCloudAddressBookToCloudFile(cloudAddressBook);
        } catch (FileNotFoundException e) {
            logAndUpdateStatus("Failed to add tag: cloud address book " + targetAddressBook + " not found");
        } catch (DataConversionException e) {
            logAndUpdateStatus("Failed to add tag: error occurred");
        }
    }

    @Override
    public RemoteResponse createPerson(String addressBookName, CloudPerson newPerson, String previousETag) {
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFail(true)) return getNetworkFailedResponse();
        RemoteResponse actualResponse = super.createPerson(addressBookName, newPerson, previousETag);
        return actualResponse;
    }

    private boolean shouldFail(boolean isSyncUp) {
        if (shouldFailNext) return true;
        if (!isSyncUp) return false;
        return failSyncUpsCheckBox.isSelected();
    }

    @Override
    public RemoteResponse getPersons(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFail(false)) return getNetworkFailedResponse();
        RemoteResponse actualResponse = super.getPersons(addressBookName, pageNumber, resourcesPerPage, previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse getUpdatedPersons(String addressBookName, String timeString, int pageNumber, int resourcesPerPage, String previousETag) {
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFail(false)) return getNetworkFailedResponse();
        RemoteResponse actualResponse = super.getUpdatedPersons(addressBookName, timeString, pageNumber, resourcesPerPage, previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse getTags(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFail(false)) return getNetworkFailedResponse();
        RemoteResponse actualResponse = super.getTags(addressBookName, pageNumber, resourcesPerPage, previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse getRateLimitStatus(String previousETag) {
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFail(false)) return getNetworkFailedResponse();
        RemoteResponse actualResponse = super.getRateLimitStatus(previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse updatePerson(String addressBookName, int personId, CloudPerson updatedPerson, String previousETag) {
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFail(true)) return getNetworkFailedResponse();
        RemoteResponse actualResponse = super.updatePerson(addressBookName, personId, updatedPerson, previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse deletePerson(String addressBookName, int personId) {
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFail(true)) return getNetworkFailedResponse();
        RemoteResponse actualResponse = super.deletePerson(addressBookName, personId);
        return actualResponse;
    }

    @Override
    public RemoteResponse createTag(String addressBookName, CloudTag newTag, String previousETag) {
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFail(true)) return getNetworkFailedResponse();
        RemoteResponse actualResponse = super.createTag(addressBookName, newTag, previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse editTag(String addressBookName, String oldTagName, CloudTag updatedTag, String previousETag) {
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFail(true)) return getNetworkFailedResponse();
        RemoteResponse actualResponse = super.editTag(addressBookName, oldTagName, updatedTag, previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse deleteTag(String addressBookName, String tagName) {
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFail(true)) return getNetworkFailedResponse();
        RemoteResponse actualResponse = super.deleteTag(addressBookName, tagName);
        return actualResponse;
    }

    @Override
    public RemoteResponse createAddressBook(String addressBookName) {
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFail(true)) return getNetworkFailedResponse();
        RemoteResponse actualResponse = super.createAddressBook(addressBookName);
        return actualResponse;
    }

    private void addCloudPerson(List<CloudPerson> personList) {
        CloudPerson person = new CloudPerson(java.util.UUID.randomUUID().toString(),
                java.util.UUID.randomUUID().toString());
        logger.info("Simulating person addition: '{}'", person);
        personList.add(person);
    }

    private void addCloudTag(List<CloudTag> tagList) {
        CloudTag tag = new CloudTag(java.util.UUID.randomUUID().toString());
        logger.debug("Simulating tag addition: '{}'", tag);
        tagList.add(tag);
    }

    private void deleteCloudTag(List<CloudTag> tagList) {
        CloudTag tagRemoved = tagList.remove(RANDOM_GENERATOR.nextInt(tagList.size()));
        logger.debug("Simulating tag deletion: '{}'", tagRemoved);
    }

    private void modifyCloudPerson(CloudPerson cloudPerson) {
        logger.debug("Simulating person modification on: '{}'", cloudPerson);
        cloudPerson.setCity(java.util.UUID.randomUUID().toString());
        cloudPerson.setStreet(java.util.UUID.randomUUID().toString());
        cloudPerson.setPostalCode(String.valueOf(RANDOM_GENERATOR.nextInt(999999)));
    }

    private void modifyCloudTag(CloudTag cloudTag) {
        logger.debug("Simulating tag modification on: '{}'", cloudTag);
        cloudTag.setName(UUID.randomUUID().toString());
    }

    private void delayRandomAmount() {
        shouldDelayNext = false;
        long delayAmount = RANDOM_GENERATOR.nextInt(MAX_DELAY_IN_SEC - MIN_DELAY_IN_SEC) + MIN_DELAY_IN_SEC;
        try {
            logAndUpdateStatus("Delaying response by " + delayAmount + " secs");
            TimeUnit.SECONDS.sleep(delayAmount);
        } catch (InterruptedException e) {
            logAndUpdateStatus("Error occurred while delaying cloud response");
        }
    }

    private RemoteResponse getNetworkFailedResponse() {
        shouldFailNext = false;
        logAndUpdateStatus("Simulated network failure occurred!");
        return new RemoteResponse(HttpURLConnection.HTTP_CLIENT_TIMEOUT, null, cloudRateLimitStatus, null);
    }
}
