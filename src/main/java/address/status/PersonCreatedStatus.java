package address.status;

import address.model.datatypes.Person;

/**
 * An event triggered when an AddressBook's contact is created.
 */
public class PersonCreatedStatus extends PersonBaseStatus {

    public PersonCreatedStatus(Person person) {
        super(person);
    }

    @Override
    public String toString() {
        return String.format("%s %s is created", person.getFirstName(), person.getLastName());
    }
}
