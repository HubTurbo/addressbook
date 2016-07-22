package address.controller;

import address.events.model.SingleTargetCommandResultEvent;
import address.ui.SingleTargetCommandResultListViewCell;
import commons.FxViewUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

/**
 *
 */
public class ActivityHistoryController {

    @FXML
    private AnchorPane mainPane;

    private ObservableList<SingleTargetCommandResultEvent> results;

    public void setConnections(ObservableList<SingleTargetCommandResultEvent> results) {
        this.results = results;
    }

    public void init() {
        ListView<SingleTargetCommandResultEvent> listView = new ListView<>();
        listView.setItems(results);
        listView.setCellFactory(lv -> new SingleTargetCommandResultListViewCell());
        FxViewUtil.applyAnchorBoundaryParameters(listView, 0.0, 0.0, 0.0, 0.0);
        this.mainPane.getChildren().add(listView);
    }
}
