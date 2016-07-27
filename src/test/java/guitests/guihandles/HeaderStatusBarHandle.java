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

    public static String formatEditSuccessMessage(String firstName, Optional<String> secondName) {
        return "Edit" + " Person [ " + getFormattedNames(firstName, secondName)
                + " ] completed successfully.";
    }

    public static String formatEditCancelledMessage(String firstName, Optional<String> secondName) {
        return "Edit" + " Person [ " + getFormattedNames(firstName, secondName) + " ] was cancelled.";
    }

    public static String formatAddSuccessMessage(String firstName, Optional<String> secondName) {
        return "Add" + " Person [ " + getFormattedNames(firstName, secondName)
                + " ] completed successfully.";
    }

    public static String formatAddCancelledMessage(String firstName, Optional<String> secondName) {
        return "Add" + " Person [ " + getFormattedNames(firstName, secondName) + " ] was cancelled.";
    }

    public static String formatDeleteSuccessMessage(String firstName, Optional<String> secondName) {
        return "Delete" + " Person [ " + getFormattedNames(firstName, secondName)
                + " ] completed successfully.";
    }

    public static String formatDeleteCancelledMessage(String firstName, Optional<String> secondName) {
        return "Delete" + " Person [ " + getFormattedNames(firstName, secondName) + " ] was cancelled.";
    }

    private static String getFormattedNames(String firstName, Optional<String> secondName) {
        return firstName + (secondName.isPresent() ? " -> " + secondName.get() : "");
    }
}
