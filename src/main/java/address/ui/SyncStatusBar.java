package address.ui;

import javafx.application.Platform;
import org.controlsfx.control.StatusBar;

/**
 * The AddressBook footer status bar to show periodic messages. (i.e. Sync status)
 */
public class SyncStatusBar extends StatusBar {

    public SyncStatusBar() {

    }

    /**
     * Display message to the status bar.
     * This can be called at Non-UI thread.
     */
    public void displayMessage(String message){
        Platform.runLater(() -> setText(message));
    }

    public void displayErrorMessage(String message){
        Platform.runLater(() -> setText(message));
        this.setProgress(0.0f);
        this.progressProperty().unbind();
    }

}
