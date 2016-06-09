package address.status;

import address.model.datatypes.person.ReadOnlyPerson;

/**
 * An event triggered when an AddressBook's contact is deleted.
 */
public class PersonDeletedStatus extends PersonBaseStatus {

    public PersonDeletedStatus(ReadOnlyPerson person) {
        super(person);
    }

    @Override
    public String toString() {
        return person + " has been deleted.";
    }
}
