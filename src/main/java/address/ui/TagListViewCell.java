package address.ui;

import address.controller.TagCardController;
import address.controller.MainController;
import address.model.datatypes.Tag;
import address.model.ModelManager;
import javafx.scene.control.ListCell;

public class TagListViewCell extends ListCell<Tag> {
    MainController mainController;
    ModelManager modelManager;

    public TagListViewCell(MainController mainController, ModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;
    }

    @Override
    public void updateItem(Tag tag, boolean empty) {
        super.updateItem(tag, empty);
        if (empty || tag == null) {
            setGraphic(null);
            setText(null);
        } else {
            setGraphic(new TagCardController(tag, mainController, modelManager).getLayout());
        }
    }
}
