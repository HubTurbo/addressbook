package address.controller;

import address.model.CommandInfo;
import address.ui.CommandInfoListViewCell;
import address.util.FxViewUtil;
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

    private ObservableList<CommandInfo> infos;

    public void setConnections(ObservableList<CommandInfo> infos) {
        this.infos = infos;
    }

    public void init() {
        ListView<CommandInfo> listView = new ListView<>();
        listView.setItems(infos);
        listView.setCellFactory(lv -> new CommandInfoListViewCell());
        FxViewUtil.applyAnchorBoundaryParameters(listView, 0.0, 0.0, 0.0, 0.0);
        this.mainPane.getChildren().add(listView);
    }
}
