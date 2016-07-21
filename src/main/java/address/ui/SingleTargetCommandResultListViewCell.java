package address.ui;

import address.controller.ActivityHistoryCardController;
import address.events.SingleTargetCommandResultEvent;
import javafx.scene.control.ListCell;

/**
 *
 */
public class SingleTargetCommandResultListViewCell extends ListCell<SingleTargetCommandResultEvent> {

    @Override
    protected void updateItem(SingleTargetCommandResultEvent item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setGraphic(null);
            setText("");
        } else {
            setGraphic(new ActivityHistoryCardController(item).getLayout());
        }
    }
}
