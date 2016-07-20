package address.ui;

import address.controller.ActivityHistoryCardController;
import address.model.SingleTargetCommandResult;
import javafx.scene.control.ListCell;

/**
 *
 */
public class SingleTargetCommandResultListViewCell extends ListCell<SingleTargetCommandResult> {

    @Override
    protected void updateItem(SingleTargetCommandResult item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setGraphic(null);
            setText("");
        } else {
            setGraphic(new ActivityHistoryCardController(item).getLayout());
        }
    }
}
