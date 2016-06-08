package address.controller;

import java.io.IOException;

import address.model.datatypes.ObservableViewablePerson;
import address.model.datatypes.Person;
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
    private Label tags;

    private ObservableViewablePerson person;

    public PersonCardController(ObservableViewablePerson person) {
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
        tags.textProperty().bind(new StringBinding(){
            {
                bind(person.getTags()); //Bind property at instance initializer
            }

            @Override
            protected String computeValue() {
                return person.tagsString();
            }
        });
    }

    public GridPane getLayout() {
        return gridPane;
    }
}
