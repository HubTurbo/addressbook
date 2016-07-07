package guitests.guihandles;

import guitests.GuiRobot;

/**
 * Provides a handle for the main GUI.
 */
public class MainGuiHandle {
    private final GuiRobot guiRobot;
    private MainMenuHandle mainMenu;

    public MainGuiHandle(GuiRobot guiRobot){
        this.guiRobot = guiRobot;
        this.mainMenu = new MainMenuHandle(guiRobot);
    }

    public PersonListPanelHandle getPersonListPanel(){
        return new PersonListPanelHandle(guiRobot);
    }

    public MainMenuHandle getMainMenu() {
        return mainMenu;
    }


    public boolean isMinimized() {
        return true;
        //TODO: implement this
    }

    public boolean isMaximized() {
        return true;
        //TODO: implement this
    }

    public boolean isDefaultSize() {
        return true;
        //TODO: implement this
    }
}
