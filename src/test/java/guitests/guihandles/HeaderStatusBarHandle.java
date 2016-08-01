package guitests.guihandles;

import address.TestApp;
import address.controller.StatusBarHeaderController;
import guitests.GuiRobot;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;

import java.util.Optional;

/**
 * A handler for the HeaderStatusBar of the UI
 */
public class HeaderStatusBarHandle extends GuiHandle {

    public HeaderStatusBarHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage, TestApp.APP_TITLE);
    }

    public String getText() {
        return getStatusBar().getText();
    }

    private StatusBar getStatusBar() {
        return (StatusBar) getNode("#" + StatusBarHeaderController.HEADER_STATUS_BAR_ID);
    }

    public static String formatEditSuccessMessage(String firstFullName, Optional<String> secondFullName) {
        return "Edit" + " Person [ " + getFormattedNames(firstFullName, secondFullName) + " ] completed successfully.";
    }

    public static String formatEditCancelledMessage(String firstFullName, Optional<String> secondFullName) {
        return "Edit" + " Person [ " + getFormattedNames(firstFullName, secondFullName) + " ] was cancelled.";
    }

    public static String formatAddSuccessMessage(String firstFullName, Optional<String> secondFullName) {
        return "Add" + " Person [ " + getFormattedNames(firstFullName, secondFullName) + " ] completed successfully.";
    }

    public static String formatAddCancelledMessage(String firstFullName, Optional<String> secondFullName) {
        return "Add" + " Person [ " + getFormattedNames(firstFullName, secondFullName) + " ] was cancelled.";
    }

    public static String formatDeleteSuccessMessage(String firstFullName, Optional<String> secondFullName) {
        return "Delete" + " Person [ " + getFormattedNames(firstFullName, secondFullName) + " ] completed successfully.";
    }

    public static String formatDeleteCancelledMessage(String firstFullName, Optional<String> secondFullName) {
        return "Delete" + " Person [ " + getFormattedNames(firstFullName, secondFullName) + " ] was cancelled.";
    }

    private static String getFormattedNames(String firstFullName, Optional<String> secondFullName) {
        return firstFullName + (secondFullName.isPresent() ? " -> " + secondFullName.get() : "");
    }
}
