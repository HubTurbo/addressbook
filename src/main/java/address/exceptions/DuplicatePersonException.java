package address.exceptions;

import address.model.datatypes.Person;

public class DuplicatePersonException extends DuplicateDataException {

    public final Person offender;

    public DuplicatePersonException(Person dup) {
        offender = dup;
    }

    @Override
    public String toString() {
        return "Duplicate person not allowed: " + offender + " already exists!";
    }
}
