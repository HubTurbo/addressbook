package address.controller;

import address.model.datatypes.tag.Tag;
import address.model.ModelManager;
import address.ui.TagListViewCell;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class TagListController {
    @FXML
    private ListView<Tag> tags;

    @FXML
    private void initialize() {
    }

    public void setTags(ObservableList<Tag> tagList, MainController mainController,
                        ModelManager modelManager) {
        tags.setItems(tagList);
        tags.setCellFactory(listItem -> new TagListViewCell(mainController, modelManager));
    }
}
