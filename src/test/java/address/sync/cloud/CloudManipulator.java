package address.sync.cloud;

import address.exceptions.DataConversionException;
import address.sync.cloud.model.CloudAddressBook;
import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;
import address.util.AppLogger;
import address.util.Config;
import address.util.LoggerManager;
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

/**
 * Adds data/response manipulation features to the CloudSimulator
 *
 * Launches a GUI for the tester to simulate errors and data modifications
 * By default, the responses returned should be same as the ones returned from CloudSimulator, with little to no delay
 */
public class CloudManipulator extends CloudSimulator {
    private class Manipulation {// TODO: create a list of this so that the tester can chain a set of errors
        long delay = 0;
        boolean removePerson = false;
    }

    private static final AppLogger logger = LoggerManager.getLogger(CloudManipulator.class);
    private static final Random RANDOM_GENERATOR = new Random();
    private static final int MIN_DELAY_IN_SEC = 1;
    private static final int MAX_DELAY_IN_SEC = 5;
    private static final int MAX_NUM_PERSONS_TO_ADD = 2;
    private static final int MAX_NUM_TAGS_TO_ADD = 2;

    private boolean shouldDelayNext = false;
    private boolean shouldFailNext = false;

    private TextArea statusArea;

    public CloudManipulator(Config config) {
        super(config);
        if (config.getCloudDataFilePath() != null) initializeCloudFile(config.getCloudDataFilePath(), config.getAddressBookName());
    }

    public CloudManipulator(Config config, CloudAddressBook cloudAddressBook) {
        super(config);
        initializeCloudFile(cloudAddressBook, config.getAddressBookName());
    }

    private void initializeCloudFile(String cloudDataFilePath, String addressBookName) {
        try {
            CloudAddressBook cloudAddressBook = fileHandler.readCloudAddressBookFromFile(cloudDataFilePath);
            initializeCloudFile(cloudAddressBook, addressBookName);
        } catch (DataConversionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeCloudFile(CloudAddressBook cloudAddressBook, String addressBookName) {
        try {
            fileHandler.initializeCloudAddressBookFile(addressBookName);
            fileHandler.writeCloudAddressBookToCloudFile(cloudAddressBook);
        } catch (DataConversionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(Stage stage) {
        VBox buttonBox = new VBox();
        buttonBox.setMinWidth(300);
        Button delayButton = getButton("Delay next response");
        ImageView clockIcon = getIcon("/images/clock.png");
        delayButton.setGraphic(clockIcon);
        delayButton.setOnAction(actionEvent -> shouldDelayNext = true);

        Button failButton = getButton("Fail next response");
        ImageView failIcon = getIcon("/images/fail.png");
        failButton.setGraphic(failIcon);
        failButton.setOnAction(actionEvent -> shouldFailNext = true);

        Button simulatePersonAdditionButton = getButton("Add person");
        simulatePersonAdditionButton.setOnAction(actionEvent -> addRandomPersonToAddressBookFile("cloud"));
        Button simulatePersonModificationButton = getButton("Modify person");
        simulatePersonModificationButton.setOnAction(actionEvent -> modifyRandomPersonInAddressBookFile("cloud"));

        statusArea = getStatusArea();
        buttonBox.getChildren().addAll(delayButton, failButton, simulatePersonAdditionButton, simulatePersonModificationButton, statusArea);


        Dialog<Void> dialog = new Dialog<>();
        dialog.initModality(Modality.NONE);
        dialog.initOwner(stage);
        dialog.setTitle("Cloud Manipulator");
        dialog.getDialogPane().getChildren().add(buttonBox);
        dialog.getDialogPane().setStyle("-fx-border-color: black;");
        dialog.setX(stage.getX() + 740);
        dialog.setY(stage.getY());
        dialog.getDialogPane().setPrefWidth(300);
        dialog.getDialogPane().setPrefHeight(600);
        dialog.show();
    }

    private TextArea getStatusArea() {
        TextArea statusArea = new TextArea();
        statusArea.setEditable(false);
        statusArea.setMinWidth(300);
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

    private Button getButton(String text) {
        Button button = new Button(text);
        button.setMinWidth(300);
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

    private void modifyRandomPersonInAddressBookFile(String addressBookName) {
        logAndUpdateStatus("Modifying random person in address book");
        try {
            CloudAddressBook cloudAddressBook = fileHandler.readCloudAddressBookFromCloudFile(addressBookName);
            modifyCloudPerson(getRandomPerson(cloudAddressBook.getAllPersons()));
            fileHandler.writeCloudAddressBookToCloudFile(cloudAddressBook);
        } catch (FileNotFoundException e) {
            logAndUpdateStatus("Failed to modify person: cloud addressbook " + addressBookName + " not found");
        } catch (DataConversionException e) {
            logAndUpdateStatus("Failed to modify person: error occurred");
        }
    }

    private void addRandomPersonToAddressBookFile(String addressBookName) {
        logAndUpdateStatus("Adding random person to address book");
        try {
            CloudAddressBook cloudAddressBook = fileHandler.readCloudAddressBookFromCloudFile(addressBookName);
            addCloudPersons(cloudAddressBook.getAllPersons());
            fileHandler.writeCloudAddressBookToCloudFile(cloudAddressBook);
        } catch (FileNotFoundException e) {
            logAndUpdateStatus("Failed to add person: cloud addressbook " + addressBookName + " not found");
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
            logger.info("Simulating data addition for person '{}'", person);
            personList.add(person);
        }
    }

    private void addCloudTags(List<CloudTag> tagList) {
        for (int i = 0; i < MAX_NUM_TAGS_TO_ADD; i++) {
            CloudTag tag = new CloudTag(java.util.UUID.randomUUID().toString());
            logger.debug("Simulating data addition for tag '{}'", tag);
            tagList.add(tag);
        }
    }

    private void modifyCloudPerson(CloudPerson cloudPerson) {
        logger.debug("Simulating data modification on person '{}'", cloudPerson);
        cloudPerson.setCity(java.util.UUID.randomUUID().toString());
        cloudPerson.setStreet(java.util.UUID.randomUUID().toString());
        cloudPerson.setPostalCode(String.valueOf(RANDOM_GENERATOR.nextInt(999999)));
    }

    private void modifyCloudTag(CloudTag cloudTag) {
        logger.debug("Simulating data modification on tag '{}'", cloudTag);
        cloudTag.setName(UUID.randomUUID().toString());
    }

    private void delayRandomAmount() {
        long delayAmount = RANDOM_GENERATOR.nextInt(MAX_DELAY_IN_SEC - MIN_DELAY_IN_SEC) + MIN_DELAY_IN_SEC;
        try {
            logAndUpdateStatus("Delayed response by " + delayAmount + " secs");
            TimeUnit.SECONDS.sleep(delayAmount);
        } catch (InterruptedException e) {
            logAndUpdateStatus("Error occurred while delaying cloud response");
        }
        shouldDelayNext = false;
    }

    private RemoteResponse getNetworkFailedResponse() {
        shouldFailNext = false;
        logAndUpdateStatus("Simulated network failure occurred!");
        return new RemoteResponse(HttpURLConnection.HTTP_CLIENT_TIMEOUT, null, cloudRateLimitStatus, null);
    }
}
