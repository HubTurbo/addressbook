package address.status;

import address.model.datatypes.Person;

/**
 * A base event for Contact
 */
public abstract class PersonBaseStatus extends BaseEvent{

    protected Person person;

    public PersonBaseStatus(Person person) {
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}
