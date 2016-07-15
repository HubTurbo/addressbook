package address.events;

/**
 * An event triggered when installer is updating.
 */
public class UpdaterInProgressEvent extends BaseEvent{

    private double progress;
    private String message;

    public UpdaterInProgressEvent(String message, double progress) {
        this.message = message;
        this.progress = progress;
    }

    @Override
    public String toString() {
        return message;
    }

    public double getProgress() {
        return progress;
    }
}
