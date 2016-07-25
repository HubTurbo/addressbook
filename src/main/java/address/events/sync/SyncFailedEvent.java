package address.events.sync;

import address.events.BaseEvent;

public class SyncFailedEvent extends BaseEvent {
    String message;

    public SyncFailedEvent(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
