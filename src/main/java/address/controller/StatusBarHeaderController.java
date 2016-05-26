package address.controller;

import address.events.ContactCreatedEvent;
import address.events.ContactDeletedEvent;
import address.events.ContactEditedEvent;
import address.events.EventManager;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import org.controlsfx.control.StatusBar;

public class StatusBarHeaderController {

    private StatusBar footerStatusBar;

    public StatusBarHeaderController() {
        EventManager.getInstance().registerHandler(this);
        footerStatusBar = new StatusBar();
    }

    public StatusBar getFooterStatusBarView() {
        return footerStatusBar;
    }

    @Subscribe
    public void handleContactEditedEvent(ContactEditedEvent e) {
        Platform.runLater(() -> footerStatusBar.setText(e.toString()));
    }

    @Subscribe
    public void handleContactDeletedEvent(ContactDeletedEvent e) {
        Platform.runLater(() -> footerStatusBar.setText(e.toString()));
    }

    @Subscribe
    public void handleContactCreatedEvent(ContactCreatedEvent e) {
        Platform.runLater(() -> footerStatusBar.setText(e.toString()));
    }
}
