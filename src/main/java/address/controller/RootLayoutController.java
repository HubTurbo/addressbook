package address.controller;

import java.io.File;
import java.util.Collections;
import java.util.Optional;

import address.MainApp;
import address.events.EventManager;
import address.events.LoadDataRequestEvent;
import address.events.SaveRequestEvent;
import address.exceptions.DuplicateDataException;
import address.exceptions.DuplicateGroupException;
import address.model.ContactGroup;
import address.model.ModelManager;
import address.shortcuts.ShortcutsManager;
import address.preferences.PrefsManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
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

    @FXML
    private MenuItem menuFileNew;

    @FXML
    private MenuItem menuFileOpen;

    @FXML
    private MenuItem menuFileSave;

    @FXML
    private MenuItem menuFileSaveAs;

    public void setConnections(MainApp mainApp, MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;
        this.mainApp = mainApp;
    }

    public void setShortcuts(){
        menuFileNew.setAccelerator(ShortcutsManager.SHORTCUT_FILE_NEW);
        menuFileOpen.setAccelerator(ShortcutsManager.SHORTCUT_FILE_OPEN);
        menuFileSave.setAccelerator(ShortcutsManager.SHORTCUT_FILE_SAVE);
        menuFileSaveAs.setAccelerator(ShortcutsManager.SHORTCUT_FILE_SAVE_AS);
    }

    /**
     * Creates a new empty address book at the default filepath
     */
    @FXML
    private void handleNew() {
        PrefsManager.getInstance().clearSaveFilePath();
        PrefsManager.getInstance().clearMirrorFilePath();
        modelManager.clearModel();
    }

    /**
     * Opens a FileChooser to let the user select an address book to load.
     */
    @FXML
    private void handleOpen() {
        // Show open file dialog
        File file = getXmlFileChooser().showOpenDialog(mainController.getPrimaryStage());
        if (file == null) return;
        EventManager.getInstance().post(new LoadDataRequestEvent(file));
    }

    /**
     * Saves the file to the person file that is currently open. If there is no
     * open file, the "save as" dialog is shown.
     */
    @FXML
    private void handleSave() {
        final File saveFile = PrefsManager.getInstance().getSaveFile();
        EventManager.getInstance().post(new SaveRequestEvent(saveFile, modelManager.getPersonsModel(),
                                                             modelManager.getGroupModel()));
    }

    /**
     * Opens a FileChooser to let the user select a file to save to.
     */
    @FXML
    private void handleSaveAs() {
        // Show save file dialog
        File file = getXmlFileChooser().showSaveDialog(mainController.getPrimaryStage());

        if (file == null) return;

        // Make sure it has the correct extension
        if (!file.getPath().endsWith(".xml")) {
            file = new File(file.getPath() + ".xml");
        }

        PrefsManager.getInstance().setSaveFile(file);
        EventManager.getInstance().post(new SaveRequestEvent(file, modelManager.getPersonsModel(),
                                        modelManager.getGroupModel()));
    }

    /**
     * Appends dummy data to existing data
     */
    @FXML
    private void handleAppendSampleData() {
        try {
            modelManager.updateWithSampleData();
        } catch (DuplicateDataException e) {
            mainController.showAlertDialogAndWait(AlertType.INFORMATION, "Will cause duplicates",
                    "Adding sample data clashes with existing data",
                    "Some existing data already matches those in the sample data");
        }
    }


    /**
     * @return a file chooser for choosing xml files. The initial folder is set to the same folder that the
     *     current data file is located (if any).
     */
    private FileChooser getXmlFileChooser() {
        // Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        final FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add(extFilter);
        final File currentFile = PrefsManager.getInstance().getSaveFile();
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
        Optional<ContactGroup> newGroup = Optional.of(new ContactGroup());
        while (true) { // keep re-asking until user provides valid input or cancels operation.
            newGroup = mainController.getGroupDataInput(newGroup.get());
            if (!newGroup.isPresent()) break;
            try {
                modelManager.addGroup(newGroup.get());
                break;
            } catch (DuplicateGroupException e) {
                mainController.showAlertDialogAndWait(AlertType.WARNING, "Warning", "Cannot have duplicate groups",
                                                      e.toString());
            }
        }
    }

    @FXML
    private void handleShowGroups() {
        mainController.showGroupList(modelManager.getGroupModel());
    }
}
