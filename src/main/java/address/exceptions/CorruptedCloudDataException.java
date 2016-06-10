package address.exceptions;

public class CorruptedCloudDataException extends Exception {
    public final String eventMessage;

    public CorruptedCloudDataException(String eventMessage) {
        this.eventMessage = eventMessage;
    }

    @Override
    public String toString() {
        return eventMessage;
    }
}
