package address.controller;

import javafx.stage.Stage;

/**
 * Base class for UI Parts.
 */
public class BaseUiPart {
    protected final Stage primaryStage;

    public BaseUiPart(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
