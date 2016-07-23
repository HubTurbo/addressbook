package address.controller;

import address.model.UserPrefs;
import javafx.stage.Stage;

/**
 * The main Window of the App.
 */
public class MainWindow extends BaseUiPart {
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
}
