package address.model;

public class SingleTargetCommandResult {

    public enum CommandStatus {
        SUCCESSFUL ("Successful"),
        FAILED ("Failed"),
        CANCELLED ("Cancelled");

        private final String descr;
        CommandStatus(String descr) {
            this.descr = descr;
        }
        @Override
        public String toString() {
            return descr;
        }
    }

    SingleTargetCommandResult(int commandId, String commandType, CommandStatus status, String targetType,
                              String targetIdString, String targetNameBefore, String targetNameAfter) {
        this.commandId = commandId;
        this.targetIdString = targetIdString;
        this.status = status;
        commandTypeString = commandType;
        targetTypeString = targetType;
        targetNameBeforeExecution = targetNameBefore;
        targetNameAfterExecution = targetNameAfter;
        System.out.println("\n\t\tID: "+targetIdString+"\n\t\t"+targetNameBefore+"\n\t\t"+targetNameAfter+"\n");
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
    public final CommandStatus status;

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
     */
    public final String targetNameBeforeExecution;

    /**
     * string representation of target's name after the command terminated.
     */
    public final String targetNameAfterExecution;
}
