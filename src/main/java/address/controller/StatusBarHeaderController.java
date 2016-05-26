package address.controller;

import address.status.PersonCreatedStatus;
import address.status.PersonDeletedStatus;
import address.status.PersonEditedStatus;
import javafx.application.Platform;
import org.controlsfx.control.StatusBar;

public class StatusBarHeaderController {

    private StatusBar footerStatusBar;

    public StatusBarHeaderController() {
        footerStatusBar = new StatusBar();
        footerStatusBar.setText("");
    }

    public StatusBar getFooterStatusBarView() {
        return footerStatusBar;
    }

    public void postStatus(PersonEditedStatus e) {
        Platform.runLater(() -> footerStatusBar.setText(e.toString()));
    }

    public void postStatus(PersonDeletedStatus e) {
        Platform.runLater(() -> footerStatusBar.setText(e.toString()));
    }

    public void postStatus(PersonCreatedStatus e) {
        Platform.runLater(() -> footerStatusBar.setText(e.toString()));
    }
}
