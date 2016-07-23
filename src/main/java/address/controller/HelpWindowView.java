package address.controller;

import address.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by dcsdcr on 23/7/2016.
 */
public class HelpWindowView {
    private static final String FXML_HELP = "/view/HelpWindow.fxml";
    private static final String ICON_HELP = "/images/help_icon.png";
    AnchorPane page;

    public HelpWindowView() {
        final String fxmlResourcePath = FXML_HELP;
        // Load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = loadFxml(fxmlResourcePath);
        page = (AnchorPane) loadLoader(loader, "Error loading help page");
    }

    public void show() {
        Scene scene = new Scene(page);
        Stage dialogStage = loadDialogStage("Help", null, scene);
        dialogStage.getIcons().add(getImage(ICON_HELP));
        dialogStage.setMaximized(true);
        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();
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

    private Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

}
