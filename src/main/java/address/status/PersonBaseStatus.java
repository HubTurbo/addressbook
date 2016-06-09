package address.status;

import address.model.datatypes.person.ReadOnlyPerson;

/**
 * A base event for Contact
 */
public abstract class PersonBaseStatus {

    protected ReadOnlyPerson person;

    public PersonBaseStatus(ReadOnlyPerson person) {
        this.person = person;
    }
}
