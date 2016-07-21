package address.events;

public class ApplicationUpdateInProgressEvent extends BaseEvent {
    private String message;
    private double progress;

    public ApplicationUpdateInProgressEvent(String message, double progress) {
        this.message = message;
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public double getProgress() {
        return progress;
    }

    @Override
    public String toString() {
        return message + ": " + progress;
    }
}
