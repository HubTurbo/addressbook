package address.controller;

import address.model.ModelManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.util.collections.UnmodifiableObservableList;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The Person List Panel in the UI.
 */
public class PersonListPanel extends BaseUiPart {

    private PersonListPanelView view;
    private PersonListPanelController controller;

    public PersonListPanel(Stage primaryStage, AnchorPane pane, MainController mainController,
                           ModelManager modelManager, UnmodifiableObservableList<ReadOnlyViewablePerson> personList) {
        super(primaryStage);
        view = new PersonListPanelView(pane);
        controller = view.getLoader().getController();
        controller.setConnections(mainController, modelManager, personList);
    }
}
