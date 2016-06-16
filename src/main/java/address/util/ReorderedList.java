package address.util;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import com.sun.corba.se.impl.orbutil.concurrent.Mutex;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * A wrapper class to contain the move-able(able to move items between different indexes) copy of the read-only person model.
 */
public class ReorderedList {

    private ObservableList<ReadOnlyViewablePerson> actualList;
    private ObservableList<ReadOnlyViewablePerson> displayedList;

    private Mutex mutex = new Mutex();

    public ReorderedList(ObservableList<ReadOnlyViewablePerson> actualList) {
        displayedList = FXCollections.observableArrayList();
        this.actualList = actualList;
        this.actualList.addListener((ListChangeListener<ReadOnlyViewablePerson>) c -> {

            try {
                mutex.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
            mutex.release();
        });
        displayedList.addAll(actualList);
    }

    /**
     * Move element between indexes in the list.
     * @param from
     * @param to
     */
    public void moveElement(int from, int to){
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        displayedList.add(to, displayedList.remove(from));
        mutex.release();
    }

    /**
     * Gets the observableList of this ReorderedList.
     * @return
     */
    public ObservableList<ReadOnlyViewablePerson> getDisplayedList() {
        return displayedList;
    }

}
