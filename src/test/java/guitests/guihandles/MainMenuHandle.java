package guitests.guihandles;

import guitests.GuiTestBase;

import java.util.Arrays;

/**
 * Provides a handle to the main menu of the app.
 */
public class MainMenuHandle extends GuiHandle {
    public MainMenuHandle(GuiTestBase guiTestBase) {
        super(guiTestBase);
    }

    public GuiHandle clickOn(String... menuText) {
        Arrays.stream(menuText).forEach((menuItem) -> guiTestBase.clickOn(menuItem));
        return this;
    }
}
