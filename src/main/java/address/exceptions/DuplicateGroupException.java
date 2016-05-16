package address.exceptions;

import address.model.ContactGroup;

public class DuplicateGroupException extends DuplicateDataException {

    public final ContactGroup offender;

    public DuplicateGroupException(ContactGroup grp) {
        offender = grp;
    }

    @Override
    public String toString() {
        return "Duplicate contact group not allowed: " + offender + " already exists!";
    }
}
