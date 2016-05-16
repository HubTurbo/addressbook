package address.exceptions;

import address.model.Person;

/**
 * Signifies an attempt to add a duplicate Person
 */
public class DuplicatePersonException extends Exception {

    public final Person offender;

    public DuplicatePersonException(Person offender) {
        this.offender = offender;
    }

    @Override
    public String toString() {
        return offender + " already exists!";
    }
}
