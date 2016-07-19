package commons;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This class is meant as a way for the updater to notify the main application of its progress
 */
public class UpdateInformationNotifier {
    private Consumer<String> statusFinishedReader;
    private Consumer<String> statusFailedReader;
    private BiConsumer<String, Double> statusInProgressReader;
    private Consumer<String> upgradeUpdaterReader;

    public UpdateInformationNotifier(Consumer<String> statusFinishedReader,
                                     Consumer<String> statusFailedReader,
                                     BiConsumer<String, Double> statusInProgressReader,
                                     Consumer<String> upgradeUpdaterReader) {
        this.statusFinishedReader = statusFinishedReader;
        this.statusFailedReader = statusFailedReader;
        this.statusInProgressReader = statusInProgressReader;
        this.upgradeUpdaterReader = upgradeUpdaterReader;
    }

    public void sendStatusFinishedWithoutUpgrade(String message) {
        statusFinishedReader.accept(message);
    }

    public void sendStatusFinishedWithUpgrade(String message, String upgradeUpdater) {
        statusFinishedReader.accept(message);
        upgradeUpdaterReader.accept(upgradeUpdater);
    }

    public void sendStatusFailed(String message) {
        statusFailedReader.accept(message);
    }

    public void sendStatusInProgress(String message, double progress) {
        statusInProgressReader.accept(message, progress);
    }
}
