package address.controller;

import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The View class for the {@link PersonListPanel}.
 */
public class PersonListPanelView extends BaseView{
    private VBox panel;

    @Override
    String getFxmlFileName() {
        return "PersonListPanel.fxml";
    }

    public PersonListPanelView(Stage primaryStage, AnchorPane pane) {
        super(primaryStage);
        panel = (VBox) mainNode;
        SplitPane.setResizableWithParent(pane, false);
        pane.getChildren().add(panel);
    }

}
