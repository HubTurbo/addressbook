package address.sync;

import address.model.Person;
import address.util.XmlHelper;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CloudSimulator {
    private static final double FAILURE_PROBABILITY = 0.1;

    private static final int MIN_DELAY_IN_SEC = 1;
    private static final int DELAY_RANGE = 5;

    private static final double MODIFY_PERSON_PROBABILITY = 0.1;
    private static final double ADD_PERSON_PROBABILITY = 0.05;
    private static final int MAX_NUM_PERSONS_TO_ADD = 2;

    private boolean isSimulateRandomChanges = false;
    private static final Random random = new Random();

    public CloudSimulator(boolean isSimulateRandomChanges) {
        this.isSimulateRandomChanges = isSimulateRandomChanges;
    }

    /**
     * Gets the updated data subjected to simulated random behaviors such as random changes,
     * random delays and random failures. The data is originally obtained from a given file.
     * The data is possibly modified in each call to this method and is persisted onto the same file.
     * When failure condition occurs, this returns an empty data set.
     */
    public List<Person> getSimulatedCloudData(File file) {
        System.out.println("Simulating cloud data retrieval...");
        List<Person> modifiedData = new ArrayList<>();
        try {
            List<Person> data = XmlHelper.getDataFromFile(file);
            if (!this.isSimulateRandomChanges) {
                return data;
            }

            if (random.nextDouble() <= FAILURE_PROBABILITY) {
                System.out.println("Cloud simulator: failure occurred!");
                return new ArrayList<>();
            }

            modifiedData = simulateDataModification(data);
            modifiedData.addAll(simulateDataAddition());
            XmlHelper.saveToFile(file, modifiedData);
            TimeUnit.SECONDS.sleep(random.nextInt(DELAY_RANGE) + MIN_DELAY_IN_SEC);
        } catch (JAXBException e) {
            System.out.println("File not found or is not in valid xml format : " + file);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return modifiedData;
    }

    /**
     * Requests the simulated cloud to update its data with the given data. This data should be
     * written to the provided mirror file
     * @param delay Duration of delay in seconds to be simulated before the request is completed
     */
    public void requestChangesToCloud(File file, List<Person> data, int delay) throws JAXBException {
        XmlHelper.saveToFile(file, data);
    }

    private List<Person> simulateDataAddition() {
        List<Person> newData = new ArrayList<>();

        for (int i = 0; i < MAX_NUM_PERSONS_TO_ADD; i++) {
            if (random.nextDouble() <= ADD_PERSON_PROBABILITY) {
                Person person = new Person(java.util.UUID.randomUUID().toString(),
                                           java.util.UUID.randomUUID().toString());
                System.out.println("Cloud simulator: adding " + person);
                newData.add(person);
            }
        }

        return newData;
    }

    private List<Person> simulateDataModification(List<Person> data) {
        List<Person> modifiedData = new ArrayList<>();

        for (Person person : data) {
            if (random.nextDouble() <= MODIFY_PERSON_PROBABILITY) {
                System.out.println("Cloud simulator: modifying " + person);
                person.setCity(java.util.UUID.randomUUID().toString());
                person.setStreet(java.util.UUID.randomUUID().toString());
                person.setPostalCode(random.nextInt(999999));
            }
            modifiedData.add(person);
        }

        return modifiedData;
    }
}
