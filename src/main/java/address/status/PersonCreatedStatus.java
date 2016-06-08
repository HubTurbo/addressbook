package address.status;

import address.model.datatypes.ReadablePerson;

/**
 * An event triggered when an AddressBook's contact is created.
 */
public class PersonCreatedStatus extends PersonBaseStatus {

    public PersonCreatedStatus(ReadablePerson person) {
        super(person);
    }

    @Override
    public String toString() {
        return person + " was created";
    }
}
