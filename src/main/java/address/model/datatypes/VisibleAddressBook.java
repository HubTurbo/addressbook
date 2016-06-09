package address.model.datatypes;

import address.model.VisibleModel;
import address.model.datatypes.person.ObservableViewablePerson;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadableViewablePerson;
import address.model.datatypes.person.ViewablePerson;
import address.model.datatypes.tag.Tag;
import address.util.collections.UnmodifiableObservableList;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.HashSet;
import java.util.stream.Collectors;

public class VisibleAddressBook implements VisibleModel {

    private final AddressBook backingModel;

    private final ObservableList<ViewablePerson> allPersons;
    private final ObservableList<Tag> allTags; // todo change to viewabletag class

    {
        allPersons = FXCollections.observableArrayList(ExtractableObservables::extractFrom);
    }

    VisibleAddressBook(AddressBook src) {
        backingModel = src;
        allTags = backingModel.getTags(); // change when viewabletag is implemented

        allPersons.setAll(backingModel.getPersons().stream()
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
                    allPersons.removeAll(change.getRemoved().stream()
                            // remove map when person ID implemented and VP-P equals comparison is implemented.
                            .map(ViewablePerson::new).collect(Collectors.toCollection(HashSet::new)));
                    // newly added
                    allPersons.addAll(change.getAddedSubList().stream()
                            .map(ViewablePerson::new).collect(Collectors.toList()));
                }
            }

        });
    }

    @Override
    public ObservableList<ObservableViewablePerson> getAllViewablePersonsAsObservable() {
        return new UnmodifiableObservableList<>(allPersons);
    }

    @Override
    public ObservableList<ReadableViewablePerson> getAllViewablePersonsAsReadOnly() {
        return new UnmodifiableObservableList<>(allPersons);
    }

    @Override
    public ObservableList<Tag> getAllViewableTags() {
        return new UnmodifiableObservableList<>(allTags);
    }


}
