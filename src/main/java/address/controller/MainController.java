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
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The controller that creates the other controllers
 */
public class MainController {

    private Config config;
    private Stage primaryStage;
    private BorderPane rootLayout;

    private ModelManager modelManager;
    private MainApp mainApp;

    public MainController(MainApp mainApp, ModelManager modelManager, Config config){
        EventManager.getInstance().registerHandler(this);
        this.modelManager = modelManager;
        this.config = config;
        this.mainApp = mainApp;
    }
    
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        setTitle(config.appTitle, PreferencesManager.getInstance().getPersonFile());

        // Set the application icon.
        this.primaryStage.getIcons().add(getImage("/images/address_book_32.png"));

        initRootLayout();
        showPersonOverview();
    }

    /**
     * Initializes the root layout and tries to load the last opened
     * person file.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setMinHeight(200);
            primaryStage.setMinWidth(340);
            primaryStage.setHeight(600);
            primaryStage.setWidth(340);
            primaryStage.setScene(scene);

            // Give the rootController access to the main controller and modelManager
            RootLayoutController rootController = loader.getController();
            rootController.setConnections(mainApp, this, modelManager);

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the person overview inside the root layout.
     */
    public void showPersonOverview() {
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/PersonOverview.fxml"));
            AnchorPane personOverview = (AnchorPane) loader.load();

            // Set person overview into the center of root layout.
            rootLayout.setCenter(personOverview);

            // Give the personOverviewController access to the main app and modelManager.
            PersonOverviewController personOverviewController = loader.getController();
            personOverviewController.setConnections(this, modelManager);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a dialog to edit details for the specified person. If the user
     * clicks OK, the changes are saved into the provided person object and true
     * is returned.
     *
     * @param person the person object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showPersonEditDialog(Person person) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/PersonEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Person");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.getIcons().add(getImage("/images/edit.png"));

            // Set the person into the controller.
            PersonEditDialogController personEditDialogController = loader.getController();
            personEditDialogController.setDialogStage(dialogStage);
            personEditDialogController.setModelManager(modelManager);
            personEditDialogController.setPerson(person);
            personEditDialogController.setModel(modelManager.getGroupData(), person.getContactGroups());

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return personEditDialogController.isOkClicked();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Opens a dialog to edit details for the specified group. If the user
     * clicks OK, the changes are saved into the provided group object and true
     * is returned.
     *
     * @param group the group object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showGroupEditDialog(ContactGroup group) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/GroupEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Group");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.getIcons().add(getImage("/images/edit.png"));

            // Set the group into the controller.
            GroupEditDialogController groupEditDialogController = loader.getController();
            groupEditDialogController.setDialogStage(dialogStage);
            groupEditDialogController.setModelManager(modelManager);
            groupEditDialogController.setGroup(group);
            groupEditDialogController.setGroups(modelManager.getGroupData());

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return groupEditDialogController.isOkClicked();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean showGroupList(List<ContactGroup> groups) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/GroupList.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("List of Contact Groups");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            //dialogStage.getIcons().add(getImage("/images/edit.png"));

            // Set the group into the controller.
            GroupListController groupListController = loader.getController();
            groupListController.setGroups(modelManager.getGroupData(), this, modelManager);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Opens a dialog to show birthday statistics.
     */
    public void showBirthdayStatistics() {
        try {
            // Load the fxml file and create a new stage for the popup.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/BirthdayStatistics.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Birthday Statistics");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.getIcons().add(getImage("/images/calendar.png"));

            // Set the persons into the controller.
            BirthdayStatisticsController controller = loader.getController();
            controller.setPersonData(modelManager.getPersonData());

            dialogStage.show();

        } catch (IOException e) {
            e.printStackTrace();
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
    public void setTitle(String appTitle, File file){
        primaryStage.setTitle(appTitle + " - " + file.getName());
    }

    @Subscribe
    private void handleFileOpeningExceptionEvent(FileOpeningExceptionEvent foee){
        showFileOperationAlert("Could not load data", "Could not load data from file", foee.file, foee.exception);
    }

    @Subscribe
    private void handleFileSavingExceptionEvent(FileSavingExceptionEvent fsee){
        showFileOperationAlert("Could not save data", "Could not save data to file", fsee.file, fsee.exception);
    }

    private void showFileOperationAlert(String header, String message, File file, Exception e) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(primaryStage);
        alert.setTitle("Error");
        alert.setHeaderText(header);

        final StringBuilder content = new StringBuilder();
        content.append(message)
            .append(":\n")
            .append(file == null ? "none" : file.getPath())
            .append("\n\nDetails:\n=====\n")
            .append(e.toString());

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

    public void showWarningDialogAndWait(String title, String headerText, String contentText){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

}
