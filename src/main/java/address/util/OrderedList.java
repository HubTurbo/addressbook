package address.util;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import com.sun.javafx.collections.SourceAdapterChange;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

/**
 *
 */
public class OrderedList<T> extends TransformationList<T, T> {


    private ObservableList<T> orderedList;

    /**
     * Creates a new Transformation list wrapped around the source list.
     *
     * @param source the wrapped list
     */
    public OrderedList(ObservableList<T> source) {
        super(source);
        orderedList = FXCollections.observableArrayList();

    }

    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends T> c) {
        beginChange();
        while (c.next()) {
            if (c.wasAdded()) {
                orderedList.addAll(c.getAddedSubList());
                continue;
            }

            if (c.wasRemoved()) {
                orderedList.removeAll(c.getRemoved());
                continue;
            }
        }
        endChange();
        fireChange(new SourceAdapterChange<>(this, c));
    }

    @Override
    public int getSourceIndex(int index) {
        return index;
    }

    @Override
    public T get(int index) {
        return orderedList.get(index);
    }

    @Override
    public int size() {
        return orderedList.size();
    }

    public void moveElement(int from, int to) {
        if (from < to) {
            T tmpPerson = orderedList.get(from);
            orderedList.add(to, tmpPerson);
            orderedList.remove(from);
        } else if (from > to) {
            T tmpPerson = orderedList.remove(from);
            orderedList.add(to, tmpPerson);
        }
    }

}
