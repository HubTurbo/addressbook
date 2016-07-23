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

    public static final String PERSON_LIST_PANEL_ID = "#personListPanel";
    private VBox rootLayout;
    private Scene scene;

    @Override
    String getFxmlFileName() {
        return "RootLayout.fxml";
    }

    public RootLayoutView(Stage primaryStage) {
        super();
        rootLayout = (VBox) mainNode;
        scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
    }

    public void setKeyEventHandler(EventHandler<? super KeyEvent> handler){
        scene.setOnKeyPressed(handler);
    }

    //TODO: to be removed with more specific method e.g. getListPanelSlot
    public AnchorPane getAnchorPane(String anchorPaneId) {
        return (AnchorPane) rootLayout.lookup(anchorPaneId);
    }

    public AnchorPane getPersonListSlot() {
        return getAnchorPane(PERSON_LIST_PANEL_ID);
    }
}
