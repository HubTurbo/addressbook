package address.events;

/**
 * An event triggered when an update is completed.
 */
public class UpdateCompletedEvent<T> extends BaseEvent {
    private T data;
    private String message;

    public UpdateCompletedEvent(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return message;
    }
}
