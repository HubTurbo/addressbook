package address.util;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * A wrapper class to contain the move-able(able to move items between different indexes) copy of the read-only person model.
 */
public class MoveableList {

    private ObservableList<ReadOnlyViewablePerson> actualList;
    private ObservableList<ReadOnlyViewablePerson> displayedList;

    public MoveableList(ObservableList<ReadOnlyViewablePerson> actualList) {
        displayedList = FXCollections.observableArrayList();
        this.actualList = actualList;
        this.actualList.addListener((ListChangeListener<ReadOnlyViewablePerson>) c -> {
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
        });
        displayedList.addAll(actualList);
    }

    /**
     * Move element between indexes in the list.
     * @param from
     * @param to
     */
    public void moveElement(int from, int to){
        displayedList.add(to, displayedList.remove(from));
    }

    /**
     * Gets the observableList of this MoveableList.
     * @return
     */
    public ObservableList<ReadOnlyViewablePerson> getDisplayedList() {
        return displayedList;
    }

}
