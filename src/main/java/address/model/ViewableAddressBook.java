package address.model;

import address.model.datatypes.ObservableViewablePerson;
import address.model.datatypes.ViewablePerson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class ViewableAddressBook {

    private final ObservableList<ViewablePerson> allPersons;
    private final FilteredList<ViewablePerson> filteredPerson;

    public ViewableAddressBook(AddressBook source) {
        allPersons = FXCollections.observableArrayList((Collection<ViewablePerson>)
                source.getPersons().stream()
                        .map(ViewablePerson::new)
                        .collect(Collectors.toCollection(ArrayList::new)));

        filteredPerson = new FilteredList<>(allPersons);
    }

    ObservableList<ViewablePerson> getAllPersons() {
        return allPersons;
    }

    ObservableList<ViewablePerson> getFilteredPersons() {
        return filteredPerson;
    }

    
}
