package address.util;

import address.events.SingleTargetCommandResultEvent;

/**
 * Contains method to present the SingleTargetCommandResult data.
 */
public final class CommandResultFormatter {

    private static String getNameRepresentation(String targetNameBefore, String targetNameAfter) {
        if (targetNameAfter.equals(targetNameBefore)) {
            return targetNameAfter;
        }
        return targetNameBefore + " -> " + targetNameAfter;
    }

    public static String getStringRepresentation(SingleTargetCommandResultEvent result) {
        final StringBuilder sb = new StringBuilder();

        sb.append(result.commandTypeString).append(' ').append(result.targetTypeString).append(" [ ")
                .append(getNameRepresentation(result.targetNameBeforeExecution, result.targetNameAfterExecution))
                .append(" ] ");

        switch(result.status) {
            case SUCCESSFUL :
                sb.append("completed successfully.");
                break;
            case CANCELLED :
                sb.append("was cancelled.");
                break;
            case REMOTE_CONFLICT :
                sb.append("found conflicting unseen changes on the remomte server.");
                break;
            case REQUEST_FAILED :
                sb.append("failed when making the change on the remote server.");
                break;
            default:
                assert false : "Incomplete Implementation! Add handling for any new status enum cases.";
        }
        return sb.toString();
    }

}
