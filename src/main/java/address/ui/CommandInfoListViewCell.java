package address.ui;

import address.controller.ActivityHistoryCardController;
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
            setGraphic(new ActivityHistoryCardController(item).getLayout());
        }
    }
}
