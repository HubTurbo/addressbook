package address.status;

import address.model.datatypes.Person;

/**
 * A base event for Contact
 */
public abstract class PersonBaseStatus {

    protected Person person;

    public PersonBaseStatus(Person person) {
        this.person = person;
    }
}
