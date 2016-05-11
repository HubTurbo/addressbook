package address.ui;

import address.controller.GroupCardController;
import address.model.ContactGroup;
import javafx.scene.control.ListCell;

public class GroupListViewCell extends ListCell<ContactGroup> {
    @Override
    public void updateItem(ContactGroup group, boolean empty) {
        super.updateItem(group, empty);
        if (empty || group == null) {
            setGraphic(null);
            setText(null);
        } else {
            setGraphic(new GroupCardController(group).getLayout());
        }
    }
}
