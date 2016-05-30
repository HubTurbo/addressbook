package address.controller;

import java.io.File;
import java.util.Optional;

import address.MainApp;
import address.events.*;
import address.exceptions.DuplicateDataException;
import address.exceptions.DuplicateTagException;
import address.model.datatypes.Tag;
import address.model.ModelManager;
import address.shortcuts.ShortcutsManager;
import address.prefs.PrefsManager;

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

    private static final String SAVE_LOC_TEXT_PREFIX = "Save File: ";
    private static final String MIRROR_LOC_TEXT_PREFIX = "Mirror File: ";
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
    private MenuItem menuChooseMirror;

    @FXML
    private Text saveLocText;
    @FXML
    private Text mirrorLocText;

    public RootLayoutController() {
        EventManager.getInstance().registerHandler(this);
    }

    @FXML
    private void initialize() {
        updateSaveLocationDisplay();
        updateMirrorLocationDisplay();
    }


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

    @Subscribe
    private void handleSaveLocationChangedEvent(SaveLocationChangedEvent e) {
        updateSaveLocationDisplay();
    }

    @Subscribe
    private void handleMirrorLocationChangedEvent(MirrorLocationChangedEvent e) {
        updateMirrorLocationDisplay();
    }

    private void updateSaveLocationDisplay() {
        saveLocText.setText(SAVE_LOC_TEXT_PREFIX + (PrefsManager.getInstance().isSaveLocationSet() ?
                PrefsManager.getInstance().getSaveLocation().getName() : LOC_TEXT_NOT_SET));
    }

    private void updateMirrorLocationDisplay() {
        mirrorLocText.setText(MIRROR_LOC_TEXT_PREFIX + (PrefsManager.getInstance().isMirrorLocationSet() ?
                PrefsManager.getInstance().getMirrorLocation().getName() : LOC_TEXT_NOT_SET));
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
     * Creates a new empty address book not connected to any save or mirror file
     */
    @FXML
    private void handleNew() {
        PrefsManager.getInstance().clearSaveLocation();
        PrefsManager.getInstance().clearMirrorLocation();
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

    @FXML
    private void handleChooseMirror() {
        // Show open file dialog
        File toSyncWith = getXmlFileChooser().showOpenDialog(mainController.getPrimaryStage());
        if (toSyncWith == null) return;
        PrefsManager.getInstance().setMirrorLocation(toSyncWith);
    }

    /**
     * Saves the file to the person file that is currently open. If there is no
     * open file, the "save as" dialog is shown.
     */
    @FXML
    private void handleSave() {
        final File saveFile = PrefsManager.getInstance().getSaveLocation();
        EventManager.getInstance().post(new SaveRequestEvent(saveFile, modelManager.getPersonsModel(),
                                                             modelManager.getTagModel()));
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
        EventManager.getInstance().post(new SaveRequestEvent(file, modelManager.getPersonsModel(),
                                        modelManager.getTagModel()));
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
        mainController.showTagList(modelManager.getTagModel());
    }
}
