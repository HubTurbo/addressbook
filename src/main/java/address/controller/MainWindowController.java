package address.controller;

import address.MainApp;
import address.keybindings.KeyBindingsManager;
import address.model.ModelManager;
import address.ui.Ui;
import address.util.AppLogger;
import address.util.LoggerManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;

/**
 * The controller for the Main Window. Provides layout provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindowController extends UiController {
    private static AppLogger logger = LoggerManager.getLogger(MainWindowController.class);

    private Ui ui; //TODO: remove this dependency as per TODOs given in methods below
    private ModelManager modelManager;
    private MainApp mainApp; //TODO: remove this dependency as per TODOs given in methods below

    @FXML
    private MenuItem helpMenuItem;

    public MainWindowController() {
        super();
    }

    public void setConnections(MainApp mainApp, Ui ui, ModelManager modelManager) {
        this.ui = ui;
        this.modelManager = modelManager;
        this.mainApp = mainApp;
    }

    public void setAccelerators() {
        helpMenuItem.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("HELP_PAGE_ACCELERATOR").get());
    }

    @FXML
    private void handleHelp() {
        logger.debug("Showing help page about the application.");
        HelpWindow helpWindow = ViewLoader.loadView(primaryStage, new HelpWindow());
        helpWindow.show();
    }

    /**
     * Opens an about dialog.
     */
    @FXML
    private void handleAbout() {
        //TODO: refactor to be similar to handleHelp and remove the dependency to ui
        logger.debug("Showing information about the application.");
        ui.showAlertDialogAndWait(AlertType.INFORMATION, "AddressApp", "About",
                "Version " + MainApp.VERSION.toString() + "\nSome code adapted from http://code.makery.ch");
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        //TODO: remove dependency on mainApp by using an event
        mainApp.stop();
    }

    /**
     * Opens the birthday statistics.
     */
    @FXML
    private void handleShowBirthdayStatistics() {
        //TODO: refactor to be similar to handleHelp and remove the dependency to ui
        ui.showBirthdayStatistics();
    }


    @FXML
    private void handleNewTag() {
        //TODO: refactor to be similar to handleHelp and remove the dependency to ui
        logger.debug("Adding a new tag from the root layout.");
        ui.addTagData();
    }

    @FXML
    private void handleShowTags() {
        //TODO: refactor to be similar to handleHelp and remove the dependency to ui
        logger.debug("Attempting to show tag list.");
        ui.showTagList(modelManager.getAllViewableTagsReadOnly());
    }
}
