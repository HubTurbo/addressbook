package address.controller;

import address.MainApp;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Created by dcsdcr on 24/7/2016.
 */
public abstract class BaseUiController {
    Stage primaryStage;

    public abstract void setNode(Node node);

    public abstract String getFxmlPath();

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    protected Stage loadDialogStage(String value, Stage primaryStage, Scene scene) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(value);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.setScene(scene);
        return dialogStage;
    }

    protected Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

    protected void setIcon(String iconSource) {
        primaryStage.getIcons().add(getImage(iconSource));
    }

    protected void setIcon(Stage stage, String iconSource) {
        stage.getIcons().add(getImage(iconSource));
    }

    public abstract void init();
}
