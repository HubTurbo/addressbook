package address.events;

/**
 * An event triggered when an update for a particular resource is completed.
 */
public class SyncUpdateResourceCompletedEvent<T> extends BaseEvent {
    private T data;
    private String message;

    public SyncUpdateResourceCompletedEvent(T data, String message) {
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
