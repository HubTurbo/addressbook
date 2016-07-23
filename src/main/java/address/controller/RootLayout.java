package address.controller;

import address.MainApp;
import address.model.ModelManager;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The root layout of the main App Window.
 */
public class RootLayout extends BaseUiPart{
    private RootLayoutView view;
    private RootLayoutController controller;

    public RootLayout(Stage primaryStage, MainApp mainApp, MainController mainController, ModelManager modelManager) {
        super(primaryStage);
        view = new RootLayoutView(primaryStage);
        controller = view.getLoader().getController();
        controller.setConnections(mainApp, mainController, modelManager);
    }

    public void setKeyEventHandler(EventHandler<? super KeyEvent> handler) {
        view.setKeyEventHandler(handler);
    }

    public void setAccelerators() {
        controller.setAccelerators();
    }

    //TODO: to be removed with more specific method e.g. getListPanelSlot
    public AnchorPane getAnchorPane(String anchorPaneId) {
        return view.getAnchorPane(anchorPaneId);
    }

    public AnchorPane getPersonListSlot() {
        return view.getPersonListSlot();
    }
}
