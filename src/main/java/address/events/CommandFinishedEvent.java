package address.events;

import address.model.SingleTargetCommandResult;

/**
 * Wraps result of a finished command
 */
public class CommandFinishedEvent extends BaseEvent {

    public final SingleTargetCommandResult result;

    public CommandFinishedEvent(SingleTargetCommandResult result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return String.format("User command #%d finished execution (%s): Target [%s %s]",
                result.commandId, result.status.toString(), result.targetTypeString, result.targetIdString);
    }
}
