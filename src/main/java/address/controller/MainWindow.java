package address.controller;

import address.MainApp;
import address.events.MinimizeAppRequestEvent;
import address.events.ResizeAppRequestEvent;
import address.keybindings.KeyBindingsManager;
import address.model.ModelManager;
import address.model.UserPrefs;
import address.ui.Ui;
import address.util.AppLogger;
import address.util.GuiSettings;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * The controller for the Main Window. Provides layout provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends BaseUiPart {
    private static AppLogger logger = LoggerManager.getLogger(MainWindow.class);
    private static final String ICON = "/images/address_book_32.png";
    private static final String FXML = "MainWindow.fxml";
    public static final int MIN_HEIGHT = 600;
    public static final int MIN_WIDTH = 450;
    public static final String PERSON_LIST_PANEL_PLACEHOLDER_ID = "#personListPanel";

    private MainApp mainApp; //TODO: remove this dependency as per TODOs given in methods below
    private Ui ui; //TODO: remove this dependency as per TODOs given in methods below

    //Link to the model
    private ModelManager modelManager;

    //Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;

    //Handles to elements of this Ui container
    private VBox rootLayout;
    private Scene scene;


    @FXML
    private MenuItem helpMenuItem;

    public MainWindow() {
        super();
    }

    @Override
    public void setNode(Node node) {
        rootLayout = (VBox) node;
    }

    @Override
    public String getFxmlPath() {
        return FXML;
    }

    public void configure(String appTitle, UserPrefs prefs, MainApp mainApp,
                          Ui ui, ModelManager modelManager) {
        //Set connections
        this.mainApp = mainApp;
        this.ui = ui;
        this.modelManager = modelManager;

        //Configure the UI
        setTitle(appTitle);
        setIcon(ICON);
        setStageMinSize();
        setStageDefaultSize(prefs);
        scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
    }

    public void fillInnerParts() {
        createPersonListPanel();
    }

    /**
     * Shows the person list panel inside the root layout.
     */
    public PersonListPanel createPersonListPanel() {
        logger.debug("Loading person list panel.");
        PersonListPanel personListPanel =
                ViewLoader.loadView(primaryStage, getPersonListSlot(), new PersonListPanel());
        personListPanel.configure(ui, modelManager, modelManager.getAllViewablePersonsReadOnly());
        return personListPanel;
    }

    public void hide() {
        primaryStage.hide();
    }

    public GuiSettings getCurrentGuiSetting() {
        return new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
    }

    @Subscribe
    private void handleResizeAppRequestEvent(ResizeAppRequestEvent event) {
        logger.debug("Handling the resize app window request");
        Platform.runLater(this::handleResizeRequest);
    }

    @Subscribe
    private void handleMinimizeAppRequestEvent(MinimizeAppRequestEvent event) {
        logger.debug("Handling the minimize app window request");
        Platform.runLater(this::minimizeWindow);
    }

    public void setKeyEventHandler(EventHandler<? super KeyEvent> handler){
        scene.setOnKeyPressed(handler);
    }

    //TODO: to be removed with more specific method e.g. getListPanelSlot
    public AnchorPane getAnchorPane(String anchorPaneId) {
        return (AnchorPane) rootLayout.lookup(anchorPaneId);
    }

    public AnchorPane getPersonListSlot() {
        return getAnchorPane(PERSON_LIST_PANEL_PLACEHOLDER_ID);
    }

    private void setTitle(String appTitle) {
        primaryStage.setTitle(appTitle);
    }

    protected void setStageDefaultSize(UserPrefs prefs) {
        primaryStage.setHeight(prefs.getGuiSettings().getWindowHeight());
        primaryStage.setWidth(prefs.getGuiSettings().getWindowWidth());
        if (prefs.getGuiSettings().getWindowCoordinates() != null) {
            primaryStage.setX(prefs.getGuiSettings().getWindowCoordinates().getX());
            primaryStage.setY(prefs.getGuiSettings().getWindowCoordinates().getY());
        }
    }

    private void setStageMinSize() {
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
    }

    public void show() {
        primaryStage.show();
    }

    public void minimizeWindow() {
        primaryStage.setIconified(true);
        primaryStage.setMaximized(false);
    }

    public void handleResizeRequest() {
        logger.info("Handling resize request.");
        if (primaryStage.isIconified()) {
            logger.debug("Cannot resize as window is iconified, attempting to show window instead.");
            primaryStage.setIconified(false);
        } else {
            resizeWindow();
        }
    }

    public void resizeWindow() {
        logger.info("Resizing window");
        // specially handle since stage operations on Mac seem to not be working as intended
        if (commons.OsDetector.isOnMac()) {
            // refresh stage so that resizing effects (apart from the first resize after iconify-ing) are applied
            // however, this will cause minor flinching in window visibility
            primaryStage.hide(); // hide has to be called before setMaximized,
            // or first resize attempt after iconify-ing will resize twice
            primaryStage.show();

            // on Mac, setMaximized seems to work like "setResize"
            // isMaximized also does not seem to return the correct value
            primaryStage.setMaximized(true);
        } else {
            primaryStage.setMaximized(!primaryStage.isMaximized());
        }

        logger.debug("Stage width: {}", primaryStage.getWidth());
        logger.debug("Stage height: {}", primaryStage.getHeight());
    }

    public void setAccelerators() {
        helpMenuItem.setAccelerator(KeyBindingsManager.getAcceleratorKeyCombo("HELP_PAGE_ACCELERATOR").get());
    }

    @FXML
    private void handleHelp() {
        logger.debug("Showing help page about the application.");
        HelpWindow helpWindow = ViewLoader.loadView(primaryStage, new HelpWindow());
        helpWindow.configure();
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