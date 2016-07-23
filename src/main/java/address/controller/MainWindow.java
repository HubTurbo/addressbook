package address.controller;

import address.model.UserPrefs;
import address.util.AppLogger;
import address.util.LoggerManager;
import javafx.stage.Stage;

/**
 * The main Window of the App.
 */
public class MainWindow extends BaseUiPart {
    private static final AppLogger logger = LoggerManager.getLogger(MainWindow.class);
    private static final String ICON_LOCATION = "/images/address_book_32.png";
    public static final int MIN_HEIGHT = 600;
    public static final int MIN_WIDTH = 450;

    public MainWindow(Stage primaryStage, String appTitle, UserPrefs prefs) {
        super(primaryStage);
        setTitle(appTitle);
        setIcon(ICON_LOCATION);
        setStageMinSize();
        setStageDefaultSize(prefs);
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

}
