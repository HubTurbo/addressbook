package address.events;

/**
 * An event triggered when Updater failed updating.
 */
public class UpdaterFailedEvent extends BaseEvent {
    private String message;

    public UpdaterFailedEvent(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
