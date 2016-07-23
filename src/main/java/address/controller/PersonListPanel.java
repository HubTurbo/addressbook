package address.controller;

import address.model.ModelManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.util.collections.UnmodifiableObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * Created by dcsdcr on 23/7/2016.
 */
public class PersonListPanel {

    private PersonListPanelView personListPanelView;
    private PersonListPanelController personListPanelController;

    public PersonListPanel(AnchorPane pane, MainController mainController, ModelManager modelManager, UnmodifiableObservableList<ReadOnlyViewablePerson> personList) {
        personListPanelView = new PersonListPanelView(pane);
        personListPanelController = personListPanelView.getLoader().getController();
        personListPanelController.setConnections(mainController, modelManager, personList);
    }
}
