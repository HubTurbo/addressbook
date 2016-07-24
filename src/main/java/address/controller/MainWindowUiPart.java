package address.controller;

import address.MainApp;
import address.events.MinimizeAppRequestEvent;
import address.events.ResizeAppRequestEvent;
import address.model.ModelManager;
import address.model.UserPrefs;
import address.ui.Ui;
import address.util.AppLogger;
import address.util.GuiSettings;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The root layout of the main App Window.
 */
public class MainWindowUiPart extends BaseUiPart{
    private static final AppLogger logger = LoggerManager.getLogger(MainWindowUiPart.class);

    private Ui ui; //TODO: remove this dependency

    private MainWindowView view;
    private MainWindowController controller;
    private PersonListPanel personListPanel;

    private ModelManager modelManager;

    public MainWindowUiPart(Stage primaryStage, String appTitle, UserPrefs prefs, MainApp mainApp,
                            Ui ui, ModelManager modelManager) {
        super(primaryStage);
        view = new MainWindowView(primaryStage, appTitle, prefs);
        controller = view.getLoader().getController();
        controller.setConnections(mainApp, ui, modelManager);
        controller.setStage(primaryStage);
        this.modelManager = modelManager;
        this.ui = ui;
    }

    public void setKeyEventHandler(EventHandler<? super KeyEvent> handler) {
        view.setKeyEventHandler(handler);
    }

    public void setAccelerators() {
        controller.setAccelerators();
    }

    public void fillInnerParts() {
        createPersonListPanel();
    }

    /**
     * Shows the person list panel inside the root layout.
     */
    public PersonListPanel createPersonListPanel() {
        logger.debug("Loading person list panel.");
        return new PersonListPanel(primaryStage, getPersonListSlot(), ui, modelManager, modelManager.getAllViewablePersonsReadOnly());
    }

    //TODO: to be removed with more specific method e.g. getListPanelSlot
    public AnchorPane getAnchorPane(String anchorPaneId) {
        return view.getAnchorPane(anchorPaneId);
    }

    public AnchorPane getPersonListSlot() {
        return view.getPersonListSlot();
    }

    public void show() {
        view.show();
    }

    public void minimizeWindow() {
        view.minimizeWindow();
    }

    public void handleResizeRequest() {
        view.handleResizeRequest();
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
}
