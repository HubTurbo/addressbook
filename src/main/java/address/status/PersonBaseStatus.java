package address.status;

import address.model.datatypes.ReadablePerson;

/**
 * A base event for Contact
 */
public abstract class PersonBaseStatus {

    protected ReadablePerson person;

    public PersonBaseStatus(ReadablePerson person) {
        this.person = person;
    }
}
