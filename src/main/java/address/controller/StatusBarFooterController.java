package address.controller;

import address.events.sync.SyncCompletedEvent;
import address.events.sync.SyncFailedEvent;
import address.events.sync.SyncStartedEvent;
import address.events.update.ApplicationUpdateFailedEvent;
import address.events.update.ApplicationUpdateFinishedEvent;
import address.events.update.ApplicationUpdateInProgressEvent;
import address.util.*;
import com.google.common.eventbus.Subscribe;
import commons.DateTimeUtil;
import commons.FxViewUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextAlignment;
import org.controlsfx.control.StatusBar;

import java.util.concurrent.*;

public class StatusBarFooterController extends UiController {

    @FXML
    private AnchorPane updaterStatusBarPane;
    @FXML
    private AnchorPane syncStatusBarPane;

    public static StatusBar syncStatusBar;
    public static StatusBar updaterStatusBar;

    private TickingTimer timer;
    private long updateIntervalInSecs;

    private final Label secondaryStatusBarLabel;

    public StatusBarFooterController() {
        super();
        this.secondaryStatusBarLabel = new Label("");
    }

    private void initSyncTimer(long updateInterval) {
        updateIntervalInSecs = (int) DateTimeUtil.millisecsToSecs(updateInterval);
        timer = new TickingTimer("Sync timer", (int) updateIntervalInSecs,
                this::syncSchedulerOntick, this::syncSchedulerOnTimeOut, TimeUnit.SECONDS);
    }

    private void syncSchedulerOntick(int onTick) {
        Platform.runLater(() -> syncStatusBar.setText(String.format("Synchronization with server in %d secs.",
                                                                    onTick)));
    }

    private void syncSchedulerOnTimeOut() {
        Platform.runLater(() -> syncStatusBar.setText("Synchronization starting..."));
        timer.pause();
    }

    /**
     * Initializes the status bar
     * @param updateInterval The sync period
     * @param addressBookName name of the active address book
     */
    public void init(long updateInterval, String addressBookName) {
        initSyncTimer(updateInterval);
        initStatusBar();
        initAddressBookLabel(addressBookName);
    }

    private void initAddressBookLabel(String addressBookName) {
        updateSaveLocationDisplay(addressBookName);
        secondaryStatusBarLabel.setTextAlignment(TextAlignment.LEFT);
        setTooltip(secondaryStatusBarLabel);
    }

    private void initStatusBar() {
        this.syncStatusBar = new StatusBar();
        this.updaterStatusBar = new StatusBar();
        FxViewUtil.applyAnchorBoundaryParameters(syncStatusBar, 0.0, 0.0, 0.0, 0.0);
        FxViewUtil.applyAnchorBoundaryParameters(updaterStatusBar, 0.0, 0.0, 0.0, 0.0);
        syncStatusBarPane.getChildren().add(syncStatusBar);
        updaterStatusBarPane.getChildren().add(updaterStatusBar);
    }

    private void setTooltip(Label label) {
        Tooltip tp = new Tooltip();
        tp.textProperty().bind(label.textProperty());
        label.setTooltip(tp);
    }

    @Subscribe
    public void handleSyncingStartedEvent(SyncStartedEvent sse) {
        Platform.runLater(() -> syncStatusBar.setText(sse.toString()));
    }

    @Subscribe
    public void handleSyncCompletedEvent(SyncCompletedEvent sce) {
        Platform.runLater(() -> syncStatusBar.setText(sce.toString()));
        if (timer.isStarted()) {
            timer.restart();
            if (timer.isPaused()) {
                timer.resume();
            }
        } else {
            timer.start();
        }
    }

    @Subscribe
    public void handleSyncFailedEvent(SyncFailedEvent sfe) {
        Platform.runLater(() -> syncStatusBar.setText(sfe.toString()));
        if (timer.isStarted()) {
            timer.restart();
            if (timer.isPaused()) {
                timer.resume();
            }
        } else {
            timer.start();
        }
    }


    @Subscribe
    private void handleApplicationUpdateFailedEvent(ApplicationUpdateFailedEvent aufe) {
        Platform.runLater(() -> {
            updaterStatusBar.setText(aufe.getMessage());
            updaterStatusBar.setProgress(0);
            showSecondaryStatusBarLabel();
        });
    }

    @Subscribe
    private void handleApplicationUpdateInProgressEvent(ApplicationUpdateInProgressEvent auipe) {
        Platform.runLater(() -> {
            updaterStatusBar.setText(auipe.getMessage());
            updaterStatusBar.setProgress(auipe.getProgress());
        });
    }

    @Subscribe
    private void handleApplicationUpdateFinishedEvent(ApplicationUpdateFinishedEvent aufe) {
        Platform.runLater(() -> {
            updaterStatusBar.setText(aufe.getMessage());
            updaterStatusBar.setProgress(0);
            showSecondaryStatusBarLabel();
        });
    }

    private void showSecondaryStatusBarLabel() {
        secondaryStatusBarLabel.setVisible(true);
        updaterStatusBar.getRightItems().add(secondaryStatusBarLabel);
    }

    private void updateSaveLocationDisplay(String addressBookName) {
        secondaryStatusBarLabel.setText(addressBookName);
    }
}
