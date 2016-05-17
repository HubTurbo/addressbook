package address.controller;

import address.MainApp;
import address.events.EventManager;
import address.events.FileNameChangedEvent;
import address.events.FileOpeningExceptionEvent;
import address.events.FileSavingExceptionEvent;
import address.model.ContactGroup;
import address.model.ModelManager;
import address.model.Person;
import address.preferences.PreferencesManager;
import address.util.Config;

import com.google.common.eventbus.Subscribe;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * The controller that creates the other controllers
 */
public class MainController {
    private static final String FXML_GROUP_EDIT_DIALOG = "/view/GroupEditDialog.fxml";
    private static final String FXML_PERSON_EDIT_DIALOG = "/view/PersonEditDialog.fxml";
    private static final String FXML_PERSON_OVERVIEW = "/view/PersonOverview.fxml";
    private static final String FXML_GROUP_LIST = "/view/GroupList.fxml";
    private static final String FXML_BIRTHDAY_STATISTICS = "/view/BirthdayStatistics.fxml";
    private static final String FXML_ROOT_LAYOUT = "/view/RootLayout.fxml";
    private static final String ICON_APPLICATION = "/images/address_book_32.png";
    private static final String ICON_EDIT = "/images/edit.png";
    private static final String ICON_CALENDAR = "/images/calendar.png";

    private Config config;
    private Stage primaryStage;
    private BorderPane rootLayout;

    private ModelManager modelManager;
    private MainApp mainApp;

    Browser browser;

    public MainController(MainApp mainApp, ModelManager modelManager, Config config) {
        EventManager.getInstance().registerHandler(this);
        this.modelManager = modelManager;
        this.config = config;
        this.mainApp = mainApp;
        this.browser = new Browser();
    }
    
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        setTitle(config.appTitle, PreferencesManager.getInstance().getPersonFile());

        // Set the application icon.
        this.primaryStage.getIcons().add(getImage(ICON_APPLICATION));

        initRootLayout();
        showPersonOverview();
        showPersonWebPage();
    }

    public void loadBrowserUrl(URL url){
        browser.loadURL(url.toExternalForm());
    }

    /**
     * Initializes the root layout and tries to load the last opened
     * person file.
     */
    public void initRootLayout() {
        final String fxmlResourcePath = FXML_ROOT_LAYOUT;
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            rootLayout = loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setMinHeight(400);
            primaryStage.setMinWidth(740);
            primaryStage.setHeight(600);
            primaryStage.setWidth(340);
            primaryStage.setScene(scene);

            // Give the rootController access to the main controller and modelManager
            RootLayoutController rootController = loader.getController();
            rootController.setConnections(mainApp, this, modelManager);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml root layout.",
                                   "IOException when trying to load " + fxmlResourcePath);
        }
    }

    /**
     * Shows the person overview inside the root layout.
     */
    public void showPersonOverview() {
        final String fxmlResourcePath = FXML_PERSON_OVERVIEW;
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            AnchorPane personOverview = loader.load();
            // Set person overview into the center of root layout.
            personOverview.setMinWidth(340);
            rootLayout.setLeft(personOverview);

            // Give the personOverviewController access to the main app and modelManager.
            PersonOverviewController personOverviewController = loader.getController();
            personOverviewController.setConnections(this, modelManager);

        } catch (IOException e) {
            e.printStackTrace();
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml for person overview.",
                                   "IOException when trying to load " + fxmlResourcePath);
        }
    }

    public void showPersonWebPage() {
        BrowserView browserView = new BrowserView(browser);
        browser.loadHTML("<html><body><h3>To view contact's web page, click on the contact on the left." +
                "</h3></body></html>");
        rootLayout.setCenter(browserView);
    }

    /**
     * Get user input for defining a Person object.
     *
     * @param defaultData default data shown for user input
     * @return a defensively copied optional containing the input data from user, or an empty optional if the
     *          operation is to be cancelled.
     */
    public Optional<Person> getPersonDataInput(Person defaultData) {
        return showPersonEditDialog(defaultData);
    }

    /**
     * Opens a dialog to edit details for a Person object. If the user
     * clicks OK, the input data is recorded in a new Person object and returned.
     *
     * @param initialData the person object determining the initial data in the input fields
     * @return an optional containing the new data, or an empty optional if there was an error
     *         creating the dialog or the user clicked cancel
     */
    private Optional<Person> showPersonEditDialog(Person initialData) {
        final String fxmlResourcePath = FXML_PERSON_EDIT_DIALOG;
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Person");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.getIcons().add(getImage(ICON_EDIT));

            // Pass relevant data into the controller.
            PersonEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setInitialPersonData(initialData);
            controller.setGroupsModel(modelManager.getGroupData(), initialData.getContactGroupsCopy());

            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                return Optional.of(controller.getFinalInput());
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml for edit person dialog.",
                                   "IOException when trying to load " + fxmlResourcePath);
            return Optional.empty();
        }
    }

    /**
     * Opens a dialog to edit details for the specified group. If the user
     * clicks OK, the changes are recorded in a new ContactGroup and returned.
     *
     * @param group the group object determining the initial data in the input fields
     * @return an optional containing the new data, or an empty optional if there was an error
     *         creating the dialog or the user clicked cancel
     */
    public Optional<ContactGroup> getGroupDataInput(ContactGroup group) {
        final String fxmlResourcePath = FXML_GROUP_EDIT_DIALOG;
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Group");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.getIcons().add(getImage(ICON_EDIT));

            // Pass relevant data to the controller.
            GroupEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setInitialGroupData(group);

            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                return Optional.of(controller.getFinalInput());
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml for edit group dialog.",
                                   "IOException when trying to load " + fxmlResourcePath);
            return Optional.empty();
        }
    }

    public void showGroupList(ObservableList<ContactGroup> groups) {
        final String fxmlResourcePath = FXML_GROUP_LIST;
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("List of Contact Groups");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the group into the controller.
            GroupListController groupListController = loader.getController();
            groupListController.setGroups(groups, this, modelManager);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml for group list.",
                                   "IOException when trying to load " + fxmlResourcePath);
        }
    }

    /**
     * Opens a dialog to show birthday statistics.
     */
    public void showBirthdayStatistics() {
        final String fxmlResourcePath = FXML_BIRTHDAY_STATISTICS;
        try {
            // Load the fxml file and create a new stage for the popup.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Birthday Statistics");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.getIcons().add(getImage(ICON_CALENDAR));

            // Set the persons into the controller.
            BirthdayStatisticsController controller = loader.getController();
            controller.setPersonData(modelManager.getFilteredPersons());

            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml for birthday stats.",
                                   "IOException when trying to load " + fxmlResourcePath);
        }
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @Subscribe
    public void handleFileNameChangedEvent(FileNameChangedEvent fnce){
        setTitle(config.appTitle, fnce.file != null ? fnce.file : new File(PreferencesManager.DEFAULT_FILE_PATH));
    }

    /**
     * Sets the title of the app UI based on the file location
     * @param file the data file used by the app, or null if no file chosen
     */
    public void setTitle(String appTitle, File file) {
        primaryStage.setTitle(appTitle + " - " + file.getName());
    }

    @Subscribe
    private void handleFileOpeningExceptionEvent(FileOpeningExceptionEvent foee) {
        showFileOperationAlertAndWait("Could not load data", "Could not load data from file", foee.file,
                                      foee.exception);
    }

    @Subscribe
    private void handleFileSavingExceptionEvent(FileSavingExceptionEvent fsee) {
        showFileOperationAlertAndWait("Could not save data", "Could not save data to file", fsee.file, fsee.exception);
    }

    private void showFileOperationAlertAndWait(String description, String details, File file, Throwable cause) {
        final StringBuilder content = new StringBuilder();
        content.append(details)
            .append(":\n")
            .append(file == null ? "none" : file.getPath())
            .append("\n\nDetails:\n======\n")
            .append(cause.toString());

        showAlertDialogAndWait(AlertType.ERROR, "File Op Error", description, content.toString());
    }

    private Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

    public void showAlertDialogAndWait(AlertType type, String title, String headerText, String contentText) {
        showAlertDialogAndWait(primaryStage, type, title, headerText, contentText);
    }

    public static void showAlertDialogAndWait(Stage owner, AlertType type, String title, String headerText,
                                              String contentText) {
        final Alert alert = new Alert(type);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }
}
