package address.controller;

import java.io.IOException;


import address.model.datatypes.Person;

import javafx.animation.FadeTransition;
import javafx.application.Platform;

import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class PersonCardController {
    @FXML
    private AnchorPane cardPane;
    @FXML
    private ImageView profileImage;
    @FXML
    private Label firstName;
    @FXML
    private Label lastName;
    @FXML
    private Label address;
    @FXML
    private Label birthday;
    @FXML
    private Label tags;

    private Person person;

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

        if (person.getGithubProfilePicUrl().length() > 0) {
            setProfileImage();
        }

        if (person.getIsDeleted()){
            Platform.runLater(() -> cardPane.setOpacity(0.1f));
        }

        double xyPositionAndRadius = profileImage.getFitHeight()/2.0;
        profileImage.setClip(new Circle(xyPositionAndRadius,xyPositionAndRadius,xyPositionAndRadius));

        firstName.textProperty().bind(person.firstNameProperty());
        lastName.textProperty().bind(person.lastNameProperty());
        address.textProperty().bind(new StringBinding(){
            {
                bind(person.streetProperty());
                bind(person.postalCodeProperty());
                bind(person.cityProperty());
            }
            @Override
            protected String computeValue() {
                StringBuilder sb = new StringBuilder();
                if (person.getStreet().length() > 0){
                    sb.append(person.getStreet() + "\n");
                }
                if(person.getCity().length() > 0){
                    sb.append(person.getCity() + "\n");
                }
                if (person.getPostalCode().length() > 0){
                    sb.append(person.getPostalCode());
                }
                return sb.toString();
            }
        });
        birthday.textProperty().bind(new StringBinding(){
            {
                bind(person.birthdayProperty()); //Bind property at instance initializer
            }

            @Override
            protected String computeValue() {
                if (person.birthdayString().length() > 0){
                    return "DOB: " + person.birthdayString();
                }
                return "";
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
        person.isDeletedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue == true){
                    handleDeletedPerson();
                }
            }
        });
        person.githubProfilePicUrlProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.length() > 0){
                    setProfileImage();
                }
            }
        });
    }

    /**
     * Asynchronously sets the profile image to the image view.
     * Involves making an internet connection with the image hosting server.
     */
    private void setProfileImage() {
        new Thread(() -> profileImage.setImage(new Image(person.getGithubProfilePicUrl()))).start();
    }

    public void handleDeletedPerson(){
        Platform.runLater(() -> {
            FadeTransition ft = new FadeTransition(Duration.millis(1000), cardPane);
            ft.setFromValue(1.0);
            ft.setToValue(0.1);
            ft.setCycleCount(1);
            ft.play();
        });
    }

    public AnchorPane getLayout() {
        return cardPane;
    }
}
