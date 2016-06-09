package address.ui;

import address.controller.PersonCardController;
import address.model.datatypes.person.ReadOnlyViewablePerson;

import javafx.scene.control.ListCell;

public class PersonListViewCell extends ListCell<ReadOnlyViewablePerson> {

    @Override
    public void updateItem(ReadOnlyViewablePerson person, boolean empty) {
        super.updateItem(person, empty);

        if (empty || person == null) {
            setGraphic(null);
            setText(null);
        } else {
            setGraphic(new PersonCardController(person).getLayout());
        }
    }
}
