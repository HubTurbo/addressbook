package guitests.guihandles;

import guitests.GuiTestBase;

/**
 * Provides a handle for the main GUI.
 */
public class MainGuiHandle {
    private final GuiTestBase guiTestBase;
    private MainMenuHandle mainMenu;

    public MainGuiHandle(GuiTestBase guiTestBase){
        this.guiTestBase = guiTestBase;
        this.mainMenu = new MainMenuHandle(guiTestBase);
    }

    public PersonListPanelHandle getPersonListPanel(){
        return new PersonListPanelHandle(guiTestBase);
    }

    public MainMenuHandle getMainMenu() {
        return mainMenu;
    }


}
