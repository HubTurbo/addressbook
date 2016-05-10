package address.ui;

import address.controller.PersonCardController;
import address.model.Person;
import javafx.scene.control.ListCell;

public class PersonListViewCell extends ListCell<Person> {

    @Override
    public void updateItem(Person person, boolean empty) {
        super.updateItem(person, empty);
        if (person != null) {

            setGraphic(new PersonCardController(person).getLayout());
        }
    }
}

