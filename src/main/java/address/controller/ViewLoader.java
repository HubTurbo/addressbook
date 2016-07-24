package address.controller;

import address.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * Created by dcsdcr on 24/7/2016.
 */
public class ViewLoader {
    private final static String FXML_FILE_FOLDER = "/view/";

    public static <T extends BaseUiController> T loadView(Stage primaryStage, T controllerSeed) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource(FXML_FILE_FOLDER + controllerSeed.getFxmlPath()));
        Node mainNode = loadLoader(loader, "Error loading " + controllerSeed.getFxmlPath());
        BaseUiController controller = loader.getController();
        controller.setNode(mainNode);
        controller.setStage(primaryStage);
        controller.init();
        return (T)controller;
    }


    protected static Node loadLoader(FXMLLoader loader, String errorMsg) {
        try {
            return loader.load();
        } catch (IOException e) {
            String errorMessage = "FXML Load Error " + errorMsg + " IOException when trying to load " + loader.getLocation().toExternalForm();
            throw new RuntimeException(errorMessage, e);
        }
    }

}
