package address.controller;

import address.model.ModelManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.ui.Ui;
import address.util.collections.UnmodifiableObservableList;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The Person List Panel in the UI.
 */
public class PersonListPanel extends BaseUiPart {

    private PersonListPanelView view;
    private PersonListPanelController controller;

    public PersonListPanel(Stage primaryStage, AnchorPane pane, Ui ui,
                           ModelManager modelManager, UnmodifiableObservableList<ReadOnlyViewablePerson> personList) {
        super(primaryStage);
        view = new PersonListPanelView(primaryStage, pane);
        controller = view.getLoader().getController();
        controller.setConnections(ui, modelManager, personList);
    }
}
