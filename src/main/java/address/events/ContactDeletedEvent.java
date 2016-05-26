package address.events;

import address.model.Person;

/**
 * An event triggered when an AddressBook's contact is deleted.
 */
public class ContactDeletedEvent extends ContactBaseEvent{

    public ContactDeletedEvent(Person person) {
        super(person);
    }

    @Override
    public String toString() {
        return String.format("%s %s has been deleted.", person.getFirstName(), person.getLastName());
    }
}
