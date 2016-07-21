package guitests.guihandles;

import address.TestApp;
import address.controller.MainController;
import guitests.GuiRobot;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;

/**
 * A handler for the HeaderStatusBar of the UI
 */
public class HeaderStatusBarHandle extends GuiHandle {

    public static final String HEADER_STATUS_BAR_FIELD_ID = "#headerStatusBar";

    public HeaderStatusBarHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage, TestApp.APP_TITLE);
    }

    public String getText() {
        return getStatusBar().getText();
    }

    private StatusBar getStatusBar() {
        return (StatusBar) getNode(HEADER_STATUS_BAR_FIELD_ID);
    }
}
