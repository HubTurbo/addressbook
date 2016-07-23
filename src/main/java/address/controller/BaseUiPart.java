package address.controller;

import address.MainApp;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Base class for UI Parts.
 */
public class BaseUiPart {
    protected final Stage primaryStage;

    public BaseUiPart(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    protected void setIcon(String iconSource) {
        primaryStage.getIcons().add(getImage(iconSource));
    }

    private Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }
}
