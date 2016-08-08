package address.ui;

import address.controller.PersonCardController;
import address.model.datatypes.person.ReadOnlyPerson;
import javafx.scene.control.ListCell;

public class PersonListViewCell extends ListCell<ReadOnlyPerson> {

    public PersonListViewCell() {

    }

    @Override
    protected void updateItem(ReadOnlyPerson person, boolean empty) {
        super.updateItem(person, empty);

        if (empty || person == null) {
            setGraphic(null);
            setText(null);
        } else {
            setGraphic(new PersonCardController(person).getLayout());
        }
    }
}
