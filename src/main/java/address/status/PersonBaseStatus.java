package address.status;

import address.events.BaseEvent;
import address.model.Person;

/**
 * A base event for Contact
 */
public abstract class PersonBaseStatus {

    protected Person person;

    public PersonBaseStatus(Person person) {
        this.person = person;
    }
}
