package address.util;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * A wrapper class to contain the move-able(able to move items between
 * different indexes) copy of the read-only person model.
 */
public class ReorderedList {

    private ObservableList<ReadOnlyViewablePerson> actualList;
    private ObservableList<ReadOnlyViewablePerson> displayedList;

    public ReorderedList(ObservableList<ReadOnlyViewablePerson> actualList) {
        displayedList = FXCollections.observableArrayList();
        this.actualList = actualList;
        this.actualList.addListener((ListChangeListener<ReadOnlyViewablePerson>) c -> {
            synchronized (this) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        displayedList.addAll(c.getAddedSubList());
                        continue;
                    }

                    if (c.wasRemoved()) {
                        displayedList.removeAll(c.getRemoved());
                        continue;
                    }
                }
            }
        });
        displayedList.addAll(actualList);
    }

    /**
     * Move element between indexes in the list.
     * @param from
     * @param to
     */
    public void moveElement(int from, int to){
        synchronized (this) {
            System.out.println("Drag from " + from + " to " + to);
            if (from < to) {
                ReadOnlyViewablePerson tmpPerson = displayedList.get(from);
                displayedList.add(to, tmpPerson);
                displayedList.remove(from);
            } else if (from > to) {
                ReadOnlyViewablePerson tmpPerson = displayedList.remove(from);
                displayedList.add(to, tmpPerson);
            }
        }
    }

    /**
     * Gets the observableList of this ReorderedList.
     * @return
     */
    public ObservableList<ReadOnlyViewablePerson> getDisplayedList() {
        return displayedList;
    }
}
