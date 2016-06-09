package address.status;

import address.model.datatypes.person.ReadOnlyPerson;

/**
 * An event triggered when an AddressBook's contact is created.
 */
public class PersonCreatedStatus extends PersonBaseStatus {

    public PersonCreatedStatus(ReadOnlyPerson person) {
        super(person);
    }

    @Override
    public String toString() {
        return person + " was created";
    }
}
