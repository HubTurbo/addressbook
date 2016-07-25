package address.events.model;

import address.events.BaseEvent;
import address.model.ChangeObjectInModelCommand.CommandState;

/**
 * Immutable data wrapper object representing the result of a completed command.
 */
public class SingleTargetCommandResultEvent extends BaseEvent {

    public enum ResultState {
        REMOTE_CONFLICT ("Remote Conflict"),
        REQUEST_FAILED ("Request Failed"),

        SUCCESSFUL ("Successful"),
        CANCELLED ("Cancelled");

        public static ResultState fromCommandState(CommandState commandState) {
            switch (commandState) {
                case CANCELLED :
                    return CANCELLED;
                case CONFLICT_FOUND :
                    return REMOTE_CONFLICT;
                case REQUEST_FAILED :
                    return REQUEST_FAILED;
                case SUCCESSFUL :
                    return SUCCESSFUL;
                default :
                    assert false : "This state is not terminal nor user intervention required";
                    throw new IllegalArgumentException();
            }
        }

        private final String descr;
        ResultState(String descr) {
            this.descr = descr;
        }
        @Override
        public String toString() {
            return descr;
        }
    }

    public SingleTargetCommandResultEvent(int commandId, String commandType, CommandState status, String targetType,
                                          String targetIdString, String targetNameBefore, String targetNameAfter) {
        this.commandId = commandId;
        this.targetIdString = targetIdString;
        this.status = ResultState.fromCommandState(status);
        commandTypeString = commandType;
        targetTypeString = targetType;
        targetNameBeforeExecution = targetNameBefore;
        targetNameAfterExecution = targetNameAfter;
    }
    
    /**
     * the command's unique identifier (1-indexed)
     */
    public final int commandId;

    /**
     * string representation of this command's type
     */
    public final String commandTypeString;

    /**
     * string representation of the final status of this command
     */
    public final ResultState status;

    /**
     * string representation of target's type
     */
    public final String targetTypeString;

    /**
     * string representation of target unique type-specific identifier
     */
    public final String targetIdString;

    /**
     * string representation of target's name before the command was applied.
     * will be same as {@link #targetNameAfterExecution} if there was no name change
     */
    public final String targetNameBeforeExecution;

    /**
     * string representation of target's name after the command terminated.
     * will be same as {@link #targetNameBeforeExecution} if there was no name change
     */
    public final String targetNameAfterExecution;

    @Override
    public String toString() {
        return String.format("User command #%d finished execution (%s): Target [%s %s]",
                commandId, status.toString(), targetTypeString, targetIdString);
    }
}
