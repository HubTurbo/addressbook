package address.controller;

import address.events.EventManager;
import javafx.stage.Stage;

/**
 * Base class for UI Parts.
 */
public class BaseUiPart {
    protected final Stage primaryStage;

    public BaseUiPart(Stage primaryStage) {
        this.primaryStage = primaryStage;
        EventManager.getInstance().registerHandler(this);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
