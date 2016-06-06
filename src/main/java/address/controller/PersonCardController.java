package address.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;


import address.model.datatypes.Person;

import address.events.EventManager;
import address.events.SyncCompletedEvent;

import address.status.PersonDeletedStatus;
import address.ui.PersonListViewCell;
import address.util.FxViewUtil;
import com.google.common.eventbus.Subscribe;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;

import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class PersonCardController {
    @FXML
    private GridPane gridPane;
    @FXML
    private HBox hBox;
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
        EventManager.getInstance().registerHandler(this);
    }

    @FXML
    public void initialize() {
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
        if (person.getIsDeleted()){
            Platform.runLater(() -> gridPane.setOpacity(0.1f));

        }
        double xyPositionAndRadius = profileImage.getFitHeight()/2.0;
        Circle circle = new Circle(xyPositionAndRadius,xyPositionAndRadius,xyPositionAndRadius);
        profileImage.setClip(circle);
        FxViewUtil.applyAnchorBoundaryParameters(gridPane, 0.0, 0.0, 0.0, 0.0);
    }

    public void handleDeletedPerson(){

        Platform.runLater(() -> {
            FadeTransition ft = new FadeTransition(Duration.millis(1000), gridPane);
            ft.setFromValue(1.0);
            ft.setToValue(0.1);
            ft.setCycleCount(1);
            ft.play();
        });
    }

    public AnchorPane getLayout() {
        return cardPane;
    }

    @Subscribe
    public void handlePersonDeletedStatus(PersonDeletedStatus e){
        if (e.getPerson().equals(this.person)){
            FadeTransition ft = new FadeTransition(Duration.millis(3000), gridPane);
            ft.setFromValue(1.0);
            ft.setToValue(0.1);
            ft.setCycleCount(1);
            ft.play();

        }
    }
}
