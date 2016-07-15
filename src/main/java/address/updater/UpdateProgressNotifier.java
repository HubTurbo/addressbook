package address.updater;

import java.util.function.Consumer;

/**
 * This class is meant as a way for the updater to notify the main application of its progress
 */
public class UpdateProgressNotifier {

    public enum Status {
        IN_PROGRESS, FINISHED, FAILED
    }

    private Consumer<String> messageReader;
    private Consumer<Long> progressReader;
    private Consumer<Status> statusReader;

    public UpdateProgressNotifier(Consumer<String> messageReader,
                                  Consumer<Long> progressReader,
                                  Consumer<Status> statusReader) {
        this.messageReader = messageReader;
        this.progressReader = progressReader;
        this.statusReader = statusReader;
    }

    private void sendUpdateMessage(String updateMessage) {
        messageReader.accept(updateMessage);
    }

    private void sendUpdateProgress(long updateProgress) {
        progressReader.accept(updateProgress);
    }

    private void sendStatus(Status updateStatus) {
        statusReader.accept(updateStatus);
    }

    public void sendStatusFinished(String message) {
        sendUpdateMessage(message);
        sendUpdateProgress(new Long("0"));
        sendStatus(Status.FINISHED);
    }

    public void sendStatusFailed(String message) {
        sendUpdateMessage(message);
        sendUpdateProgress(new Long("0"));
        sendStatus(Status.FAILED);
    }

    public void sendStatusInProgress(String message, long progress) {
        sendUpdateMessage(message);
        sendUpdateProgress(progress);
        sendStatus(Status.IN_PROGRESS);
    }
}
