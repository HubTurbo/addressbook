package address.ui;

import address.MainApp;
import address.browser.BrowserManager;
import address.controller.MainController;
import address.model.ModelManager;
import address.model.UserPrefs;
import address.util.Config;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * The UI of the app.
 */
public class Ui {
    MainController mainController;
    UserPrefs pref;

    public Ui(MainApp mainApp, ModelManager modelManager, Config config, UserPrefs pref){
        mainController = new MainController(mainApp, modelManager, config, pref);
        this.pref = pref;
    }

    public void start(Stage primaryStage) {
        mainController.start(primaryStage);
    }

    public void showAlertDialogAndWait(Alert.AlertType alertType, String alertTitle, String headerText, String contentText) {
        mainController.showAlertDialogAndWait(alertType, alertTitle, headerText, contentText);
    }

    public void stop() {
        ImmutablePair<Double, Double> pair = new ImmutablePair<>(mainController.getPrimaryStage().getWidth(),
                                               mainController.getPrimaryStage().getHeight());
        pref.setScreenSize(pair);
        mainController.stop();
    }
}
