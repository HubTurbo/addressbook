package address.status;

import address.model.datatypes.ReadablePerson;

/**
 * An event triggered when an AddressBook's contact is edited.
 */
public class PersonEditedStatus extends PersonBaseStatus {

    private ReadablePerson uneditedPerson;

    public PersonEditedStatus(ReadablePerson uneditedPerson, ReadablePerson person) {
        super(person);
        this.uneditedPerson = uneditedPerson;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (uneditedPerson.equals(person)){
            return person + " has been edited";
        } else {
            return uneditedPerson + " has been edited to " + person;
        }
    }
}
