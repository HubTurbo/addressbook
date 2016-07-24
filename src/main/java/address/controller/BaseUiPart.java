package address.controller;

import address.MainApp;
import address.events.BaseEvent;
import address.events.EventManager;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Base class for UI parts.
 */
public abstract class BaseUiPart {
    Stage primaryStage;

    public BaseUiPart(){
        EventManager.getInstance().registerHandler(this);
    }

    protected void raise(BaseEvent event){
        EventManager.getInstance().post(event);
    }

    protected void raisePotentialEvent(BaseEvent event) {
        EventManager.getInstance().postPotentialEvent(event);
    }

    public abstract void setNode(Node node);

    public abstract String getFxmlPath();

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    protected Stage loadDialogStage(String value, Stage parentStage, Scene scene) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(value);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(parentStage);
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

    public void setPlaceholder(AnchorPane placeholder) {
        //Do nothing by default.
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
