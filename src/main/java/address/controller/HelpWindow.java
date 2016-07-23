package address.controller;

/**
 * Created by dcsdcr on 23/7/2016.
 */
public class HelpWindow {
    private HelpWindowView helpWindowView;
    private HelpWindowController helpWindowController;

    public HelpWindow() {
        helpWindowView = new HelpWindowView();
    }

    public void show() {
        helpWindowView.show();
    }
}
