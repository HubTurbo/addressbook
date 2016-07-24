package address.controller;

import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import address.util.AppLogger;
import address.util.LoggerManager;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by damithch on 7/24/2016.
 */
public class PersonEditDialogUiPart extends BaseUiPart {
    private static final AppLogger logger = LoggerManager.getLogger(PersonEditDialogUiPart.class);
    private PersonEditDialogController controller;
    private PersonEditDialogView view;

    public PersonEditDialogUiPart(Stage primaryStage, ReadOnlyPerson initialData, List<Tag> tags) {
        super(primaryStage);
        view = new PersonEditDialogView(primaryStage);
        controller = view.getLoader().getController();

        controller.setDialogStage(view.getDialogStage());
        controller.setInitialPersonData(initialData);
        controller.setTags(tags, new ArrayList<>(initialData.getObservableTagList()));
    }


    public Optional<ReadOnlyPerson> getInput() {
        view.showAndWait();
        if (controller.isOkClicked()) {
            logger.debug("Person collected: " + controller.getEditedPerson().toString());
            return Optional.of(controller.getEditedPerson());
        } else {
            return Optional.empty();
        }
    }
}
