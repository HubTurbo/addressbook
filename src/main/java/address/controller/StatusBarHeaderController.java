package address.controller;


import address.events.SingleTargetCommandResultEvent;
import address.ui.Ui;
import address.util.CommandResultFormatter;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.controlsfx.control.StatusBar;

public class StatusBarHeaderController extends UiController{

    private StatusBar headerStatusBar;
    private Ui ui;
    private ObservableList<SingleTargetCommandResultEvent> finishedCommands;

    public StatusBarHeaderController(Ui ui, ObservableList<SingleTargetCommandResultEvent> finishedCommands) {
        this.ui = ui;
        this.finishedCommands = finishedCommands;
        headerStatusBar = new StatusBar();
        headerStatusBar.getStyleClass().removeAll();
        headerStatusBar.getStyleClass().add("status-bar-with-border");
        headerStatusBar.setText("");
        headerStatusBar.setOnMouseClicked(event -> ui.showActivityHistoryDialog());
        this.finishedCommands.addListener((ListChangeListener<SingleTargetCommandResultEvent>) c -> {
            SingleTargetCommandResultEvent lastCommandInfo = finishedCommands.get(finishedCommands.size() - 1);
            Platform.runLater(() -> headerStatusBar.setText(CommandResultFormatter.getStringRepresentation(lastCommandInfo)));
        });
    }

    public StatusBar getHeaderStatusBarView() {
        return headerStatusBar;
    }
}
