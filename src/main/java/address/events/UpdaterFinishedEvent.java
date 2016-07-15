package address.events;

/**
 * An event triggered when UpdateManager has finished updating.
 */
public class UpdaterFinishedEvent extends BaseEvent {
    private String message;

    public UpdaterFinishedEvent(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
