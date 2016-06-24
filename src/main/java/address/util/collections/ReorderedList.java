package address.util.collections;

import com.sun.javafx.collections.SourceAdapterChange;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

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
     * Moves the element in the list
     * @param from The index(before shifting occurred) of the element to be shifted(before shift).
     * @param to The index(before shifting occurred) of the list where element is to be shifted to.
     */
    public synchronized void moveElement(int from, int to) {

        if (from < to) {
            //Element to be shifted is below the index where the element need to be shifted to.
            //Removing the element first will shift every element down.
            //Therefore, only remove the element after shifting finished.
            T tmpPerson = mappingList.get(from);
            mappingList.add(to, tmpPerson);
            mappingList.remove(from);
        } else if (from > to) {
            T tmpPerson = mappingList.remove(from);
            mappingList.add(to, tmpPerson);
        }
    }

}
