package address.events;

public class ApplicationUpdateFailedEvent extends BaseEvent {
    private String message;

    public ApplicationUpdateFailedEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
