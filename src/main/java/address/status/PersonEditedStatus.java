package address.status;

import address.model.datatypes.person.ReadOnlyPerson;

/**
 * An event triggered when an AddressBook's contact is edited.
 */
public class PersonEditedStatus extends PersonBaseStatus {

    private ReadOnlyPerson uneditedPerson;

    public PersonEditedStatus(ReadOnlyPerson uneditedPerson, ReadOnlyPerson person) {
        super(person);
        this.uneditedPerson = uneditedPerson;
    }

    @Override
    public String toString() {
        if (uneditedPerson.equals(person)) {
            return person + " has been edited";
        } else {
            return uneditedPerson + " has been edited to " + person;
        }
    }
}
