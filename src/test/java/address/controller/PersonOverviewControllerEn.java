package address.controller;

import address.model.datatypes.person.ReadOnlyViewablePerson;

import java.util.List;

public class PersonOverviewControllerEn {

    private PersonOverviewController personOverviewController;

    public PersonOverviewControllerEn(PersonOverviewController personOverviewController){
        this.personOverviewController = personOverviewController;
    }


    public List<ReadOnlyViewablePerson> getDisplayedPersonList() {
        return personOverviewController.personListView.getItems();
    }

    public List<ReadOnlyViewablePerson> getSelectedPersons(){
        return personOverviewController.personListView.getSelectionModel().getSelectedItems();
    }

    public ReadOnlyViewablePerson getFocusedPerson() {
        return personOverviewController.personListView.getFocusModel().getFocusedItem();
    }
}
