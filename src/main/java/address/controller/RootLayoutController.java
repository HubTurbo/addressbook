package address.controller;

import java.io.File;
import java.util.Optional;

import address.MainApp;
import address.events.*;
import address.exceptions.DuplicateDataException;
import address.exceptions.DuplicateTagException;
import address.model.datatypes.tag.Tag;
import address.model.ModelManager;
import address.keybindings.KeyBindingsManager;
import address.prefs.PrefsManager;

import address.util.AppLogger;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;

/**
 * The controller for the root layout. The root layout provides the basic
 * application layout containing a menu bar and space where other JavaFX
 * elements can be placed.
 */
public class RootLayoutController {
    private static AppLogger logger = LoggerManager.getLogger(RootLayoutController.class);

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

    public RootLayoutController() {
        EventManager.getInstance().registerHandler(this);
    }

    public void setConnections(MainApp mainApp, MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;
        this.mainApp = mainApp;
    }

    public void setAccelerators(){
        menuFileNew.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("FILE_NEW_ACCELERATOR").get());
        menuFileOpen.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("FILE_OPEN_ACCELERATOR").get());
        menuFileSave.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("FILE_SAVE_ACCELERATOR").get());
        menuFileSaveAs.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("FILE_SAVE_AS_ACCELERATOR").get());
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
        final File currentFile = PrefsManager.getInstance().getPrefs().getSaveLocation();
        fileChooser.setInitialDirectory(currentFile.getParentFile());
        return fileChooser;
    }


    /**
     * Creates a new empty address book
     */
    @FXML
    private void handleNew() {
        logger.debug("Wiping current model data.");
        PrefsManager.getInstance().clearSaveLocation();
        modelManager.clearModel();
    }

    /**
     * Opens a FileChooser to let the user select an address book to load.
     */
    @FXML
    private void handleOpen() {
        logger.debug("Prompting file dialog for data source.");
        // Show open file dialog
        File toOpen = getXmlFileChooser().showOpenDialog(mainController.getPrimaryStage());
        if (toOpen == null) return;
        PrefsManager.getInstance().setSaveLocation(toOpen);
        EventManager.getInstance().post(new LoadDataRequestEvent(toOpen));
    }

    /**
     * Saves the file to the person file that is currently open. If there is no
     * open file, the "save as" dialog is shown.
     */
    @FXML
    private void handleSave() {
        final File saveFile = PrefsManager.getInstance().getPrefs().getSaveLocation();
        logger.debug("Requesting save to: {}.", saveFile);
        EventManager.getInstance().post(new SaveRequestEvent(saveFile, modelManager.backingPersonList(),
                                                             modelManager.getTagList()));
    }

    /**
     * Opens a FileChooser to let the user select a file to save to.
     */
    @FXML
    private void handleSaveAs() {
        logger.debug("Prompting file dialog for save destination.");
        // Show save file dialog
        File file = getXmlFileChooser().showSaveDialog(mainController.getPrimaryStage());

        if (file == null) return;

        // Make sure it has the correct extension
        if (!file.getPath().endsWith(".xml")) {
            file = new File(file.getPath() + ".xml");
        }

        PrefsManager.getInstance().setSaveLocation(file);
        EventManager.getInstance().post(new SaveRequestEvent(file, modelManager.backingPersonList(),
                                        modelManager.getTagList()));
    }

    /**
     * Clears existing data and appends dummy data
     */
    @FXML
    private void handleResetWithSampleData() {
        logger.debug("Resetting with sample data.");
        try {
            modelManager.resetWithSampleData();
        } catch (DuplicateDataException e) {
            logger.warn("Error resetting sample data: {}", e);
            mainController.showAlertDialogAndWait(AlertType.INFORMATION, "Duplicate data found",
                    "Sample data has duplicates",
                    "Verify that the sample data is valid and does not contain duplicates before adding");
        }
    }

    /**
     * Opens an about dialog.
     */
    @FXML
    private void handleAbout() {
        logger.debug("Showing information about the application.");
        mainController.showAlertDialogAndWait(AlertType.INFORMATION, "AddressApp", "About",
                "Some code adapted from http://code.makery.ch");
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
    private void handleNewTag() {
        logger.debug("Adding a new tag from the root layout.");
        mainController.addTagData();
    }

    @FXML
    private void handleShowTags() {
        logger.debug("Attempting to show tag list.");
        mainController.showTagList(modelManager.getAllViewableTagsReadOnly());
    }
}
