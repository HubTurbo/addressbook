package address.controller;

import address.model.CommandInfo;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.controlsfx.control.StatusBar;

public class StatusBarHeaderController extends UiController{

    private StatusBar headerStatusBar;
    private MainController mainController;
    private ObservableList<CommandInfo> finishedCommands;

    public StatusBarHeaderController(MainController mainController, ObservableList<CommandInfo> finishedCommands) {
        this.mainController = mainController;
        this.finishedCommands = finishedCommands;
        headerStatusBar = new StatusBar();
        headerStatusBar.getStyleClass().removeAll();
        headerStatusBar.getStyleClass().add("status-bar-with-border");
        headerStatusBar.setText("");
        headerStatusBar.setOnMouseClicked(event -> mainController.showActivityHistoryDialog());
        this.finishedCommands.addListener((ListChangeListener<CommandInfo>) c -> {
            CommandInfo lastCommandInfo = finishedCommands.get(finishedCommands.size() -1);
            Platform.runLater(() -> headerStatusBar.setText(lastCommandInfo.getName() + " "
                                                            + lastCommandInfo.statusString()));
        });
    }

    public StatusBar getHeaderStatusBarView() {
        return headerStatusBar;
    }
}
