package address.controller;

import address.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by dcsdcr on 23/7/2016.
 */
public class PersonListPanelView {
    private static final String FXML_PERSON_LIST_PANEL = "/view/PersonListPanel.fxml";
    private FXMLLoader loader;

    public PersonListPanelView(AnchorPane pane) {
        loader = loadFxml(FXML_PERSON_LIST_PANEL);
        VBox personListPanel = (VBox) loadLoader(loader, "Error loading person list panel");
        SplitPane.setResizableWithParent(pane, false);
        pane.getChildren().add(personListPanel);
    }

    private FXMLLoader loadFxml(String fxmlResourcePath) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
        return loader;
    }

    public FXMLLoader getLoader(){
        return loader;
    }

    private Node loadLoader(FXMLLoader loader, String errorMsg) {
        try {
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            /* TODO: deal with error handling
            logger.fatal(errorMsg + ": {}", e);
            showFatalErrorDialogAndShutdown("FXML Load Error", errorMsg,
                    "IOException when trying to load ", loader.getLocation().toExternalForm());
            */
            return null;
        }
    }

    private Stage loadDialogStage(String value, Stage primaryStage, Scene scene) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(value);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.setScene(scene);
        return dialogStage;
    }
}
