package address.sync;

import address.model.AddressBookWrapper;
import address.model.ContactGroup;
import address.model.Person;
import address.util.XmlHelper;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CloudSimulator {
    private static final double FAILURE_PROBABILITY = 0.1;

    private static final int MIN_DELAY_IN_SEC = 1;
    private static final int DELAY_RANGE = 5;

    private static final double MODIFY_PERSON_PROBABILITY = 0.1;
    private static final double ADD_PERSON_PROBABILITY = 0.05;
    private static final int MAX_NUM_PERSONS_TO_ADD = 2;

    private boolean isSimulateRandomChanges = false;
    private static final Random RANDOM_GENERATOR = new Random();

    public CloudSimulator(boolean isSimulateRandomChanges) {
        this.isSimulateRandomChanges = isSimulateRandomChanges;
    }

    /**
     * Gets the updated data subjected to simulated random behaviors such as random changes,
     * random delays and random failures. The data is originally obtained from a given file.
     * The data is possibly modified in each call to this method and is persisted onto the same file.
     * When failure condition occurs, this returns an empty data set.
     */
    public AddressBookWrapper getSimulatedCloudData(File cloudFile) {
        System.out.println("Simulating cloud data retrieval...");
        AddressBookWrapper modifiedData = new AddressBookWrapper();
        try {
            AddressBookWrapper data = XmlHelper.getDataFromFile(cloudFile);
            if (!this.isSimulateRandomChanges) {
                return data;
            }

            // no data could be retrieved
            if (RANDOM_GENERATOR.nextDouble() <= FAILURE_PROBABILITY) {
                System.out.println("Cloud simulator: failure occurred! Could not retrieve data");
                AddressBookWrapper wrapper = new AddressBookWrapper();
                wrapper.setPersons(new ArrayList<>());
                wrapper.setGroups(new ArrayList<>());
                return wrapper;
            }

            modifiedData = simulateDataModification(data);
            modifiedData.getPersons().addAll(simulateDataAddition());
            XmlHelper.saveToFile(cloudFile, modifiedData.getPersons(), modifiedData.getGroups());
            TimeUnit.SECONDS.sleep(RANDOM_GENERATOR.nextInt(DELAY_RANGE) + MIN_DELAY_IN_SEC);
        } catch (JAXBException e) {
            System.out.println("File not found or is not in valid xml format : " + cloudFile);
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
    public void requestChangesToCloud(File file, List<Person> people, List<ContactGroup> groups, int delay)
            throws JAXBException {
        if (file == null) return;
        List<Person> persons = people.stream().map(Person::new).collect(Collectors.toList());
        persons.forEach((p) -> p.setUpdatedAt(LocalDateTime.now()));
        XmlHelper.saveToFile(file, people, groups);
        try {
            TimeUnit.SECONDS.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<Person> simulateDataAddition() {
        List<Person> newData = new ArrayList<>();

        for (int i = 0; i < MAX_NUM_PERSONS_TO_ADD; i++) {
            if (RANDOM_GENERATOR.nextDouble() <= ADD_PERSON_PROBABILITY) {
                Person person = new Person(java.util.UUID.randomUUID().toString(),
                                           java.util.UUID.randomUUID().toString());
                System.out.println("Cloud simulator: adding " + person);
                newData.add(person);
            }
        }

        return newData;
    }

    /**
     * WARNING: MUTATES data ARGUMENT
     * TODO: currently only modifies Persons
     *
     * @param data
     * @return the (possibly) modified argument addressbookwrapper
     */
    private AddressBookWrapper simulateDataModification(AddressBookWrapper data) {
        List<Person> modifiedData = new ArrayList<>();

        // currently only modifies persons
        for (Person person : data.getPersons()) {
            if (RANDOM_GENERATOR.nextDouble() <= MODIFY_PERSON_PROBABILITY) {
                System.out.println("Cloud simulator: modifying " + person);
                person.setCity(java.util.UUID.randomUUID().toString());
                person.setStreet(java.util.UUID.randomUUID().toString());
                person.setPostalCode(RANDOM_GENERATOR.nextInt(999999));
                person.setUpdatedAt(LocalDateTime.now());
            }
            modifiedData.add(person);
        }

        data.setPersons(modifiedData);
        return data;
    }
}
