package address.events;

/**
 * An event triggered when an update is completed.
 */
public class SyncUpdateCompletedEvent<T> extends BaseEvent {
    private T data;
    private String message;

    public SyncUpdateCompletedEvent(T data, String message) {
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
