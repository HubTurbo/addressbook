package address.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This event is raised when SyncManager is asked to obtain updates
 * without any addressbook specified
 */
public class UpdateWithoutActiveSyncEvent extends BaseEvent {
    private static final Logger logger = LogManager.getLogger(UpdateWithoutActiveSyncEvent.class);
    private String exceptionMessage;

    public UpdateWithoutActiveSyncEvent() {
        this.exceptionMessage = "Update called without specifying active sync!";
    }

    @Override
    public String toString() {
        return exceptionMessage;
    }
}
