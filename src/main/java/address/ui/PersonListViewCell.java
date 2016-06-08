package address.ui;

import address.controller.PersonCardController;
import address.model.datatypes.ObservableViewablePerson;
import address.model.datatypes.Person;
import javafx.scene.control.ListCell;

public class PersonListViewCell extends ListCell<ObservableViewablePerson> {

    @Override
    public void updateItem(ObservableViewablePerson person, boolean empty) {
        super.updateItem(person, empty);
        if (empty || person == null) {
            setGraphic(null);
            setText(null);
        } else {
            setGraphic(new PersonCardController(person).getLayout());
        }
    }
}

