package guitests.guihandles;

import guitests.GuiRobot;
import javafx.stage.Stage;

/**
 * Provides a handle to the About dialog
 */
public class AboutDialogHandle extends GuiHandle {
    public AboutDialogHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage, "AddressApp");
    }
}
