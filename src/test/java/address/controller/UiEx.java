package address.controller;

import address.MainApp;
import address.model.ModelManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.ui.Ui;
import address.util.Config;

import java.util.List;

public class UiEx extends Ui {

    public UiEx(MainApp mainApp, ModelManager modelManager, Config config) {
        super(mainApp, modelManager, config);
    }

    public List<ReadOnlyViewablePerson> getDisplayedPersonList() {
        return getEnhancedPersonOverviewController().getDisplayedPersonList();
    }

    protected PersonOverviewControllerEn getEnhancedPersonOverviewController() {
        return new PersonOverviewControllerEn(mainController.personOverviewController);
    }

    public List<ReadOnlyViewablePerson> getSelectedPersons() {
        return getEnhancedPersonOverviewController().getSelectedPersons();
    }

    public ReadOnlyViewablePerson getFocusedPerson() {
        return getEnhancedPersonOverviewController().getFocusedPerson();
    }
}
