package address.events;

/**
 * Represents a user request to minimize the app window
 */
public class MinimizeAppRequestEvent extends BaseEvent {
    @Override
    public String toString() {
        return "Request to minimize app window";
    }
}
