package address.controller;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.Consumer;


import address.image.ImageManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;

import javafx.animation.FadeTransition;
import javafx.application.Platform;

import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class PersonCardController {
    @FXML
    private HBox cardPane;
    @FXML
    private ImageView profileImage;
    @FXML
    private Label idLabel;
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

    private ReadOnlyViewablePerson person;

    public PersonCardController(ReadOnlyViewablePerson person) {
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

        if (person.getGithubUserName().length() > 0) {
            setProfileImage();
        }

        if (person.isDeleted()){
            Platform.runLater(() -> cardPane.setOpacity(0.1f));
        }

        double xyPositionAndRadius = profileImage.getFitHeight() / 2.0;
        profileImage.setClip(new Circle(xyPositionAndRadius, xyPositionAndRadius, xyPositionAndRadius));

        initIdLabel();
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
                if (person.getCity().length() > 0){
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
                bind(person.getObservableTagList()); //Bind property at instance initializer
            }

            @Override
            protected String computeValue() {
                return person.tagsString();
            }
        });
        person.isDeletedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == true){
                handleDeletedPerson();
            }
        });
        person.githubUserNameProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0){
                setProfileImage();
            }
        });
    }

    private void initIdLabel() {
        idLabel.setText(person.idString());
        person.onRemoteIdConfirmed(id -> {
            if (Platform.isFxApplicationThread()) {
                idLabel.setText(person.idString());
            } else {
                Platform.runLater(() -> idLabel.setText(person.idString()));
            }
        });
    }

    /**
     * Asynchronously sets the profile image to the image view.
     * Involves making an internet connection with the image hosting server.
     */
    private void setProfileImage() {
        final Optional<String> profileImageUrl = person.githubProfilePicUrl();
        if (profileImageUrl.isPresent()){
            new Thread(() -> {
                Image image = ImageManager.getInstance().getImage(profileImageUrl.get());
                if (image != null && image.getHeight() > 0) {
                    profileImage.setImage(image);
                } else {
                    profileImage.setImage(
                            new Image(this.getClass().getResourceAsStream("/images/default_profile_picture.png"))
                    );
                }
            }).start();
        }
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

    public HBox getLayout() {
        return cardPane;
    }
}
