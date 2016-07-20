package address.controller;


import address.model.SingleTargetCommandResult;
import address.util.CommandResultFormatter;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.controlsfx.control.StatusBar;

public class StatusBarHeaderController extends UiController{

    private StatusBar headerStatusBar;
    private MainController mainController;
    private ObservableList<SingleTargetCommandResult> finishedCommands;

    public StatusBarHeaderController(MainController mainController, ObservableList<SingleTargetCommandResult> finishedCommands) {
        this.mainController = mainController;
        this.finishedCommands = finishedCommands;
        headerStatusBar = new StatusBar();
        headerStatusBar.getStyleClass().removeAll();
        headerStatusBar.getStyleClass().add("status-bar-with-border");
        headerStatusBar.setText("");
        headerStatusBar.setOnMouseClicked(event -> mainController.showActivityHistoryDialog());
        this.finishedCommands.addListener((ListChangeListener<SingleTargetCommandResult>) c -> {
            SingleTargetCommandResult lastCommandInfo = finishedCommands.get(finishedCommands.size() - 1);
            Platform.runLater(() -> headerStatusBar.setText(CommandResultFormatter.getStringRepresentation(lastCommandInfo)));
        });
    }

    public StatusBar getHeaderStatusBarView() {
        return headerStatusBar;
    }
}
