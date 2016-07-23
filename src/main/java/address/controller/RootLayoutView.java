package address.controller;

import address.MainApp;
import address.events.KeyBindingEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by dcsdcr on 23/7/2016.
 */
public class RootLayoutView {
    private static final String FXML_ROOT_LAYOUT = "/view/RootLayout.fxml";

    private VBox rootLayout;
    private FXMLLoader loader;
    private Scene scene;

    public RootLayoutView(Stage primaryStage) {
        loader = loadFxml(FXML_ROOT_LAYOUT);
        rootLayout = (VBox) loadLoader(loader, "Error initializing root layout");
        // Show the scene containing the root layout.
        scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
    }

    public void setKeyEventHandler(EventHandler<? super KeyEvent> handler){
        scene.setOnKeyPressed(handler);
    }

    public FXMLLoader getLoader(){
        return loader;
    }

    private FXMLLoader loadFxml(String fxmlResourcePath) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
        return loader;
    }

    private Node loadLoader(FXMLLoader loader, String errorMsg ) {
        try {
            return loader.load();
        } catch (IOException e) {
            /* TODO: deal with error handling
            logger.fatal(errorMsg + ": {}", e);
            showFatalErrorDialogAndShutdown("FXML Load Error", errorMsg,
                    "IOException when trying to load ", loader.getLocation().toExternalForm());
            */
            return null;
        }
    }

    public AnchorPane getAnchorPane(String anchorPaneId) {
        return (AnchorPane) rootLayout.lookup(anchorPaneId);
    }
}
