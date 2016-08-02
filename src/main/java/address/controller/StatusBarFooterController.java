package address.controller;

import address.events.sync.SyncCompletedEvent;
import address.events.sync.SyncFailedEvent;
import address.events.sync.SyncStartedEvent;
import address.util.TickingTimer;
import com.google.common.eventbus.Subscribe;
import commons.DateTimeUtil;
import commons.FxViewUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.StatusBar;

import java.util.concurrent.TimeUnit;

public class StatusBarFooterController extends UiController {

    private static StatusBar syncStatusBar;
    private static StatusBar saveLocStatusBar;

    @FXML
    private AnchorPane saveLocStatusBarPane;
    @FXML
    private AnchorPane syncStatusBarPane;

    private TickingTimer timer;
    private long updateIntervalInSecs;

    public StatusBarFooterController() {
        super();
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

    private void initAddressBookLabel(String addressBookName) {
        updateSaveLocationDisplay(addressBookName);
        setTooltip(saveLocStatusBar);
    }

    private void initStatusBar() {
        this.syncStatusBar = new StatusBar();
        this.saveLocStatusBar = new StatusBar();
        FxViewUtil.applyAnchorBoundaryParameters(syncStatusBar, 0.0, 0.0, 0.0, 0.0);
        FxViewUtil.applyAnchorBoundaryParameters(saveLocStatusBar, 0.0, 0.0, 0.0, 0.0);
        syncStatusBarPane.getChildren().add(syncStatusBar);
        saveLocStatusBarPane.getChildren().add(saveLocStatusBar);
    }

    private void setTooltip(StatusBar statusBar) {
        Tooltip tp = new Tooltip();
        tp.textProperty().bind(statusBar.textProperty());
        statusBar.setTooltip(tp);
    }

    private void updateSaveLocationDisplay(String addressBookName) {
        saveLocStatusBar.setText(addressBookName);
    }
}
