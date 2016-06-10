package address.events;

/**
 * An event triggered when Syncing is completed.
 */
public class SyncCompletedEvent extends BaseEvent {

    @Override
    public String toString() {
        return "Synchronization is completed.";
    }
}