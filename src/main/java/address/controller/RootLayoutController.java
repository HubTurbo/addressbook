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

    private static final String SAVE_LOC_TEXT_PREFIX = "Save File: ";
    private static final String LOC_TEXT_NOT_SET = "[NOT SET]";

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

    @FXML
    private Text saveLocText;

    public RootLayoutController() {
        EventManager.getInstance().registerHandler(this);
    }

    @FXML
    private void initialize() {
        updateSaveLocationDisplay();
    }


    public void setConnections(MainApp mainApp, MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;
        this.mainApp = mainApp;
    }

    public void setAccelerators(){
        menuFileNew.setAccelerator(KeyBindingsManager.BINDINGS.FILE_NEW_ACCELERATOR.getKeyCombination());
        menuFileOpen.setAccelerator(KeyBindingsManager.BINDINGS.FILE_OPEN_ACCELERATOR.getKeyCombination());
        menuFileSave.setAccelerator(KeyBindingsManager.BINDINGS.FILE_SAVE_ACCELERATOR.getKeyCombination());
        menuFileSaveAs.setAccelerator(KeyBindingsManager.BINDINGS.FILE_SAVE_AS_ACCELERATOR.getKeyCombination());
    }

    @Subscribe
    private void handleSaveLocationChangedEvent(SaveLocationChangedEvent e) {
        updateSaveLocationDisplay();
    }

    private void updateSaveLocationDisplay() {
        saveLocText.setText(SAVE_LOC_TEXT_PREFIX + (PrefsManager.getInstance().isSaveLocationSet() ?
                PrefsManager.getInstance().getSaveLocation().getName() : LOC_TEXT_NOT_SET));
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
        final File currentFile = PrefsManager.getInstance().getSaveLocation();
        fileChooser.setInitialDirectory(currentFile.getParentFile());
        return fileChooser;
    }


    /**
     * Creates a new empty address book
     */
    @FXML
    private void handleNew() {
        PrefsManager.getInstance().clearSaveLocation();
        modelManager.clearModel();
    }

    /**
     * Opens a FileChooser to let the user select an address book to load.
     */
    @FXML
    private void handleOpen() {
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
        final File saveFile = PrefsManager.getInstance().getSaveLocation();
        EventManager.getInstance().post(new SaveRequestEvent(saveFile, modelManager.getAllPersons(),
                                                             modelManager.getAllTags()));
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

        PrefsManager.getInstance().setSaveLocation(file);
        EventManager.getInstance().post(new SaveRequestEvent(file, modelManager.getAllPersons(),
                                        modelManager.getAllTags()));
    }

    /**
     * Appends dummy data to existing data
     */
    @FXML
    private void handleResetWithSampleData() {
        try {
            modelManager.resetWithSampleData();
        } catch (DuplicateDataException e) {
            mainController.showAlertDialogAndWait(AlertType.INFORMATION, "Will cause duplicates",
                    "Adding sample data clashes with existing data",
                    "Some existing data already matches those in the sample data");
        }
    }

    /**
     * Opens an about dialog.
     */
    @FXML
    private void handleAbout() {
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
        Optional<Tag> newTag = Optional.of(new Tag());
        while (true) { // keep re-asking until user provides valid input or cancels operation.
            newTag = mainController.getTagDataInput(newTag.get());
            if (!newTag.isPresent()) break;
            try {
                modelManager.addTag(newTag.get());
                break;
            } catch (DuplicateTagException e) {
                mainController.showAlertDialogAndWait(AlertType.WARNING, "Warning", "Cannot have duplicate tags",
                                                      e.toString());
            }
        }
    }

    @FXML
    private void handleShowTags() {
        mainController.showTagList(modelManager.getAllViewableTagsReadOnly());
    }
}
