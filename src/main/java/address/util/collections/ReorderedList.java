package address.util.collections;

import address.util.AppLogger;
import address.util.LoggerManager;
import com.sun.javafx.collections.SourceAdapterChange;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class ReorderedList<T> extends TransformationList<T, T> {
    private ObservableList<T> mappingList;

    private static AppLogger logger = LoggerManager.getLogger(ReorderedList.class);

    /**
     * Creates a new Transformation list wrapped around the source list.
     *
     * @param source the wrapped list
     */
    public ReorderedList(ObservableList<T> source) {
        super(source);
        mappingList = FXCollections.observableArrayList(source);
        logger.info("Mapping list created.");
    }

    @Override
    protected synchronized void sourceChanged(ListChangeListener.Change<? extends T> c) {
        logger.info("Source changed detected: Begin changing of mapping list");
        beginChange();
        while (c.next()) {
            if (c.wasRemoved()) {
                logger.info("c.wasRemoved:" + Arrays.toString(c.getRemoved().toArray()));
                mappingList.removeAll(c.getRemoved());
            }

            if (c.wasAdded()) {
                logger.info("c.wasAdded:" + Arrays.toString(c.getAddedSubList().toArray()));
                mappingList.addAll(c.getAddedSubList());
            }
        }
        endChange();
        logger.info("Source changed detected: End changing of mapping list");
        fireChange(new SourceAdapterChange<>(this, c));
        logger.info("Source changed detected: Fire Changes");
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

        List<Integer> movedIndices = new ArrayList<>();

        //Insert it at the back of the list.
        if (destinationIndex == mappingList.size()){
            mappingList.removeAll(toMove);
            mappingList.addAll(toMove);
            movedIndices.addAll(collectMovedIndices(toMove));
            return movedIndices;
        }

        if (toMove.contains(mappingList.get(destinationIndex))) {
            throw new IllegalArgumentException("The object at destinationIndex is not in the list of toMove");
        }

        //Keep an object reference for insertion based on this reference.
        T destElement = mappingList.get(destinationIndex);
        mappingList.removeAll(toMove);
        mappingList.addAll(mappingList.indexOf(destElement), toMove);
        movedIndices.addAll(collectMovedIndices(toMove));
        return movedIndices;
    }

    private Collection<Integer> collectMovedIndices(List<T> toMove) {
        return toMove.stream().map(mappingList::indexOf).collect(Collectors.toCollection(ArrayList::new));
    }
}
