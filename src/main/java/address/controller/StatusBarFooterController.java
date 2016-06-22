package address.controller;

import address.events.*;
import address.util.*;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextAlignment;
import org.controlsfx.control.StatusBar;

import java.util.concurrent.*;

public class StatusBarFooterController {

    @FXML
    private AnchorPane updaterStatusBarPane;

    @FXML
    private AnchorPane syncStatusBarPane;

    public static StatusBar syncStatusBar;
    public static StatusBar updaterStatusBar;

    private final TickingTimer timer;

    private final long updateIntervalInSecs;

    public StatusBarFooterController() {
        EventManager.getInstance().registerHandler(this);
        Config config = Config.getConfig();
        updateIntervalInSecs = (int) DateTimeUtil.millisecsToSecs(config.updateInterval);
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

    public void initStatusBar() {
        this.syncStatusBar = new StatusBar();
        this.updaterStatusBar = new StatusBar();

        FxViewUtil.applyAnchorBoundaryParameters(syncStatusBar, 0.0, 0.0, 0.0, 0.0);
        FxViewUtil.applyAnchorBoundaryParameters(updaterStatusBar, 0.0, 0.0, 0.0, 0.0);

        syncStatusBarPane.getChildren().add(syncStatusBar);
        updaterStatusBarPane.getChildren().add(updaterStatusBar);
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
    public void handleUpdaterInProgressEvent(UpdaterInProgressEvent uipe) {
        Platform.runLater(() -> {
            updaterStatusBar.setText(uipe.toString());
            updaterStatusBar.setProgress(uipe.getProgress());
        });
    }

    @Subscribe
    public void handleUpdaterCompletedEvent(UpdaterFinishedEvent ufe) {
        Platform.runLater(() -> {
            updaterStatusBar.setText(ufe.toString());
            updaterStatusBar.setProgress(0.0);

            // TODO make it wait for a while before showing version so update status can be read

            Label versionLabel = new Label(Version.getCurrentVersion().toString());
            versionLabel.setTextAlignment(TextAlignment.RIGHT);
            updaterStatusBar.setText("");
            updaterStatusBar.getRightItems().add(versionLabel);
        });
    }
}
