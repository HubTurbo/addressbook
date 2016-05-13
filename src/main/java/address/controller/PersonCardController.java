package address.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import address.model.ContactGroup;
import address.model.Person;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class PersonCardController {
    @FXML
    private GridPane gridPane;
    @FXML
    private Label firstName;
    @FXML
    private Label lastName;
    @FXML
    private Label street;
    @FXML
    private Label postalCode;
    @FXML
    private Label city;
    @FXML
    private Label birthday;
    @FXML
    private Label contactGroups;


    Person person;
    public PersonCardController(Person person) {
        this.person = person;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/PersonListCard.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize() {
        firstName.setText(person.getFirstName());
        lastName.setText(person.getLastName());
        street.setText(person.getStreet());
        postalCode.setText(Integer.toString(person.getPostalCode()));
        city.setText(person.getCity());
        birthday.setText(person.getBirthday().format(DateTimeFormatter.ISO_LOCAL_DATE));
        contactGroups.setText(getContactGroupsString(person.getContactGroups()));
    }

    private String getContactGroupsString(List<ContactGroup> contactGroups) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < contactGroups.size(); i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(contactGroups.get(i).getName());
        }
        return buffer.toString();
    }

    public GridPane getLayout() {
        return gridPane;
    }
}
