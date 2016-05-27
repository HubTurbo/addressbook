package address.controller;

import address.MainApp;
import address.events.*;
import address.util.Config;
import address.util.DateTimeUtil;
import address.util.FxViewUtil;
import address.util.TickingTimer;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextAlignment;
import org.controlsfx.control.StatusBar;

import java.util.concurrent.*;

public class StatusBarFooterController {

    @FXML
    private AnchorPane updaterStatusBarPane;

    @FXML
    private SplitPane statusBarFooter;

    @FXML
    private AnchorPane syncStatusBarPane;

    public static StatusBar syncStatusBar;
    public static StatusBar updaterStatusBar;

    private final TickingTimer timer;

    private final long updateIntervalInSecs;

    public StatusBarFooterController() {
        EventManager.getInstance().registerHandler(this);
        Config config = new Config();
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
    public void handleSyncingInProgressEvent(SyncInProgressEvent e) {
        Platform.runLater(() -> syncStatusBar.setText(e.toString()));
    }

    @Subscribe
    public void handleSyncCompletedEvent(SyncCompletedEvent e) {
        Platform.runLater(() -> syncStatusBar.setText(e.toString()));
        if (timer.isStarted()) {
            timer.restart();
            timer.resume();
        } else {
            timer.start();
        }
    }

    @Subscribe
    public void handleUpdaterInProgressEvent(UpdaterInProgressEvent e) {
        Platform.runLater(() -> {
            updaterStatusBar.setText(e.toString());
            updaterStatusBar.setProgress(e.getProgress());
        });
    }

    @Subscribe
    public void handleUpdaterCompletedEvent(UpdaterFinishedEvent e) {

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }

        Platform.runLater(() -> {
            updaterStatusBar.setText(e.toString());
            updaterStatusBar.setProgress(0.0);

            Label versionLabel = new Label(String.format("V%d.%d.%d", MainApp.VERSION_MAJOR, MainApp.VERSION_MINOR,
                    MainApp.VERSION_PATCH));
            versionLabel.setTextAlignment(TextAlignment.RIGHT);
            updaterStatusBar.setText("");
            updaterStatusBar.getRightItems().add(versionLabel);
        });
    }
}
