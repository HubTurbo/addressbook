package address.events;

import address.model.Person;

/**
 * An event triggered when an AddressBook's contact is created.
 */
public class ContactCreatedEvent extends ContactBaseEvent {

    public ContactCreatedEvent(Person person) {
        super(person);
    }

    @Override
    public String toString() {
        return String.format("%s %s is created", person.getFirstName(), person.getLastName());
    }
}
