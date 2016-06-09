package address.exceptions;

import address.model.datatypes.person.ReadablePerson;

public class DuplicatePersonException extends DuplicateDataException {

    public final ReadablePerson offender;

    public DuplicatePersonException(ReadablePerson dup) {
        offender = dup;
    }

    @Override
    public String toString() {
        return "Duplicate person not allowed: " + offender + " already exists!";
    }
}
