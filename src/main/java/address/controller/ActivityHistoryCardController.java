package address.controller;

import address.model.SingleTargetCommandResult;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class ActivityHistoryCardController {

    @FXML
    private HBox mainPane;

    @FXML
    private Label activityLabel;

    private SingleTargetCommandResult result;

    public ActivityHistoryCardController(SingleTargetCommandResult result) {
        this.result = result;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/ActivityHistoryCard.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize() {
        activityLabel.setText(result.commandTypeString + " " + result.status.toString());
    }

    public HBox getLayout() {
        return mainPane;
    }

}
