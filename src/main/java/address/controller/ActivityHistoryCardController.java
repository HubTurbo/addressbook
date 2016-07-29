package address.controller;

import address.events.model.SingleTargetCommandResultEvent;
import address.util.CommandResultFormatter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class ActivityHistoryCardController {

    @FXML
    private HBox activityHistoryCardMainBox;
    @FXML
    private Label activityLabel;

    private SingleTargetCommandResultEvent result;

    public ActivityHistoryCardController(SingleTargetCommandResultEvent result) {
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
        activityLabel.setText(CommandResultFormatter.getStringRepresentation(result));
    }

    public HBox getLayout() {
        return activityHistoryCardMainBox;
    }

}
