package address.controller;

import address.MainApp;
import address.keybindings.KeyBindingsManager;
import address.model.ModelManager;
import address.util.AppLogger;
import address.util.LoggerManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;

/**
 * The controller for the root layout. The root layout provides the basic
 * application layout containing a menu bar and space where other JavaFX
 * elements can be placed.
 */
public class RootLayoutController extends UiController{
    private static AppLogger logger = LoggerManager.getLogger(RootLayoutController.class);

    private MainController mainController;
    private ModelManager modelManager;
    private MainApp mainApp;

    @FXML
    private MenuItem helpMenuItem;

    public RootLayoutController() {
        super();
    }

    public void setConnections(MainApp mainApp, MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;
        this.mainApp = mainApp;
    }

    public void setAccelerators() {
        helpMenuItem.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("HELP_PAGE_ACCELERATOR").get());
    }

    @FXML
    private void handleHelp() {
        logger.debug("Showing help page about the application.");
        mainController.showHelpPage();
    }

    /**
     * Opens an about dialog.
     */
    @FXML
    private void handleAbout() {
        logger.debug("Showing information about the application.");
        mainController.showAlertDialogAndWait(AlertType.INFORMATION, "AddressApp", "About",
                "Version " + MainApp.VERSION.toString() + "\nSome code adapted from http://code.makery.ch");
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
