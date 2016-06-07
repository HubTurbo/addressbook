package address.model;

import address.model.datatypes.ExtractableObservables;
import address.model.datatypes.Person;
import address.model.datatypes.Tag;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 */
class BackingAddressBook {

    private final ObservableList<Person> allPersons;
    private final ObservableList<Tag> allTags;

    {
        allPersons = FXCollections.observableArrayList(ExtractableObservables::extractFrom);
        allTags = FXCollections.observableArrayList(ExtractableObservables::extractFrom);
    }

    BackingAddressBook(AddressBook src) {
        allPersons.setAll(src.getPersons());
        allTags.setAll(src.getTags());
    }

    VisibleAddressBook createVisibleAddressBook() {
        return new VisibleAddressBook(this);
    }

    AddressBook toAddressBook() {
        return new AddressBook(allPersons, allTags);
    }

    ObservableList<Person> getAllPersons() {
        return allPersons;
    }

    ObservableList<Tag> getAllTags() {
        return allTags;
    }


}
