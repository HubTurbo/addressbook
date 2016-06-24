package address.util;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilteredList<E> extends TransformationList<E, E> {
    private ObservableList<E> sourceCopy;
    private Predicate<E> predicate;
    private ObservableList<E> filteredList;

    public FilteredList(ObservableList<E> source, Predicate<E> predicate) {
        this(source);
        setPredicate(predicate);
    }

    public FilteredList(ObservableList<E> source) {
        super(source);
        this.sourceCopy = FXCollections.observableArrayList(source);
        this.filteredList = source;
    }

    @Override
    public E get(int index) {
        return filteredList.get(index);
    }

    @Override
    public int size() {
        return filteredList.size();
    }

    private ObservableList<E> filterList(Predicate<E> predicate) {
        return sourceCopy.stream()
                .filter(predicate)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public void setPredicate(Predicate<E> predicate) {
        this.predicate = predicate;
        ObservableList<E> newFilteredList = filterList(predicate);

        ObservableList<E> removedList = getRemovedList(filteredList, newFilteredList);
        ObservableList<E> addedList = getAddedList(filteredList, newFilteredList);

        fireRemoveChanges(removedList, filteredList);
        filteredList = newFilteredList;
        fireAddChanges(addedList, newFilteredList);
    }

    private ObservableList<E> getAddedList(ObservableList<E> oldList, ObservableList<E> newList) {
        return sourceCopy.stream()
                .filter(e -> !oldList.contains(e))
                .filter(newList::contains)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private ObservableList<E> getRemovedList(ObservableList<E> oldList, ObservableList<E> newList) {
        return sourceCopy.stream()
                .filter(e -> !newList.contains(e))
                .filter(oldList::contains)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Fires add change notifications for any observers
     *
     * @param addedList list of added elements
     * @param updatedList list after the add changes have been made
     */
    private void fireAddChanges(ObservableList<E> addedList, ObservableList<E> updatedList) {
        beginChange();
        addedList.forEach(e -> nextAdd(updatedList.indexOf(e), updatedList.indexOf(e) + 1));
        endChange();
    }

    /**
     * Fires remove change notifications for any observers
     *
     * @param removedList list of removed elements
     * @param originalList list before the remove changes have been made
     */
    private void fireRemoveChanges(ObservableList<E> removedList, ObservableList<E> originalList) {
        beginChange();
        removedList.forEach(e -> nextRemove(originalList.indexOf(e), e));
        endChange();
    }

    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends E> c) {
        beginChange();
        while (c.next()) {
            if (c.wasAdded() || c.wasRemoved()) {
                sourceCopy.addAll(c.getAddedSubList());
                c.getAddedSubList().stream()
                        .filter(predicate)
                        .forEach(e -> {
                            // added to the end of the list to preserve order of old data
                            filteredList.add(e);
                            nextAdd(filteredList.indexOf(e), filteredList.indexOf(e) + 1);
                        });


                sourceCopy.removeAll(c.getRemoved());
                c.getRemoved().stream()
                        .filter(predicate)
                        .forEach(e -> {
                            filteredList.remove(e);
                            nextRemove(filteredList.indexOf(e), e);
                        });
            }
        }
        endChange();
    }

    @Override
    public int getSourceIndex(int index) {
        return sourceCopy.indexOf(filteredList.get(index));
    }
}
