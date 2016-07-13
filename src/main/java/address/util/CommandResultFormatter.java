package address.util;

import address.model.SingleTargetCommandResult;

/**
 * Contains method to present the SingleTargetCommandResult data.
 */
public final class CommandResultFormatter {

    public static String getStringRepresentation(SingleTargetCommandResult result) {
        StringBuilder sb = new StringBuilder();

        sb.append(getNameRepresentation(result.targetNameBeforeExecution, result.targetNameAfterExecution));

        sb.append(" has been ");

        switch(result.commandTypeString.toLowerCase()) {
            case "add":
                sb.append("added");
                break;
            case "edit":
                sb.append("edited");
                break;
            case "delete":
                sb.append("deleted");
                break;
        }

        sb.append(" ");

        switch(result.status) {
            case SUCCESSFUL:
                sb.append("successfully");
                break;
            case FAILED:
                sb.append("unsuccessfully");
                break;
            case CANCELLED:
                return result.commandTypeString + " operation on " + result.targetNameBeforeExecution + " has been "
                       + result.status.toString().toLowerCase() + ".";
        }
        return sb.toString() + ".";
    }

    private static String getNameRepresentation(String targetNameBefore, String targetNameAfter) {
        if (targetNameAfter.equals(targetNameBefore)) {
            return targetNameAfter;
        }

        return targetNameBefore + " (old)" + " -> " + targetNameAfter + " (new)";
    }

}
