package address.model;

import address.model.datatypes.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.HashSet;
import java.util.stream.Collectors;

class VisibleAddressBook implements VisibleModel {

    final BackingAddressBook backingModel;

    final ObservableList<ViewablePerson> allPersons;
    final ObservableList<Tag> allTags; // todo change to viewabletag class

    {
        allPersons = FXCollections.observableArrayList(ExtractableObservables::extractFrom);
    }

    VisibleAddressBook(BackingAddressBook src) {
        backingModel = src;
        allTags = backingModel.getAllTags(); // change when viewabletag is implemented

        allPersons.setAll(backingModel.getAllPersons().stream()
                        .map(ViewablePerson::new)
                        .collect(Collectors.toList()));

        bindPersonsToBacking();
    }

    private void bindPersonsToBacking() {
        backingModel.getAllPersons().addListener((ListChangeListener<? super Person>) change -> {

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
        return ObservableViewablePerson.readOnlyCollectionCast(allPersons, FXCollections::observableArrayList);
    }

    @Override
    public ObservableList<ReadableViewablePerson> getAllViewablePersonsAsReadOnly() {
        return ReadableViewablePerson.readOnlyCollectionCast(allPersons, FXCollections::observableArrayList);
    }

    @Override
    public ObservableList<Tag> getAllViewableTags() {
        return allTags;
    }


}
