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
    private double FAILURE_PROBABILITY = 0.1;

    private int MIN_DELAY_IN_SEC = 1;
    private int DELAY_RANGE = 5;

    private double MODIFY_PERSON_PROBABILITY = 0.1;
    private double ADD_PERSON_PROBABILITY = 0.05;
    private int MAX_NUM_PERSONS_TO_ADD = 2;

    private Random random = new Random();
    private final File file;

    public CloudSimulator(File file) {
        this.file = file;
    }

    /**
     * Gets the updated data subjected to simulated random behaviors such as random changes,
     * random delays and random failures. The data is originally obtained from a given file.
     * The data is possibly modified in each call to this method and is persisted onto the same file.
     * When failure condition occurs, this returns an empty data set.
     */
    public List<Person> getSimulatedCloudData() {
        System.out.print("Simulating cloud data retrieval...");

        if (random.nextDouble() <= FAILURE_PROBABILITY) {
            System.out.println("Cloud simulator: failure occurred!");
            return new ArrayList<>();
        }

        List<Person> modifiedData = new ArrayList<>();
        try {
            List<Person> data = XmlHelper.getDataFromFile(this.file);
            modifiedData = simulateDataModification(data);
            modifiedData.addAll(simulateDataAddition());
            XmlHelper.saveToFile(this.file, modifiedData);
            TimeUnit.SECONDS.sleep(random.nextInt(DELAY_RANGE) + MIN_DELAY_IN_SEC);
        } catch (JAXBException e) {
            System.out.println("File not found or is not in valid xml format : " + file);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return modifiedData;
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
