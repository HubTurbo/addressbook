package address.events.sync;

import address.events.BaseEvent;

/**
 * Triggered when syncing is started
 */
public class SyncStartedEvent extends BaseEvent {

    @Override
    public String toString() {
        return "Synchronization with server has started.";
    }
}
