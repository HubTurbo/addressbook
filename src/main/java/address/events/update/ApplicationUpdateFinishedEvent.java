package address.events.update;

import address.events.BaseEvent;

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
