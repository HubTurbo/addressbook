package address.events;

public class ApplicationUpdateFinishedEvent extends BaseEvent {
    private String message;

    public ApplicationUpdateFinishedEvent(String message) {
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
