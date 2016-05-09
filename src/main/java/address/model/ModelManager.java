package address.model;

import address.events.EventManager;
import address.events.NewMirrorDataEvent;
import com.google.common.eventbus.Subscribe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager {

    /**
     * The data as an observable list of Persons.
     */
    private ObservableList<Person> personData = FXCollections.observableArrayList();

    /**
     * @param initialData Initial data to populate the model. If the list is
     *                    empty, some dummy data will be added instead.
     */
    public ModelManager(List<Person> initialData) {
        if (initialData != null) {
            System.out.println("Persons found : " + initialData.size());
            personData.addAll(initialData);
        } else {
            // Add some sample data
            populateDummyData();
        }
        EventManager.getInstance().registerHandler(this);
    }

    protected void populateDummyData() {
        personData.add(new Person("Hans", "Muster"));
        personData.add(new Person("Ruth", "Mueller"));
        personData.add(new Person("Heinz", "Kurz"));
        personData.add(new Person("Cornelia", "Meier"));
        personData.add(new Person("Werner", "Meyer"));
        personData.add(new Person("Lydia", "Kunz"));
        personData.add(new Person("Anna", "Best"));
        personData.add(new Person("Stefan", "Meier"));
        personData.add(new Person("Martin", "Mueller"));
    }

    /**
     * Returns the data as an observable list of Persons.
     * @return
     */
    public ObservableList<Person> getPersonData() {
        return personData;
    }

    /**
     * Adds new data to existing data. If a Person in the new data has the same
     * first name as an existing Person, the older one will be kept.
     * @param newData
     */
    public void addNewData(List<Person> newData){
        System.out.println("Attempting to add a list of size " + newData.size());

        //TODO: change to use streams instead
        for(Person p: newData){
            Optional<Person> storedPerson = getPerson(p);
            if (storedPerson.isPresent()){
                storedPerson.get().updateWith(p);
            } else {
                personData.add(p);
                System.out.println("New data added " + p);
            }
        }
    }

    private Optional<Person> getPerson(Person person) {
        for (Person p : personData) {
            if (p.equals(person)) {
                return Optional.of(p);
            }
        }

        return Optional.empty();
    }

    @Subscribe
    private void handleNewMirrorDataEvent(NewMirrorDataEvent nde){
        addNewData(nde.personData);
    }

    /**
     * Clears existing model and replaces with the provided new data.
     * @param newData
     */
    public void resetData(List<Person> newData) {
        personData.clear();
        personData.addAll(newData);
    }
}
