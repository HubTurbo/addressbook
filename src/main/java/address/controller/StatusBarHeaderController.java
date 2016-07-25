package address.controller;


import address.events.model.SingleTargetCommandResultEvent;
import address.util.CommandResultFormatter;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.controlsfx.control.StatusBar;

public class StatusBarHeaderController extends UiController{

    public static final String HEADER_STATUS_BAR_ID = "headerStatusBar";
    private static final String STATUS_BAR_STYLE_SHEET = "status-bar-with-border";
    private StatusBar headerStatusBar;
    private MainController mainController;
    private ObservableList<SingleTargetCommandResultEvent> finishedCommands;

    public StatusBarHeaderController(MainController mainController, ObservableList<SingleTargetCommandResultEvent> finishedCommands) {
        this.mainController = mainController;
        this.finishedCommands = finishedCommands;
        headerStatusBar = new StatusBar();
        headerStatusBar.setId(HEADER_STATUS_BAR_ID);
        headerStatusBar.getStyleClass().removeAll();
        headerStatusBar.getStyleClass().add(STATUS_BAR_STYLE_SHEET);
        headerStatusBar.setText("");
        headerStatusBar.setOnMouseClicked(event -> mainController.showActivityHistoryDialog());
        this.finishedCommands.addListener((ListChangeListener<SingleTargetCommandResultEvent>) c -> {
            SingleTargetCommandResultEvent lastCommandInfo = finishedCommands.get(finishedCommands.size() - 1);
            Platform.runLater(() -> headerStatusBar.setText(CommandResultFormatter.getStringRepresentation(lastCommandInfo)));
        });
    }

    public StatusBar getHeaderStatusBarView() {
        return headerStatusBar;
    }
}
