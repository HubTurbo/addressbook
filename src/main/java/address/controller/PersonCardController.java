package address.controller;

import java.io.IOException;
import java.util.List;

import address.model.ContactGroup;
import address.model.Person;
import javafx.beans.binding.StringBinding;
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
        firstName.textProperty().bind(person.firstNameProperty());
        lastName.textProperty().bind(person.lastNameProperty());
        street.textProperty().bind(person.streetProperty());
        postalCode.textProperty().bind(person.postalCodeProperty());
        city.textProperty().bind(person.cityProperty());
        birthday.textProperty().bind(new StringBinding(){
            {
                bind(person.birthdayProperty()); //Bind property at instance initializer
            }

            @Override
            protected String computeValue() {
                return person.birthdayString();
            }
        });
        contactGroups.textProperty().bind(new StringBinding(){
            {
                bind(person.getContactGroups()); //Bind property at instance initializer
            }

            @Override
            protected String computeValue() {
                return person.contactGroupsString();
            }
        });
    }

    public GridPane getLayout() {
        return gridPane;
    }
}
