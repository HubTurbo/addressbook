package address.controller;

import address.model.CommandInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

/**
 * TODO: Bind CommandInfo. Currently CommandInfo doesn't use Property class yet.
 */
public class ActivityHistoryCardController {

    @FXML
    private HBox mainPane;

    @FXML
    private Label activityLabel;

    private CommandInfo info;

    public ActivityHistoryCardController(CommandInfo info) {
        this.info = info;
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
        activityLabel.setText(info.getName() + " " + info.statusString());
    }

    public HBox getLayout() {
        return mainPane;
    }

}
