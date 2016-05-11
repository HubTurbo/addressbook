package address.controller;

import address.model.ContactGroup;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class GroupCardController {
    @FXML
    private VBox box;
    @FXML
    private Label groupName;

    public GroupCardController(ContactGroup group) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GroupListCard.fxml"));
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        groupName.setText(group.getName());
    }

    public VBox getLayout() {
        return box;
    }
}
