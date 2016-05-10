package address.model;

import address.events.EventManager;
import address.events.FilterCommittedEvent;
import address.events.LocalModelSyncedEvent;
import address.events.NewMirrorDataEvent;
import address.events.*;

import com.google.common.eventbus.Subscribe;

import address.parser.ParseException;
import address.parser.Parser;
import address.parser.expr.Expr;
import address.parser.expr.PredExpr;
import address.parser.qualifier.TrueQualifier;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager {

    /**
     * The data as an observable list of Persons.
     */
    private final ObservableList<Person> personData = FXCollections.observableArrayList();
    private final FilteredList<Person> filteredPersonData = new FilteredList<>(personData);
    private final List<ContactGroup> contactGroups = new ArrayList<>();

    /**
     * @param initialPersons Initial persons to populate the model.
     * @param initialGroups Initial groups to populate the model.
     */
    public ModelManager(List<Person> initialPersons, List<ContactGroup> initialGroups) {
        if (initialPersons != null || initialGroups != null) {
            System.out.println("Data found.");
            System.out.println("Persons found : " + initialPersons.size());
            personData.addAll(initialPersons);
            System.out.println("Groups found : " + initialGroups.size());
            contactGroups.addAll(initialGroups);

        } else {
            // Add some sample data
            populateDummyData();
        }

        //Listen to any changed to person data and raise an event
        //Note: this will not catch edits to Person objects
        personData.addListener(
                (ListChangeListener<? super Person>) (change) ->
                        EventManager.getInstance().post(new LocalModelChangedEvent(personData, contactGroups)));

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

        contactGroups.add(new ContactGroup("relatives"));
        contactGroups.add(new ContactGroup("friends"));
    }

    /**
     * Returns the data as an observable list of Persons.
     * @return
     */
    public ObservableList<Person> getPersonData() {
        return filteredPersonData;
    }

    /**
     * Returns the contact groups as a list.
     * @return
     */
    public List<ContactGroup> getContactGroups() {
        return contactGroups;
    }

    /**
     * Adds new data to existing data.
     * If a Person in the new data has the same
     * first name as an existing Person, the older one will be kept.
     * @param data
     */
    public synchronized void addNewData(AddressBookWrapper data) {
        System.out.println("Attempting to add a persons list of size " + data.getPersons().size());

        //TODO: change to use streams instead
        for (Person p : data.getPersons()) {
            Optional<Person> storedPerson = getPerson(p);
            if (storedPerson.isPresent()) {
                storedPerson.get().update(p);
            } else {
                personData.add(p);
                System.out.println("New person data added " + p);
            }
        }

        System.out.println("Attempting to add a groups list of size " + data.getGroups().size());

        //TODO: change to use streams instead
        for (ContactGroup g : data.getGroups()) {
            Optional<ContactGroup> storedGroup = getGroup(g);
            if (storedGroup.isPresent()) {
                storedGroup.get().update(g);
            } else {
                contactGroups.add(g);
                System.out.println("New group data added " + g);
            }
        }

        EventManager.getInstance().post(new LocalModelSyncedEvent(personData, contactGroups));
    }

    private Optional<Person> getPerson(Person person) {
        for (Person p : personData) {
            if (p.equals(person)) {
                return Optional.of(p);
            }
        }

        return Optional.empty();
    }

    private Optional<ContactGroup> getGroup(ContactGroup group) {
        for (ContactGroup g : contactGroups) {
            if (g.equals(group)) {
                return Optional.of(g);
            }
        }

        return Optional.empty();
    }

    /**
     * Updates the details of a Person object. Updates to Person objects should be
     * done through this method to ensure the proper events are raised to indicate
     * a change to the model.
     * @param original The Person object to be changed.
     * @param updated The temporary Person object containing new values.
     */
    public synchronized void updatePerson(Person original, Person updated){
        original.update(updated);
        EventManager.getInstance().post(new LocalModelChangedEvent(personData, contactGroups));
    }

    /**
     * Deletes the person from the model.
     * @param personToDelete
     */
    public synchronized void deletePerson(Person personToDelete){
        personData.remove(personToDelete);
    }

    /**
     * Adds a person to the model
     * @param personToAdd
     */
    public synchronized void addPerson(Person personToAdd) {
        personData.add(personToAdd);
    }

    @Subscribe
    private void handleNewMirrorDataEvent(NewMirrorDataEvent nde){
        addNewData(nde.data);
    }

    @Subscribe
    private void handleFilterCommittedEvent(FilterCommittedEvent fce) {

        if (fce.filter.isEmpty()) {
            filteredPersonData.setPredicate(new PredExpr(new TrueQualifier())::satisfies);
            EventManager.getInstance().post(new FilterSuccessEvent());
            return;
        }

        Expr filterExpression;
        try {
            filterExpression = Parser.parse(fce.filter);
        } catch (ParseException e) {
            EventManager.getInstance().post(new FilterParseErrorEvent(e.getLocalizedMessage()));
            return;
        }

        filteredPersonData.setPredicate(filterExpression::satisfies);
        EventManager.getInstance().post(new FilterSuccessEvent());
    }

    /**
     * Clears existing model and replaces with the provided new data.
     * @param newData
     */
    public void resetData(List<Person> newData, List<ContactGroup> newGroups) {
        personData.clear();
        personData.addAll(newData);

        contactGroups.clear();
        contactGroups.addAll(newGroups);
    }
}
