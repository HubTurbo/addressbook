package address.events;

/**
 * Represents a user request to maximize the app window
 */
public class MaximizeAppRequestEvent extends BaseEvent {
    @Override
    public String toString() {
        return "Request to maximize app window";
    }
}
