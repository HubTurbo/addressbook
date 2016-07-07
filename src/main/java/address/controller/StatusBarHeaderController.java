package address.controller;

import address.status.PersonCreatedStatus;
import address.status.PersonDeletedStatus;
import address.status.PersonEditedStatus;
import javafx.application.Platform;
import org.controlsfx.control.StatusBar;

public class StatusBarHeaderController extends UiController{

    private StatusBar headerStatusBar;
    private MainController mainController;

    public StatusBarHeaderController(MainController mainController) {
        this.mainController = mainController;
        headerStatusBar = new StatusBar();
        headerStatusBar.getStyleClass().removeAll();
        headerStatusBar.getStyleClass().add("status-bar-with-border");
        headerStatusBar.setText("");
        headerStatusBar.setOnMouseClicked(event -> Platform.runLater(() ->mainController.showActivityHistoryDialog()));
    }

    public StatusBar getHeaderStatusBarView() {
        return headerStatusBar;
    }

    public void postStatus(PersonEditedStatus e) {
        Platform.runLater(() -> headerStatusBar.setText(e.toString()));
    }

    public void postStatus(PersonDeletedStatus e) {
        Platform.runLater(() -> headerStatusBar.setText(e.toString()));
    }

    public void postStatus(PersonCreatedStatus e) {
        Platform.runLater(() -> headerStatusBar.setText(e.toString()));
    }
}
