package address.exceptions;

import address.model.datatypes.AddressBook;

public class CorruptedCloudDataEvent extends Exception {
    public final AddressBook offender;

    public CorruptedCloudDataEvent(AddressBook data) {
        offender = data;
    }

    @Override
    public String toString() {
        return "Duplicate data detected in cloud data.";
    }
}
