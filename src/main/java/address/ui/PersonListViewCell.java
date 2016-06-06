package address.ui;

import address.controller.PersonCardController;
import address.model.datatypes.Person;

import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;

public class PersonListViewCell extends ListCell<Person> {

    @Override
    public void updateItem(Person person, boolean empty) {
        super.updateItem(person, empty);

        if (empty || person == null) {
            setGraphic(null);
            setText(null);
            this.setVisible(false);
        }
        else
        {
            AnchorPane pane = new PersonCardController(person).getLayout();
            setGraphic(pane);
           pane.prefHeightProperty().bind(this.prefHeightProperty());
            pane.prefWidthProperty().bind(this.widthProperty());
        }
    }
}

