package address.events;

/**
 * Triggered when syncing is in progress.
 */
public class SyncInProgressEvent extends BaseEvent {

    @Override
    public String toString() {
        return "Synchronization with server in progress.";
    }
}
