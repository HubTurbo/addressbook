package address.controller;

import address.MainApp; //TODO: remove this dependency
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;

import java.io.IOException;

/**
 * Base class for all view classes
 */
public abstract class BaseView {
    protected FXMLLoader loader;
    private final static String FXML_FILE_FOLDER = "/view/";
    protected Node mainNode;

    public BaseView() {
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

    protected Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }
}
