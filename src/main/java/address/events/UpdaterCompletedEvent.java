package address.events;

/**
 * An event triggered when Updater has finished updating.
 */
public class UpdaterCompletedEvent extends BaseEvent {

    @Override
    public String toString() {
        return "Updating completed.";
    }
}
