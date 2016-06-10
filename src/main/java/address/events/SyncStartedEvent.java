package address.events;

/**
 * Triggered when syncing is started
 */
public class SyncStartedEvent extends BaseEvent {

    @Override
    public String toString() {
        return "Synchronization with server has started.";
    }
}
