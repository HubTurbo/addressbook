package address.status;

import address.model.datatypes.Person;

/**
 * An event triggered when an AddressBook's contact is deleted.
 */
public class PersonDeletedStatus extends PersonBaseStatus {

    public PersonDeletedStatus(Person person) {
        super(person);
    }

    @Override
    public String toString() {
        return String.format("%s %s has been deleted.", person.getFirstName(), person.getLastName());
    }
}
