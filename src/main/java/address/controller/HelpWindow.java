package address.controller;

import javafx.stage.Stage;

/**
 * The Help Window of the App.
 */
public class HelpWindow extends BaseUiPart{
    private HelpWindowView helpWindowView;
    private HelpWindowController helpWindowController;

    public HelpWindow(Stage primaryStage) {
        super(primaryStage);
        helpWindowView = new HelpWindowView(primaryStage);
    }

    public void show() {
        helpWindowView.show();
    }
}
