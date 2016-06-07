package address.model;

import address.model.datatypes.ExtractableObservables;
import address.model.datatypes.Person;
import address.model.datatypes.Tag;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 */
class BackingAddressBook implements Model {

    private final ObservableList<Person> allPersons;
    private final ObservableList<Tag> allTags;

    {
        allPersons = FXCollections.observableArrayList(ExtractableObservables::extractFrom);
        allTags = FXCollections.observableArrayList(ExtractableObservables::extractFrom);
    }

    BackingAddressBook(AddressBook src) {
        resetData(src);
    }

    public VisibleAddressBook createVisibleAddressBook() {
        return new VisibleAddressBook(this);
    }

    public AddressBook toAddressBook() {
        return new AddressBook(allPersons, allTags);
    }

    @Override
    public ObservableList<Person> getAllPersons() {
        return allPersons;
    }

    @Override
    public ObservableList<Tag> getAllTags() {
        return allTags;
    }


    /**
     * Clears existing model and replaces with the provided new data.
     */
    void resetData(AddressBook newData) {
        allPersons.setAll(newData.getPersons());
        allTags.setAll(newData.getTags());
    }

    /**
     * Clears existing model. Same effect as calling {@link #resetData(AddressBook)} with an empty {@code Addressbook}
     */
    void clearModel() {
        allPersons.clear();
        allTags.clear();
    }

}
