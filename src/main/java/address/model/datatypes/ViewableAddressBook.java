package address.model.datatypes;

import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.person.ViewablePerson;
import address.model.datatypes.tag.Tag;
import address.util.collections.UnmodifiableObservableList;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class ViewableAddressBook implements ReadOnlyViewableAddressBook {

    private final AddressBook backingModel;

    private final ObservableList<ViewablePerson> persons;
    private final ObservableList<Tag> tags; // todo change to viewabletag class

    {
        persons = FXCollections.observableArrayList(ExtractableObservables::extractFrom);
    }

    ViewableAddressBook(AddressBook src) {
        backingModel = src;
        tags = backingModel.getTags(); // change when viewabletag is implemented

        persons.setAll(backingModel.getPersons().stream()
                        .map(ViewablePerson::new)
                        .collect(Collectors.toList()));

        bindPersonsToBacking();
    }

    private void bindPersonsToBacking() {
        backingModel.getPersons().addListener((ListChangeListener<? super Person>) change -> {

            // ignore permutations (order doesn't matter) and updates (Viewable wrapper handles it)
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    // removed
                    persons.removeAll(change.getRemoved().stream()
                            // remove map when person ID implemented and VP-P equals comparison is implemented.
                            .map(ViewablePerson::new).collect(Collectors.toCollection(HashSet::new)));
                    // newly added
                    persons.addAll(change.getAddedSubList().stream()
                            .map(ViewablePerson::new).collect(Collectors.toList()));
                }
            }

        });
    }

    public ObservableList<ViewablePerson> getPersons() {
        return persons;
    }

    public ObservableList<Tag> getTags() {
        return tags;
    }

    @Override
    public ObservableList<ReadOnlyViewablePerson> getAllViewablePersonsReadOnly() {
        return new UnmodifiableObservableList<>(persons);
    }

    @Override
    public ObservableList<Tag> getAllViewableTagsReadOnly() {
        return new UnmodifiableObservableList<>(tags);
    }

    @Override
    public ObservableList<ReadOnlyPerson> getAllPersonsReadOnly() {
        return new UnmodifiableObservableList<>(persons);
    }

    @Override
    public ObservableList<Tag> getAllTagsReadOnly() {
        return new UnmodifiableObservableList<>(tags);
    }

    public Optional<ViewablePerson> findPerson(ReadOnlyPerson toFind) {
        for (ViewablePerson p : persons) {
            if (p.equals(toFind)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }
}
