package address.events.sync;

import address.events.BaseEvent;

/**
 * Event to request for changing of active address book
 */
public class ChangeActiveAddressBookRequestEvent extends BaseEvent {
    private String activeAddressBookName;

    public ChangeActiveAddressBookRequestEvent(String activeAddressBookName) {
        this.activeAddressBookName = activeAddressBookName;
    }

    public String getActiveAddressBookName() {
        return activeAddressBookName;
    }

    @Override
    public String toString() {
        return null;
    }
}
