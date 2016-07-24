package address.controller;

import address.MainApp; //TODO: remove this dependency
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Base class for all view classes
 */
public abstract class BaseView {
    protected FXMLLoader loader;
    private final static String FXML_FILE_FOLDER = "/view/";
    protected Node mainNode;
    protected Stage primaryStage;

    public BaseView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource(FXML_FILE_FOLDER + getFxmlFileName()));
        mainNode = loadLoader(loader, "Error loading " + getFxmlFileName());
    }

    abstract String getFxmlFileName();

    public FXMLLoader getLoader(){
        return loader;
    }

    protected Node loadLoader(FXMLLoader loader, String errorMsg) {
        try {
            return loader.load();
        } catch (IOException e) {
            String errorMessage = "FXML Load Error " + errorMsg + " IOException when trying to load " + loader.getLocation().toExternalForm();
            throw new RuntimeException(errorMessage, e);
        }
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

}
