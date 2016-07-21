package commons;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This class is meant as a way for the update to notify the main application of its progress
 */
public class UpdateInformationNotifier {
    private Consumer<String> statusFinishedReader;
    private Consumer<String> statusFailedReader;
    private BiConsumer<String, Double> statusInProgressReader;
    private BiConsumer<String, Optional<String>> upgradeReader;

    public UpdateInformationNotifier(Consumer<String> statusFinishedReader,
                                     Consumer<String> statusFailedReader,
                                     BiConsumer<String, Double> statusInProgressReader,
                                     BiConsumer<String, Optional<String>> upgradeReader) {
        this.statusFinishedReader = statusFinishedReader;
        this.statusFailedReader = statusFailedReader;
        this.statusInProgressReader = statusInProgressReader;
        this.upgradeReader = upgradeReader;
    }

    public void sendStatusFinishedWithoutUpdaterUpgrade(String message, String launcherUpgrade) {
        statusFinishedReader.accept(message);
        upgradeReader.accept(launcherUpgrade, Optional.empty());
    }

    public void sendStatusFinishedWithoutUpdates(String message) {
        statusFinishedReader.accept(message);
    }

    /**
     * Notifies that the update download has been finished and components need to be upgraded
     *
     * @param message
     * @param launcherPath non-null
     * @param updaterPath null if not needed to upgrade
     */
    public void sendStatusFinishedWithUpdaterUpgrade(String message, String launcherPath, String updaterPath) {
        statusFinishedReader.accept(message);
        upgradeReader.accept(launcherPath, Optional.ofNullable(updaterPath));
    }

    public void sendStatusFailed(String message) {
        statusFailedReader.accept(message);
    }

    public void sendStatusInProgress(String message, double progress) {
        statusInProgressReader.accept(message, progress);
    }
}
