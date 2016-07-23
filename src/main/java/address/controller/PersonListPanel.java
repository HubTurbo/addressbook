package address.controller;

import address.model.ModelManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.util.collections.UnmodifiableObservableList;
import javafx.scene.layout.AnchorPane;

/**
 * The Person List Panel in the UI.
 */
public class PersonListPanel  extends BaseUiPart{

    private PersonListPanelView view;
    private PersonListPanelController controller;

    public PersonListPanel(AnchorPane pane, MainController mainController, ModelManager modelManager, UnmodifiableObservableList<ReadOnlyViewablePerson> personList) {
        view = new PersonListPanelView(pane);
        controller = view.getLoader().getController();
        controller.setConnections(mainController, modelManager, personList);
    }
}
