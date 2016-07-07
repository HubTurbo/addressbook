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
    private static final String CLOUD_MANIPULATOR_TITLE = "Cloud Manipulator";
    private static final String ADDRESS_BOOK_FIELD_TOOLTIP_TEXT = "Enter address book to target.";
    private static final int CONSOLE_WIDTH = 300;
    private static final int CONSOLE_HEIGHT = 600;

    private static final Random RANDOM_GENERATOR = new Random();
    private static final int MIN_DELAY_IN_SEC = 1;
    private static final int MAX_DELAY_IN_SEC = 5;
    private static final int MAX_NUM_PERSONS_TO_ADD = 2;
    private static final int MAX_NUM_TAGS_TO_ADD = 2;

    private boolean shouldDelayNext = false;
    private boolean shouldFailNext = false;

    private String addressBookName;
    private TextArea statusArea;
    private TextField addressBookField;

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
            fileHandler.initializeCloudAddressBookFile(cloudAddressBook.getName());
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
        VBox consoleBox = new VBox();
        consoleBox.setMinWidth(CONSOLE_WIDTH);
        addressBookField = getAddressBookNameField(addressBookName);

        Button delayButton = getButton(DELAY_BUTTON_TEXT, getIcon(DELAY_BUTTON_ICON_PATH), actionEvent -> shouldDelayNext = true);
        Button failButton = getButton(FAIL_BUTTON_TEXT, getIcon(FAIL_BUTTON_ICON_PATH), actionEvent -> shouldFailNext = true);
        Button simulatePersonAdditionButton = getButton(ADD_PERSON_TEXT, null, actionEvent -> addRandomPersonToAddressBookFile(addressBookField::getText));
        Button simulatePersonModificationButton = getButton(MODIFY_PERSON_TEXT, null, actionEvent -> modifyRandomPersonInAddressBookFile(addressBookField::getText));

        statusArea = getStatusArea();
        consoleBox.getChildren().addAll(delayButton, failButton, addressBookField, simulatePersonAdditionButton,
                                       simulatePersonModificationButton, statusArea);

        Dialog<Void> dialog = new Dialog<>();
        dialog.initModality(Modality.NONE); // so that the dialog does not prevent interaction with the app
        dialog.initOwner(stage);
        dialog.setTitle(CLOUD_MANIPULATOR_TITLE);
        dialog.getDialogPane().getChildren().add(consoleBox);
        dialog.setX(stage.getX() + 740);
        dialog.setY(stage.getY());
        dialog.getDialogPane().setPrefWidth(CONSOLE_WIDTH);
        dialog.getDialogPane().setPrefHeight(CONSOLE_HEIGHT);
        dialog.show();
    }

    private TextField getAddressBookNameField(String startingText) {
        TextField addressBookNameField = new TextField();
        if (startingText != null) {
            addressBookNameField.setText(startingText);
        }
        addressBookNameField.setPromptText("Address Book");
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

    private CloudPerson getRandomPerson(List<CloudPerson> listOfCloudPersons) {
        int sizeOfList = listOfCloudPersons.size();
        return listOfCloudPersons.get(RANDOM_GENERATOR.nextInt(sizeOfList - 1));
    }

    private void logAndUpdateStatus(String newStatus) {
        logger.debug(newStatus);
        statusArea.appendText(LocalDateTime.now() + ": " + newStatus + "\n");
    }

    private void modifyRandomPersonInAddressBookFile(Supplier<String> addressBookName) {
        logAndUpdateStatus("Modifying random person in address book " + addressBookName.get());
        try {
            CloudAddressBook cloudAddressBook = fileHandler.readCloudAddressBookFromCloudFile(addressBookName.get());
            modifyCloudPerson(getRandomPerson(cloudAddressBook.getAllPersons()));
            fileHandler.writeCloudAddressBookToCloudFile(cloudAddressBook);
        } catch (FileNotFoundException e) {
            logAndUpdateStatus("Failed to modify person: cloud address book " + addressBookName.get() + " not found");
        } catch (DataConversionException e) {
            logAndUpdateStatus("Failed to modify person: error occurred");
        }
    }

    private void addRandomPersonToAddressBookFile(Supplier<String> addressBookName) {
        logAndUpdateStatus("Adding random person to address book");
        try {
            CloudAddressBook cloudAddressBook = fileHandler.readCloudAddressBookFromCloudFile(addressBookName.get());
            addCloudPersons(cloudAddressBook.getAllPersons());
            fileHandler.writeCloudAddressBookToCloudFile(cloudAddressBook);
        } catch (FileNotFoundException e) {
            logAndUpdateStatus("Failed to add person: cloud address book " + addressBookName.get() + " not found");
        } catch (DataConversionException e) {
            logAndUpdateStatus("Failed to modify person: error occurred");
        }
    }

    @Override
    public RemoteResponse createPerson(String addressBookName, CloudPerson newPerson, String previousETag) {
        RemoteResponse actualResponse = super.createPerson(addressBookName, newPerson, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFailNext) return getNetworkFailedResponse();
        return actualResponse;
    }

    @Override
    public RemoteResponse getPersons(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
        RemoteResponse actualResponse = super.getPersons(addressBookName, pageNumber, resourcesPerPage, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFailNext) return getNetworkFailedResponse();
        return actualResponse;
    }

    @Override
    public RemoteResponse getUpdatedPersons(String addressBookName, String timeString, int pageNumber, int resourcesPerPage, String previousETag) {
        RemoteResponse actualResponse = super.getUpdatedPersons(addressBookName, timeString, pageNumber, resourcesPerPage, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFailNext) return getNetworkFailedResponse();
        return actualResponse;
    }

    @Override
    public RemoteResponse getTags(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
        RemoteResponse actualResponse = super.getTags(addressBookName, pageNumber, resourcesPerPage, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFailNext) return getNetworkFailedResponse();
        return actualResponse;
    }

    @Override
    public RemoteResponse getRateLimitStatus(String previousETag) {
        RemoteResponse actualResponse = super.getRateLimitStatus(previousETag);
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFailNext) return getNetworkFailedResponse();
        return actualResponse;
    }

    @Override
    public RemoteResponse updatePerson(String addressBookName, int personId, CloudPerson updatedPerson, String previousETag) {
        RemoteResponse actualResponse = super.updatePerson(addressBookName, personId, updatedPerson, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFailNext) return getNetworkFailedResponse();
        return actualResponse;
    }

    @Override
    public RemoteResponse deletePerson(String addressBookName, int personId) {
        RemoteResponse actualResponse = super.deletePerson(addressBookName, personId);
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFailNext) return getNetworkFailedResponse();
        return actualResponse;
    }

    @Override
    public RemoteResponse createTag(String addressBookName, CloudTag newTag, String previousETag) {
        RemoteResponse actualResponse = super.createTag(addressBookName, newTag, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFailNext) return getNetworkFailedResponse();
        return actualResponse;
    }

    @Override
    public RemoteResponse editTag(String addressBookName, String oldTagName, CloudTag updatedTag, String previousETag) {
        RemoteResponse actualResponse = super.editTag(addressBookName, oldTagName, updatedTag, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFailNext) return getNetworkFailedResponse();
        return actualResponse;
    }

    @Override
    public RemoteResponse deleteTag(String addressBookName, String tagName) {
        RemoteResponse actualResponse = super.deleteTag(addressBookName, tagName);
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFailNext) return getNetworkFailedResponse();
        return actualResponse;
    }

    @Override
    public RemoteResponse createAddressBook(String addressBookName) {
        RemoteResponse actualResponse = super.createAddressBook(addressBookName);
        if (shouldDelayNext) delayRandomAmount();
        if (shouldFailNext) return getNetworkFailedResponse();
        return actualResponse;
    }

    private List<CloudPerson> mutateCloudPersonList(List<CloudPerson> CloudPersonList) {
        modifyCloudPersonList(CloudPersonList);
        addCloudPersons(CloudPersonList);
        return CloudPersonList;
    }

    private List<CloudTag> mutateCloudTagList(List<CloudTag> CloudTagList) {
        modifyCloudTagList(CloudTagList);
        addCloudTags(CloudTagList);
        return CloudTagList;
    }

    private void modifyCloudPersonList(List<CloudPerson> cloudPersonList) {
        cloudPersonList.stream()
                .forEach(this::modifyCloudPerson);
    }

    private void modifyCloudTagList(List<CloudTag> cloudTagList) {
        cloudTagList.stream()
                .forEach(this::modifyCloudTag);
    }

    private void addCloudPersons(List<CloudPerson> personList) {
        for (int i = 0; i < MAX_NUM_PERSONS_TO_ADD; i++) {
            CloudPerson person = new CloudPerson(java.util.UUID.randomUUID().toString(),
                    java.util.UUID.randomUUID().toString());
            logger.info("Simulating person addition: '{}'", person);
            personList.add(person);
        }
    }

    private void addCloudTags(List<CloudTag> tagList) {
        for (int i = 0; i < MAX_NUM_TAGS_TO_ADD; i++) {
            CloudTag tag = new CloudTag(java.util.UUID.randomUUID().toString());
            logger.debug("Simulating tag addition: '{}'", tag);
            tagList.add(tag);
        }
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
