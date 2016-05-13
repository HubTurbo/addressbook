package address.controller;

import java.io.File;
import java.util.Collections;

import address.MainApp;
import address.events.EventManager;
import address.events.LoadDataRequestEvent;
import address.events.SaveRequestEvent;
import address.model.ContactGroup;
import address.model.ModelManager;
import address.preferences.PreferencesManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;

/**
 * The controller for the root layout. The root layout provides the basic
 * application layout containing a menu bar and space where other JavaFX
 * elements can be placed.
 */
public class RootLayoutController {

    private MainController mainController;
    private ModelManager modelManager;
    private MainApp mainApp;

    public void setConnections(MainApp mainApp, MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;
        this.mainApp = mainApp;
    }

    /**
     * Creates an new empty address book at the default filepath
     */
    @FXML
    private void handleNew() {
        PreferencesManager.getInstance().setPersonFilePath(null);
        modelManager.resetData(Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Opens a FileChooser to let the user select an address book to load.
     */
    @FXML
    private void handleOpen() {
        FileChooser fileChooser = getFileChooser();

        // Show save file dialog
        File file = fileChooser.showOpenDialog(mainController.getPrimaryStage());

        if (file != null) {
            EventManager.getInstance().post(new LoadDataRequestEvent(file));
        }
    }

    /**
     * Saves the file to the person file that is currently open. If there is no
     * open file, the "save as" dialog is shown.
     */
    @FXML
    private void handleSave() {
        final File saveFile = PreferencesManager.getInstance().getPersonFile();
        EventManager.getInstance().post(new SaveRequestEvent(saveFile, modelManager.getPersons(),
                                                             modelManager.getGroupData()));
    }

    /**
     * Opens a FileChooser to let the user select a file to save to.
     */
    @FXML
    private void handleSaveAs() {
        FileChooser fileChooser = getFileChooser();

        // Show save file dialog
        File file = fileChooser.showSaveDialog(mainController.getPrimaryStage());

        if (file != null) {
            // Make sure it has the correct extension
            if (!file.getPath().endsWith(".xml")) {
                file = new File(file.getPath() + ".xml");
            }
            PreferencesManager.getInstance().setPersonFilePath(file);
            EventManager.getInstance().post(new SaveRequestEvent(file, modelManager.getPersons(),
                                                                 modelManager.getGroupData()));
        }
    }

    /**
     * Appends dummy data to existing data
     */
    @FXML
    private void handleAppendSampleData() {
        modelManager.appendSampleData();
    }


    /**
     * @return a file chooser for choosing xml files. The initial folder is set to the same folder that the
     *     current data file is located (if any).
     */
    private FileChooser getFileChooser() {

        // Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        final FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add(extFilter);
        final File currentFile = PreferencesManager.getInstance().getPersonFile();
        fileChooser.setInitialDirectory(currentFile.getParentFile());
        return fileChooser;
    }

    /**
     * Opens an about dialog.
     */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("AddressApp");
        alert.setHeaderText("About");
        alert.setContentText("Some code adapted from http://code.makery.ch");

        alert.showAndWait();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        mainApp.stop();
    }
    
    /**
     * Opens the birthday statistics.
     */
    @FXML
    private void handleShowBirthdayStatistics() {
        mainController.showBirthdayStatistics();
    }


    @FXML
    private void handleNewGroup() {
        ContactGroup tempGroup = new ContactGroup();
        boolean okClicked = mainController.showGroupEditDialog(tempGroup);
        if (okClicked) {
            modelManager.addGroup(tempGroup);
        }
    }

    @FXML
    private void handleShowGroups() {
        mainController.showGroupList(modelManager.getGroupData());
    }
}
