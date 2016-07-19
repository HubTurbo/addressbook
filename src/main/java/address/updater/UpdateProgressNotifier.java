package address.updater;

import java.util.function.Consumer;

import static address.updater.UpdateProgressNotifier.Status.FAILED;
import static address.updater.UpdateProgressNotifier.Status.FINISHED;
import static address.updater.UpdateProgressNotifier.Status.IN_PROGRESS;

public class UpdateProgressNotifier {
    public enum Status {
        IN_PROGRESS, FINISHED, FAILED
    }

    private Consumer<String> messageReader;
    private Consumer<Double> progressReader;
    private Consumer<Status> statusReader;

    public UpdateProgressNotifier(Consumer<String> messageReader,
                                     Consumer<Double> progressReader,
                                     Consumer<Status> statusReader) {
        this.messageReader = messageReader;
        this.progressReader = progressReader;
        this.statusReader = statusReader;
    }

    private void sendUpdateMessage(String updateMessage) {
        messageReader.accept(updateMessage);
    }

    private void sendUpdateProgress(double updateProgress) {
        progressReader.accept(updateProgress);
    }

    private void sendStatus(Status updateStatus) {
        statusReader.accept(updateStatus);
    }

    public void sendStatusFinished(String message) {
        sendUpdateMessage(message);
        sendUpdateProgress(0.0);
        sendStatus(FINISHED);
    }

    public void sendStatusFailed(String message) {
        sendUpdateMessage(message);
        sendUpdateProgress(0.0);
        sendStatus(FAILED);
    }

    public void sendStatusInProgress(String message, double progress) {
        sendUpdateMessage(message);
        sendUpdateProgress(progress);
        sendStatus(IN_PROGRESS);
    }
}
