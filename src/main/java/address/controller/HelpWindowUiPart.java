package address.controller;

import javafx.stage.Stage;

/**
 * The Help Window of the App.
 */
public class HelpWindowUiPart extends BaseUiPart{
    private HelpWindowView view;
    private HelpWindowController controller;

    public HelpWindowUiPart(Stage primaryStage) {
        super(primaryStage);
        view = new HelpWindowView(primaryStage);
        controller = view.getLoader().getController();
    }

    public void show() {
        view.show();
    }
}
