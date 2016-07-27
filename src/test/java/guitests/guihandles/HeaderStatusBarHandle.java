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

    public enum Type {
        ADD ("Add"), EDIT("Edit"), DELETE("Delete");

        String message;
        Type(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    public HeaderStatusBarHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage, TestApp.APP_TITLE);
    }

    public String getText() {
        return getStatusBar().getText();
    }

    private StatusBar getStatusBar() {
        return (StatusBar) getNode("#" + StatusBarHeaderController.HEADER_STATUS_BAR_ID);
    }

    public static String formatSuccessMessage(Type type, String firstName, Optional<String> secondName) {
        return type.toString() + " Person [ " + getFormattedNames(firstName, secondName)
                + " ] completed successfully.";
    }

    public static String formatCancelledMessage(Type type, String firstName, Optional<String> secondName) {
        return type.toString() + " Person [ " + getFormattedNames(firstName, secondName) + " ] was cancelled.";
    }

    private static String getFormattedNames(String firstName, Optional<String> secondName) {
        return firstName + (secondName.isPresent() ? " -> " + secondName.get() : "");
    }
}
