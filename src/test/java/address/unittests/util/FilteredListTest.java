package address.unittests.util;

import address.util.FilteredList;
import address.util.StringUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilteredListTest {

    @Test
    public void stringList_setFiltered_correctFilteredList() {
        ObservableList<String> listOfStrings = FXCollections.observableArrayList();
        listOfStrings.addAll("Apple", "Orange", "Pear", "Watermelon", "Strawberry", "Blueberry", "Cranberry");
        FilteredList<String> filteredList = new FilteredList<>(listOfStrings);
        assertEquals(7, filteredList.size());
        filteredList.setPredicate(string -> StringUtil.containsIgnoreCase(string, "berry"));
        assertEquals(3, filteredList.size());
    }

    @Test
    public void stringList_changeFilter_correctFilteredList() {
        ObservableList<String> listOfStrings = FXCollections.observableArrayList();
        listOfStrings.addAll("Apple", "Orange", "Pear", "Watermelon", "Strawberry", "Blueberry", "Cranberry");
        FilteredList<String> filteredList = new FilteredList<>(listOfStrings);
        assertEquals(7, filteredList.size());
        filteredList.setPredicate(string -> StringUtil.containsIgnoreCase(string, "berry"));
        assertEquals(3, filteredList.size());
        filteredList.setPredicate(string -> StringUtil.containsIgnoreCase(string, "a"));
        assertEquals(6, filteredList.size());
    }
}
