package address.ui;

import address.controller.GroupCardController;
import address.controller.MainController;
import address.model.ContactGroup;
import address.model.ModelManager;
import javafx.scene.control.ListCell;

public class GroupListViewCell extends ListCell<ContactGroup> {
    MainController mainController;
    ModelManager modelManager;

    public GroupListViewCell(MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;
    }

    @Override
    public void updateItem(ContactGroup group, boolean empty) {
        super.updateItem(group, empty);
        if (empty || group == null) {
            setGraphic(null);
            setText(null);
        } else {
            setGraphic(new GroupCardController(group, mainController, modelManager).getLayout());
        }
    }
}
