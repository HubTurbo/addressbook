package address.controller;

import address.events.*;
import address.prefs.PrefsManager;
import address.util.*;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextAlignment;
import org.controlsfx.control.StatusBar;

import java.util.concurrent.*;

public class StatusBarFooterController {

    @FXML
    private AnchorPane updaterStatusBarPane;

    @FXML
    private AnchorPane syncStatusBarPane;

    private static final String SAVE_LOC_TEXT_PREFIX = "Save File: ";
    private static final String LOC_TEXT_NOT_SET = "[NOT SET]";

    public static StatusBar syncStatusBar;
    public static StatusBar updaterStatusBar;

    private TickingTimer timer;
    private long updateIntervalInSecs;

    private final Label saveLocationLabel;

    public StatusBarFooterController() {
        EventManager.getInstance().registerHandler(this);
        this.saveLocationLabel = new Label();
    }

    private void setConfig(Config config) {
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

    /**
     * Initializes the status bar
     *
     * The given config object should have set updatedInterval
     *
     * @param config
     */
    public void initStatusBar(Config config) {
        setConfig(config);
        this.syncStatusBar = new StatusBar();
        this.updaterStatusBar = new StatusBar();
        FxViewUtil.applyAnchorBoundaryParameters(syncStatusBar, 0.0, 0.0, 0.0, 0.0);
        FxViewUtil.applyAnchorBoundaryParameters(updaterStatusBar, 0.0, 0.0, 0.0, 0.0);
        syncStatusBarPane.getChildren().add(syncStatusBar);
        updaterStatusBarPane.getChildren().add(updaterStatusBar);
        initSaveLocationLabel();
    }

    private void initSaveLocationLabel() {
        saveLocationLabel.setTextAlignment(TextAlignment.LEFT);
        setTooltip(saveLocationLabel);
        this.updaterStatusBar.getRightItems().add(saveLocationLabel);
        saveLocationLabel.setVisible(false);
    }

    private void setTooltip(Label label) {
        Tooltip tp = new Tooltip();
        tp.textProperty().bind(label.textProperty());
        label.setTooltip(tp);
    }

    @FXML
    private void initialize() {
        updateSaveLocationDisplay();
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
            updaterStatusBar.setText("");
            saveLocationLabel.setVisible(true);
        });
    }

    @Subscribe
    private void handleSaveLocationChangedEvent(SaveLocationChangedEvent e) {
        updateSaveLocationDisplay();
    }

    private void updateSaveLocationDisplay() {
        saveLocationLabel.setText(SAVE_LOC_TEXT_PREFIX + (PrefsManager.getInstance().isSaveLocationSet() ?
                PrefsManager.getInstance().getPrefs().getSaveLocation().getName() : LOC_TEXT_NOT_SET));
    }
}
