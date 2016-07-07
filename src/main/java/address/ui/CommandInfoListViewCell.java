package address.ui;

import address.model.CommandInfo;
import javafx.scene.control.ListCell;

/**
 *
 */
public class CommandInfoListViewCell extends ListCell<CommandInfo> {

    @Override
    protected void updateItem(CommandInfo item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setGraphic(null);
            setText("");
        } else {
            setText(item.getName() + " " + item.statusString());
        }
    }
}
