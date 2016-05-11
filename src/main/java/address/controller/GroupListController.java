package address.controller;

import address.model.ContactGroup;
import address.model.ModelManager;
import address.ui.GroupListViewCell;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;

public class GroupListController {
    @FXML
    private ListView<ContactGroup> groups;

    private ModelManager modelManager;
    private MainController mainController;
    private Stage stage;
    private List<ContactGroup> groupList;

    @FXML
    private void initialize() {
    }

    public void setGroups(ObservableList<ContactGroup> groupList, MainController mainController, ModelManager modelManager) {
        this.groupList = groupList;
        groups.setItems(groupList);
        groups.setCellFactory(listItem -> new GroupListViewCell(mainController, modelManager));
    }

    public void setDialogStage(Stage dialogStage) {
        this.stage = dialogStage;
    }

    public void setModelManager(ModelManager modelManager) {
        this.modelManager = modelManager;
    }

}
