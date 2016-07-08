package guitests.guihandles;

import guitests.GuiRobot;
import javafx.stage.Stage;

/**
 * Provides a handle for the main GUI.
 */
public class MainGuiHandle {
    private final GuiRobot guiRobot;
    private MainMenuHandle mainMenu;
    private Stage primaryStage;

    public MainGuiHandle(GuiRobot guiRobot, Stage primaryStage) {
        this.guiRobot = guiRobot;
        this.mainMenu = new MainMenuHandle(guiRobot);
        this.primaryStage = primaryStage;
    }

    public PersonListPanelHandle getPersonListPanel(){
        return new PersonListPanelHandle(guiRobot);
    }

    public MainMenuHandle getMainMenu() {
        return mainMenu;
    }


    public boolean isMinimized() {
        return primaryStage.isIconified() && !primaryStage.isMaximized();
    }

    public boolean isMaximized() {
        return primaryStage.isMaximized() && !primaryStage.isIconified();
    }

    public boolean isDefaultSize() {
        return !primaryStage.isMaximized() && !primaryStage.isIconified();
    }
}
