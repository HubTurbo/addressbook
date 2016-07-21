package guitests.guihandles;

import address.TestApp;
import address.keybindings.Bindings;
import commons.OsDetector;
import guitests.GuiRobot;
import javafx.stage.Stage;

/**
 * Provides a handle for the main GUI.
 */
public class MainGuiHandle extends GuiHandle{

    public MainGuiHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage, TestApp.APP_TITLE);
    }

    public PersonListPanelHandle getPersonListPanel(){
        return new PersonListPanelHandle(guiRobot, primaryStage);
    }

    public MainMenuHandle getMainMenu() {
        return new MainMenuHandle(guiRobot, primaryStage);
    }

    public HeaderStatusBarHandle getStatusBar() {
        return new HeaderStatusBarHandle(guiRobot, primaryStage);
    }

    public boolean isMinimized() {
        return primaryStage.isIconified() && !primaryStage.isMaximized();
    }

    public boolean isMaximized() {
        return primaryStage.isMaximized() && !primaryStage.isIconified();
    }

    public boolean isDefaultSize() {
        if (OsDetector.isOnMac()) {
            return !primaryStage.isIconified(); // TODO: Find a way to verify this on mac since isMaximized is always true
        } else {
            return !primaryStage.isMaximized() && !primaryStage.isIconified();
        }
    }

    public void use_APP_MINIMIZE_HOTKEY() {
        guiRobot.push(new Bindings().APP_MINIMIZE_HOTKEY.get(0));
        guiRobot.sleep(1000);
    }

    public void use_APP_RESIZE_HOTKEY() {
        guiRobot.push(new Bindings().APP_RESIZE_HOTKEY.get(0));
        guiRobot.sleep(1000);
    }

}
