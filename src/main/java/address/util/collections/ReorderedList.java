package address.util.collections;

import com.sun.javafx.collections.SourceAdapterChange;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class ReorderedList<T> extends TransformationList<T, T> {
    private ObservableList<T> mappingList;

    /**
     * Creates a new Transformation list wrapped around the source list.
     *
     * @param source the wrapped list
     */
    public ReorderedList(ObservableList<T> source) {
        super(source);
        mappingList = FXCollections.observableArrayList(source);
    }

    @Override
    protected synchronized void sourceChanged(ListChangeListener.Change<? extends T> c) {

        beginChange();
        while (c.next()) {
            if (c.wasRemoved()) {
                mappingList.removeAll(c.getRemoved());
            }

            if (c.wasAdded()) {
                mappingList.addAll(c.getAddedSubList());
            }
        }
        fireChange(new SourceAdapterChange<>(this, c));
    }

    @Override
    public synchronized int getSourceIndex(int index) {
        return this.getSource().indexOf(mappingList.get(index));
    }

    @Override
    public synchronized T get(int index) {
        return this.getSource().get(getSourceIndex(index));
    }

    @Override
    public synchronized int size() {
        return this.getSource().size();
    }

    /**
     * Moves the elements in the list.
     * Precondition: The object at destinationIndex is not in the list of toMove.
     * @param toMove The list of objects to be moved.
     * @param destinationIndex The index(before shifting) of the list where elements are to be shifted to.
     */
    public synchronized Collection<Integer> moveElements(List<T> toMove, int destinationIndex) {

        List<Integer> movedIndexes = new ArrayList<>();

        if (destinationIndex == mappingList.size()){
            mappingList.removeAll(toMove);
            mappingList.addAll(toMove);
            movedIndexes.addAll(collectMovedIndexes(toMove));
            return movedIndexes;
        }

        if (toMove.contains(mappingList.get(destinationIndex))) {
            throw new IllegalArgumentException("The object at destinationIndex is not in the list of toMove");
        }

        T destElement = mappingList.get(destinationIndex);
        mappingList.removeAll(toMove);
        mappingList.addAll(mappingList.indexOf(destElement), toMove);
        movedIndexes.addAll(collectMovedIndexes(toMove));
        return movedIndexes;
    }

    private Collection<Integer> collectMovedIndexes(List<T> toMove) {
        return toMove.stream().map(e -> mappingList.indexOf(e)).collect(Collectors.toCollection(ArrayList::new));
    }
}
