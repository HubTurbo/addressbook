package guitests.guihandles;

import address.TestApp;
import address.keybindings.Bindings;
import address.model.ModelManager;
import address.util.LoggerManager;
import address.util.OsDetector;
import guitests.GuiRobot;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.testfx.api.FxRobot;

import java.util.concurrent.TimeUnit;

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

    public boolean isIconified() {
        return primaryStage.isIconified();
    }

    public void use_APP_MINIMIZE_HOTKEY() {
        guiRobot.push(new Bindings().APP_MINIMIZE_HOTKEY.get(0));
        guiRobot.sleep(1000);
    }

    public void use_APP_RESIZE_HOTKEY() {
        guiRobot.push(new Bindings().APP_RESIZE_HOTKEY.get(0));
        guiRobot.sleep(1000);
    }

    public FxRobot sleepForGracePeriod() {
        return guiRobot.sleep((ModelManager.GRACE_PERIOD_DURATION + 1), TimeUnit.SECONDS);
    }

    public FxRobot sleep(long duration, TimeUnit timeunit) {
        return guiRobot.sleep(duration, timeunit);
    }

    public void focusOnMainApp() {
        java.util.Optional<Window> window = guiRobot.listTargetWindows().stream().filter(w -> w instanceof Stage && ((Stage)w).getTitle().equals("Test App")).findAny();

        if(!window.isPresent()) {
            LoggerManager.getLogger(this.getClass()).fatal("Can't find Main App Stage");
            throw new RuntimeException("Can't find Main App Stage");
        }

        guiRobot.targetWindow(window.get());
        guiRobot.interact(() -> window.get().requestFocus());
        LoggerManager.getLogger(this.getClass()).debug("Focused target window name: "
                + ((Stage)guiRobot.targetWindow()).getTitle());
    }
}
