package address.ui;

import address.MainApp;
import address.browser.BrowserManager;
import address.controller.MainController;
import address.model.ModelManager;
import address.util.Config;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * The UI of the app.
 */
public class Ui {
    MainController mainController;

    public Ui(MainApp mainApp, ModelManager modelManager, Config config){
        mainController = new MainController(mainApp, modelManager, config);
    }

    public void start(Stage primaryStage) {
        mainController.start(primaryStage);
    }

    public void showAlertDialogAndWait(Alert.AlertType alertType, String alertTitle, String headerText, String contentText) {
        mainController.showAlertDialogAndWait(alertType, alertTitle, headerText, contentText);
    }

    public void stop() {
        mainController.stop();
    }
}
