package address.controller;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The View class for the RootLayout.
 */
public class RootLayoutView extends BaseView{

    private VBox rootLayout;
    private Scene scene;

    @Override
    String getFxmlFileName() {
        return "RootLayout.fxml";
    }

    public RootLayoutView(Stage primaryStage) {
        loadFxml();
        rootLayout = (VBox) loadLoader(loader, "Error initializing root layout");
        scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
    }

    public void setKeyEventHandler(EventHandler<? super KeyEvent> handler){
        scene.setOnKeyPressed(handler);
    }

    public AnchorPane getAnchorPane(String anchorPaneId) {
        return (AnchorPane) rootLayout.lookup(anchorPaneId);
    }
}
