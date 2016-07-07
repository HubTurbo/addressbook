package address.controller;

import java.io.IOException;
import java.util.Optional;


import address.image.ImageManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;

import address.util.FxViewUtil;
import javafx.animation.FadeTransition;
import javafx.application.Platform;

import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class PersonCardController extends UiController{
    public static final String DELETING_PENDING_STATE_MESSAGE = "Deleted ";
    public static final String EDITING_PENDING_STATE_MESSAGE = "Edited ";
    public static final String CREATED_PENDING_STATE_MESSAGE = "Created ";

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
    @FXML
    private Label pendingStateLabel;
    @FXML
    private ProgressIndicator syncIndicator;
    @FXML
    private HBox pendingStateHolder;

    private ReadOnlyViewablePerson person;
    private FadeTransition deleteTransition;

    {
        deleteTransition = new FadeTransition(Duration.millis(1000), cardPane);
        deleteTransition.setFromValue(1.0);
        deleteTransition.setToValue(0.1);
        deleteTransition.setCycleCount(1);
    }

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

        if (person.getGithubUsername().length() > 0) {
            setProfileImage();
        }

        FxViewUtil.configureCircularImageView(profileImage);

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
                    sb.append(person.getStreet()).append("\n");
                }
                if (person.getCity().length() > 0){
                    sb.append(person.getCity()).append("\n");
                }
                if (person.getPostalCode().length() > 0){
                    sb.append(person.getPostalCode());
                }
                return sb.toString();
            }
        });
        birthday.textProperty().bind(new StringBinding() {
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
        tags.textProperty().bind(new StringBinding() {
            {
                bind(person.getObservableTagList()); //Bind property at instance initializer
            }

            @Override
            protected String computeValue() {
                return person.tagsString();
            }
        });

        person.githubUsernameProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0) {
                setProfileImage();
            }
        });
        if (person.getSecondsLeftInPendingState() > 0) {
            setPendingStateMessage(person.getChangeInProgress(), person.getSecondsLeftInPendingState());
            pendingStateHolder.setVisible(true);
        }
        person.secondsLeftInPendingStateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > 0) {
                setPendingStateMessage(person.getChangeInProgress(), newValue);
                pendingStateHolder.setVisible(true);
            } else {
                cardPane.setStyle(null);
                setPendingStateMessage(person.getChangeInProgress(), -1);
                pendingStateHolder.setVisible(true);
                syncIndicator.setVisible(true);
                person.onRemoteIdConfirmed((Integer id) -> {
                    syncIndicator.setVisible(false);
                    pendingStateLabel.setText("");
                    pendingStateHolder.setVisible(false);
                });
            }
        });
    }

    private void setPendingStateMessage(ReadOnlyViewablePerson.ChangeInProgress changeInProgress, Number secsLeft) {

        String secLeftString = "";

        if (secsLeft.shortValue() > 0) {
            secLeftString = String.valueOf(secsLeft);
        }

        if (changeInProgress == ReadOnlyViewablePerson.ChangeInProgress.ADDING) {
            pendingStateLabel.setText(CREATED_PENDING_STATE_MESSAGE + secLeftString);
        } else if (changeInProgress == ReadOnlyViewablePerson.ChangeInProgress.EDITING) {
            pendingStateLabel.setText(EDITING_PENDING_STATE_MESSAGE + secLeftString);
        } else if (changeInProgress == ReadOnlyViewablePerson.ChangeInProgress.DELETING) {
            pendingStateLabel.setText(DELETING_PENDING_STATE_MESSAGE + secLeftString);
        }
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
                    profileImage.setImage(ImageManager.getDefaultProfileImage());
                }
            }).start();
        }
    }

    public void handleDelete() {
        Platform.runLater(() -> deleteTransition.play());
    }

    public HBox getLayout() {
        return cardPane;
    }
}
