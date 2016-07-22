package address.events.update;

import address.events.BaseEvent;

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
