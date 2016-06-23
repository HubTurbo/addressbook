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

        fireRemoveChanges(filteredList, newFilteredList);
        fireAddChanges(filteredList, newFilteredList);

    }

    private void fireAddChanges(ObservableList<E> oldList, ObservableList<E> newList) {
        ObservableList<E> addedList = sourceCopy.stream()
                .filter(e -> !oldList.contains(e))
                .filter(newList::contains)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));


        filteredList = newList;
                addedList.forEach(e -> {
                    beginChange();
                    System.out.println("Adding to filtered list since filter has changed: " + e + " Index: " + newList.indexOf(e));
                    nextAdd(newList.indexOf(e), newList.indexOf(e) + 1);
                    endChange();
                });
    }

    private void fireRemoveChanges(ObservableList<E> oldList, ObservableList<E> newList) {
        sourceCopy.stream()
                .filter(e -> !newList.contains(e))
                .filter(oldList::contains)
                .forEach(e -> {
                    beginChange();
                    System.out.println("Removing from filtered list since filter has changed: " + e + " Index: " + oldList.indexOf(e));
                    nextRemove(oldList.indexOf(e), e);
                    endChange();
                });
    }

    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends E> c) {
        while (c.next()) {
            if (c.wasAdded() || c.wasRemoved()) {
                sourceCopy.addAll(c.getAddedSubList());
                c.getAddedSubList().stream()
                        .filter(predicate)
                        .forEach(e -> {
                            filteredList.add(e);
                            beginChange();
                            System.out.println("Adding to filtered list since source has changed: " + e);
                            nextAdd(filteredList.indexOf(e), filteredList.indexOf(e) + 1);
                            endChange();
                        });


                sourceCopy.removeAll(c.getRemoved());
                c.getRemoved().stream()
                        .filter(predicate)
                        .forEach(e -> {
                            filteredList.remove(e);
                            beginChange();

                            System.out.println("Removing from filtered list since source has changed: " + e);
                            nextRemove(filteredList.indexOf(e), e);
                            endChange();
                        });
            }
        }
    }

    @Override
    public int getSourceIndex(int index) {
        return sourceCopy.indexOf(filteredList.get(index));
    }
}
