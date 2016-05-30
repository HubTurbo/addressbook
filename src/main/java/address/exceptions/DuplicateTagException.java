package address.exceptions;

import address.model.datatypes.Tag;

public class DuplicateTagException extends DuplicateDataException {

    public final Tag offender;

    public DuplicateTagException(Tag tag) {
        offender = tag;
    }

    @Override
    public String toString() {
        return "Duplicate tag not allowed: " + offender + " already exists!";
    }
}
