package address.sync.cloud;

import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;
import address.util.AppLogger;
import address.util.Config;
import address.util.LoggerManager;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.testfx.api.FxToolkit;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Data returned by this cloud may be modified due to
 * simulated corruption or its responses may have significant delays.
 */
public class CloudManipulator extends CloudSimulator {
    private class Manipulation {
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
    private boolean delayNext = false;

    public CloudManipulator(Config config) {
        super(config);
        isManipulable = config.isCloudManipulable;
    }

    public void start(Stage stage) {
        AnchorPane pane = new AnchorPane();
        Button delayButton = new Button("Delay next response");
        delayButton.setOnAction(actionEvent -> delayNext = true);
        pane.getChildren().add(delayButton);

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.getDialogPane().getChildren().add(pane);
        dialog.setX(stage.getX() + 740);
        dialog.setY(stage.getY());
        dialog.show();
    }

    @Override
    public RemoteResponse createPerson(String addressBookName, CloudPerson newPerson, String previousETag) {
        RemoteResponse actualResponse = super.createPerson(addressBookName, newPerson, previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse getPersons(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
        RemoteResponse actualResponse = super.getPersons(addressBookName, pageNumber, resourcesPerPage, previousETag);
        if (delayNext) {
            try {
                logger.info("Delaying for 10 seconds.");
                Thread.sleep(10000);
                delayNext = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return actualResponse;
    }

    @Override
    public RemoteResponse getUpdatedPersons(String addressBookName, String timeString, int pageNumber, int resourcesPerPage, String previousETag) {
        RemoteResponse actualResponse = super.getUpdatedPersons(addressBookName, timeString, pageNumber, resourcesPerPage, previousETag);
        if (delayNext) {
            try {
                logger.info("Delaying for 10 seconds.");
                Thread.sleep(10000);
                delayNext = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return actualResponse;
    }

    @Override
    public RemoteResponse getTags(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
        RemoteResponse actualResponse = super.getTags(addressBookName, pageNumber, resourcesPerPage, previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse getRateLimitStatus(String previousETag) {
        RemoteResponse actualResponse = super.getRateLimitStatus(previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse updatePerson(String addressBookName, int personId, CloudPerson updatedPerson, String previousETag) {
        RemoteResponse actualResponse = super.updatePerson(addressBookName, personId, updatedPerson, previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse deletePerson(String addressBookName, int personId) {
        RemoteResponse actualResponse = super.deletePerson(addressBookName, personId);
        return actualResponse;
    }

    @Override
    public RemoteResponse createTag(String addressBookName, CloudTag newTag, String previousETag) {
        RemoteResponse actualResponse = super.createTag(addressBookName, newTag, previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse editTag(String addressBookName, String oldTagName, CloudTag updatedTag, String previousETag) {
        RemoteResponse actualResponse = super.editTag(addressBookName, oldTagName, updatedTag, previousETag);
        return actualResponse;
    }

    @Override
    public RemoteResponse deleteTag(String addressBookName, String tagName) {
        RemoteResponse actualResponse = super.deleteTag(addressBookName, tagName);
        return actualResponse;
    }

    @Override
    public RemoteResponse createAddressBook(String addressBookName) {
        RemoteResponse actualResponse = super.createAddressBook(addressBookName);
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
