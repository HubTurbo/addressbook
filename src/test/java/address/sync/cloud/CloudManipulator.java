package address.sync.cloud;

import address.exceptions.DataConversionException;
import address.sync.cloud.model.CloudAddressBook;
import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;
import address.util.AppLogger;
import address.util.Config;
import address.util.LoggerManager;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Data returned by this cloud may be modified due to
 * simulated corruption or its responses may have significant delays.
 */
public class CloudManipulator extends CloudSimulator {
    private class Manipulation {// TODO: create a list of this so that the tester can chain a set of errors
        long delay = 0;
        boolean removePerson = false;
    }

    private static final AppLogger logger = LoggerManager.getLogger(CloudManipulator.class);
    private static final Random RANDOM_GENERATOR = new Random();
    private static final double FAILURE_PROBABILITY = 0.1;
    private static final double NETWORK_DELAY_PROBABILITY = 1.0;
    private static final double MODIFY_PERSON_PROBABILITY = 0.1;
    private static final double MODIFY_TAG_PROBABILITY = 0.05;
    private static final double ADD_PERSON_PROBABILITY = 0.05;
    private static final double ADD_TAG_PROBABILITY = 0.025;
    private static final int MIN_DELAY_IN_SEC = 1;
    private static final int MAX_DELAY_IN_SEC = 5;
    private static final int MAX_NUM_PERSONS_TO_ADD = 2;
    private static final int MAX_NUM_TAGS_TO_ADD = 2;

    private boolean isManipulable = true;
    private boolean shouldDelayNext = false;

    public CloudManipulator(Config config) {
        super(config);
        isManipulable = config.isCloudManipulable;
    }

    public void start(Stage stage) {
        VBox buttonBox = new VBox();
        buttonBox.setMinWidth(300);
        Button delayButton = getButton("Delay next response");
        delayButton.setOnAction(actionEvent -> shouldDelayNext = true);
        Button simulatePersonAdditionButton = getButton("Add person");
        simulatePersonAdditionButton.setOnAction(actionEvent -> addRandomPersonToAddressBookFile("cloud"));

        buttonBox.getChildren().addAll(delayButton, simulatePersonAdditionButton);

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.getDialogPane().getChildren().add(buttonBox);
        dialog.getDialogPane().setStyle("-fx-border-color: black;");
        dialog.setX(stage.getX() + 740);
        dialog.setY(stage.getY());
        dialog.getDialogPane().setPrefWidth(300);
        dialog.show();
    }

    private Button getButton(String text) {
        Button button = new Button(text);
        button.setMinWidth(300);
        return button;
    }

    private void addRandomPersonToAddressBookFile(String addressBookName) {
        try {
            CloudAddressBook cloudAddressBook = fileHandler.readCloudAddressBookFromFile(addressBookName);
            addCloudPersonsBasedOnChance(cloudAddressBook.getAllPersons());
            fileHandler.writeCloudAddressBookToFile(cloudAddressBook);
        } catch (FileNotFoundException e) {
            logger.warn("Failed to add person: cloud addressbook {} not found", addressBookName);
        } catch (DataConversionException e) {
            logger.warn("Error adding person");
        }
    }

    @Override
    public RemoteResponse createPerson(String addressBookName, CloudPerson newPerson, String previousETag) {
        RemoteResponse actualResponse = super.createPerson(addressBookName, newPerson, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        return actualResponse;
    }

    @Override
    public RemoteResponse getPersons(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
        RemoteResponse actualResponse = super.getPersons(addressBookName, pageNumber, resourcesPerPage, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        return actualResponse;
    }

    @Override
    public RemoteResponse getUpdatedPersons(String addressBookName, String timeString, int pageNumber, int resourcesPerPage, String previousETag) {
        RemoteResponse actualResponse = super.getUpdatedPersons(addressBookName, timeString, pageNumber, resourcesPerPage, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        return actualResponse;
    }

    @Override
    public RemoteResponse getTags(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
        RemoteResponse actualResponse = super.getTags(addressBookName, pageNumber, resourcesPerPage, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        return actualResponse;
    }

    @Override
    public RemoteResponse getRateLimitStatus(String previousETag) {
        RemoteResponse actualResponse = super.getRateLimitStatus(previousETag);
        if (shouldDelayNext) delayRandomAmount();
        return actualResponse;
    }

    @Override
    public RemoteResponse updatePerson(String addressBookName, int personId, CloudPerson updatedPerson, String previousETag) {
        RemoteResponse actualResponse = super.updatePerson(addressBookName, personId, updatedPerson, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        return actualResponse;
    }

    @Override
    public RemoteResponse deletePerson(String addressBookName, int personId) {
        RemoteResponse actualResponse = super.deletePerson(addressBookName, personId);
        if (shouldDelayNext) delayRandomAmount();
        return actualResponse;
    }

    @Override
    public RemoteResponse createTag(String addressBookName, CloudTag newTag, String previousETag) {
        RemoteResponse actualResponse = super.createTag(addressBookName, newTag, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        return actualResponse;
    }

    @Override
    public RemoteResponse editTag(String addressBookName, String oldTagName, CloudTag updatedTag, String previousETag) {
        RemoteResponse actualResponse = super.editTag(addressBookName, oldTagName, updatedTag, previousETag);
        if (shouldDelayNext) delayRandomAmount();
        return actualResponse;
    }

    @Override
    public RemoteResponse deleteTag(String addressBookName, String tagName) {
        RemoteResponse actualResponse = super.deleteTag(addressBookName, tagName);
        if (shouldDelayNext) delayRandomAmount();
        return actualResponse;
    }

    @Override
    public RemoteResponse createAddressBook(String addressBookName) {
        RemoteResponse actualResponse = super.createAddressBook(addressBookName);
        if (shouldDelayNext) delayRandomAmount();
        return actualResponse;
    }

    private List<CloudPerson> mutateCloudPersonList(List<CloudPerson> CloudPersonList) {
        modifyCloudPersonList(CloudPersonList);
        addCloudPersonsBasedOnChance(CloudPersonList);
        return CloudPersonList;
    }

    private List<CloudTag> mutateCloudTagList(List<CloudTag> CloudTagList) {
        modifyCloudTagListBasedOnChance(CloudTagList);
        addCloudTagsBasedOnChance(CloudTagList);
        return CloudTagList;
    }

    private void modifyCloudPersonList(List<CloudPerson> cloudPersonList) {
        cloudPersonList.stream()
                .forEach(this::modifyCloudPersonBasedOnChance);
    }

    private void modifyCloudTagListBasedOnChance(List<CloudTag> cloudTagList) {
        cloudTagList.stream()
                .forEach(this::modifyCloudTagBasedOnChance);
    }

    private void addCloudPersonsBasedOnChance(List<CloudPerson> personList) {
        for (int i = 0; i < MAX_NUM_PERSONS_TO_ADD; i++) {
            if (isManipulable && RANDOM_GENERATOR.nextDouble() <= ADD_PERSON_PROBABILITY) {
                CloudPerson person = new CloudPerson(java.util.UUID.randomUUID().toString(),
                        java.util.UUID.randomUUID().toString());
                logger.info("Simulating data addition for person '{}'", person);
                personList.add(person);
            }
        }
    }

    private void addCloudTagsBasedOnChance(List<CloudTag> tagList) {
        for (int i = 0; i < MAX_NUM_TAGS_TO_ADD; i++) {
            if (isManipulable && RANDOM_GENERATOR.nextDouble() <= ADD_TAG_PROBABILITY) {
                CloudTag tag = new CloudTag(java.util.UUID.randomUUID().toString());
                logger.debug("Simulating data addition for tag '{}'", tag);
                tagList.add(tag);
            }
        }
    }

    private void modifyCloudPersonBasedOnChance(CloudPerson cloudPerson) {
        if (!isManipulable || RANDOM_GENERATOR.nextDouble() > MODIFY_PERSON_PROBABILITY) return;
        logger.debug("Simulating data modification on person '{}'", cloudPerson);
        cloudPerson.setCity(java.util.UUID.randomUUID().toString());
        cloudPerson.setStreet(java.util.UUID.randomUUID().toString());
        cloudPerson.setPostalCode(String.valueOf(RANDOM_GENERATOR.nextInt(999999)));
    }

    private void modifyCloudTagBasedOnChance(CloudTag cloudTag) {
        if (!isManipulable || RANDOM_GENERATOR.nextDouble() > MODIFY_TAG_PROBABILITY) return;
        logger.debug("Simulating data modification on tag '{}'", cloudTag);
        cloudTag.setName(UUID.randomUUID().toString());
    }

    private void delayRandomAmount() {
        long delayAmount = RANDOM_GENERATOR.nextInt(MAX_DELAY_IN_SEC - MIN_DELAY_IN_SEC) + MIN_DELAY_IN_SEC;
        try {
            logger.debug("Delaying response by {} secs", delayAmount);
            TimeUnit.SECONDS.sleep(delayAmount);
        } catch (InterruptedException e) {
            logger.warn("Error occurred while delaying cloud response.");
        }
    }

    private boolean shouldSimulateNetworkFailure() {
        return isManipulable && RANDOM_GENERATOR.nextDouble() <= FAILURE_PROBABILITY;
    }

    private boolean shouldSimulateSlowResponse() {
        return isManipulable && RANDOM_GENERATOR.nextDouble() <= NETWORK_DELAY_PROBABILITY;
    }

    /**
     *
     if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
     if (shouldSimulateSlowResponse()) delayRandomAmount();
     */
}
