package address.controller;

import address.model.datatypes.ContactGroup;
import address.model.ModelManager;
import address.ui.GroupListViewCell;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class GroupListController {
    @FXML
    private ListView<ContactGroup> groups;

    @FXML
    private void initialize() {
    }

    public void setGroups(ObservableList<ContactGroup> groupList, MainController mainController,
                          ModelManager modelManager) {
        groups.setItems(groupList);
        groups.setCellFactory(listItem -> new GroupListViewCell(mainController, modelManager));
    }
}
