package address.controller;

import address.MainApp;
import address.events.parser.FilterCommittedEvent;
import address.keybindings.KeyBindingsManager;
import address.model.ModelManager;
import address.parser.ParseException;
import address.parser.Parser;
import address.parser.expr.Expr;
import address.parser.expr.PredExpr;
import address.util.AppLogger;
import address.util.LoggerManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

/**
 * The controller for the root layout. The root layout provides the basic
 * application layout containing a menu bar and space where other JavaFX
 * elements can be placed.
 */
public class RootLayoutController extends UiController {
    private static AppLogger logger = LoggerManager.getLogger(RootLayoutController.class);

    private MainController mainController;
    private ModelManager modelManager;
    private MainApp mainApp;

    private Parser parser;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private TextField filterField;

    public RootLayoutController() {
        super();
        parser = new Parser();
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
    private void handleFilterChanged() {
        Expr filterExpression;
        try {
            filterExpression = parser.parse(filterField.getText());
            if (filterField.getStyleClass().contains("error")) filterField.getStyleClass().remove("error");
        } catch (ParseException e) {
            logger.debug("Invalid filter found: {}", e);
            filterExpression = PredExpr.TRUE;
            if (!filterField.getStyleClass().contains("error")) filterField.getStyleClass().add("error");
        }

        raise(new FilterCommittedEvent(filterExpression));
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

    @FXML
    private void handleShowTags() {
        logger.debug("Attempting to show tag list.");
        mainController.showTagList(modelManager.getTagsAsReadOnlyObservableList());
    }
}
