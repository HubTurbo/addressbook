package address.controller;

import address.model.ContactGroup;
import address.model.ModelContactGroup;
import address.model.ModelManager;
import address.ui.GroupListViewCell;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class GroupListController {
    @FXML
    private ListView<ModelContactGroup> groups;

    @FXML
    private void initialize() {
    }

    public void setGroups(ObservableList<ModelContactGroup> groupList, MainController mainController,
                          ModelManager modelManager) {
        groups.setItems(groupList);
        groups.setCellFactory(listItem -> new GroupListViewCell(mainController, modelManager));
    }
}
