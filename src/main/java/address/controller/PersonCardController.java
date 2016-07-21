package address.controller;

import java.io.IOException;
import java.util.Optional;

import address.image.ImageManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;

import commons.FxViewUtil;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import static address.model.datatypes.person.ReadOnlyViewablePerson.*;

public class PersonCardController extends UiController {

    @FXML
    private HBox cardPane;
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

    @FXML
    private HBox commandStateDisplayRootNode;
    @FXML
    private Label commandTypeLabel;
    @FXML
    private ProgressIndicator remoteRequestOngoingIndicator;
    @FXML
    private Label commandStateInfoLabel;

    private ReadOnlyViewablePerson person;
    private StringProperty idTooltipString = new SimpleStringProperty("");

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
        bindDisplayedPersonData();
        initCommandStateDisplay();
        initIdTooltip();
    }

    private void bindDisplayedPersonData() {
        if (person.getGithubUsername().length() > 0) {
            setProfileImage();
        }
        FxViewUtil.configureCircularImageView(profileImage);

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
                return getAddressString(person.getStreet(), person.getCity(), person.getPostalCode());
            }
        });
        birthday.textProperty().bind(new StringBinding() {
            {
                bind(person.birthdayProperty()); //Bind property at instance initializer
            }
            @Override
            protected String computeValue() {
                return person.birthdayString().length() > 0 ? "DOB: " + person.birthdayString() : "";
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
    }

    private void initCommandStateDisplay() {
        // will be overwritten/hidden if not in grace period by below calls
        commandStateInfoLabel.setText("" + person.getSecondsLeftInPendingState());
        handleCommandState(person.getOngoingCommandState());
        commandTypeLabel.setText(person.getOngoingCommandType().toString());

        person.ongoingCommandStateProperty().addListener((obs, old, newVal) -> handleCommandState(newVal));
        person.ongoingCommandTypeProperty().addListener((obs, old, newVal) -> commandTypeLabel.setText(newVal.toString()));
        person.secondsLeftInPendingStateProperty().addListener(prop -> // invalidation listener on purpose!
                commandStateInfoLabel.setText("" + person.getSecondsLeftInPendingState()));
    }

    private void handleCommandState(OngoingCommandState state) {
        commandStateDisplayRootNode.setVisible(state != OngoingCommandState.INVALID);
        remoteRequestOngoingIndicator.setVisible(state == OngoingCommandState.SYNCING_TO_REMOTE);
        commandStateInfoLabel.setVisible(state != OngoingCommandState.SYNCING_TO_REMOTE);
        switch (state) {
            case REMOTE_CONFLICT:
                commandStateInfoLabel.setText("CONFLICT");
                break;
            case REQUEST_FAILED:
                commandStateInfoLabel.setText("FAILED");
                break;
        }
    }

    private void initIdTooltip() {
        Tooltip tp = new Tooltip();
        tp.textProperty().bind(idTooltipString);
        firstName.setTooltip(tp);
        lastName.setTooltip(tp);
        idTooltipString.set(person.idString());
        person.onRemoteIdConfirmed(id -> idTooltipString.set(person.idString()));
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

    public HBox getLayout() {
        return cardPane;
    }


    public static String getAddressString(String street, String city, String postalCode) {
        StringBuilder sb = new StringBuilder();
        if (street.length() > 0){
            sb.append(street).append(System.lineSeparator());
        }
        if (city.length() > 0){
            sb.append(city).append(System.lineSeparator());
        }
        if (postalCode.length() > 0){
            sb.append(postalCode);
        }
        return sb.toString();
    }
}
