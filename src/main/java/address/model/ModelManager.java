package address.model;

import address.events.EventManager;
import address.events.LocalModelChangedEvent;
import address.events.NewMirrorDataEvent;
import com.google.common.eventbus.Subscribe;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.List;

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

        //Listen to any changed to person data and raise an event
        //Note: this will not catch edits to Person objects
        personData.addListener(
                (ListChangeListener<? super Person>) (change) ->
                        EventManager.getInstance().post(new LocalModelChangedEvent(personData)));

        //Register for general events relevant to data manager
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
            if(!personData.contains(p)){
                personData.add(p);
                System.out.println("New data added " + p);
            }
        }
    }

    /**
     * Updates the details of a Person object. Updates to Person objects should be
     * done through this method to ensure the proper events are raised to indicate
     * a change to the model.
     * @param original The Person object to be changed.
     * @param updated The temporary Person object containing new values.
     */
    public void updatePerson(Person original, Person updated){
        original.update(updated);
        EventManager.getInstance().post(new LocalModelChangedEvent(personData));
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
