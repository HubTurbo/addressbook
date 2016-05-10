package address.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import address.model.ContactGroup;
import address.model.Person;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PersonCardController {
    @FXML
    private VBox box;
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

    public PersonCardController(Person person) {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/PersonListCard.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        firstName.setText(person.getFirstName());
        lastName.setText(person.getLastName());
        street.setText(person.getStreet());
        postalCode.setText(Integer.toString(person.getPostalCode()));
        city.setText(person.getCity());
        birthday.setText(person.getBirthday().format(DateTimeFormatter.ISO_LOCAL_DATE));
        contactGroups.setText(getContactGroupsString(person.getContactGroups()));
    }

    private String getContactGroupsString(List<ContactGroup> contactGroups) {
        String contactGroupsString = "";
        for (int i = 0; i < contactGroups.size(); i++) {
            if (i > 0) {
                contactGroupsString += ", ";
            }
            contactGroupsString += contactGroups.get(i).getName();
        }
        return contactGroupsString;
    }

    public VBox getLayout() {
        return box;
    }
}
