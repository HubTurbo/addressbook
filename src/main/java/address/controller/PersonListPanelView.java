package address.controller;

import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * The View class for the {@link PersonListPanel}.
 */
public class PersonListPanelView extends BaseView{

    @Override
    String getFxmlFileName() {
        return "PersonListPanel.fxml";
    }

    public PersonListPanelView(AnchorPane pane) {
        loadFxml();
        VBox personListPanel = (VBox) loadLoader(loader, "Error loading person list panel");
        SplitPane.setResizableWithParent(pane, false);
        pane.getChildren().add(personListPanel);
    }

}
