package address.util;

import address.model.SingleTargetCommandResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the CommandResultFormatter methods
 */
public class CommandResultFormatterTest {

    @Test
    public void testGetStringRepresentation_addType_success() {
        SingleTargetCommandResult command = new SingleTargetCommandResult(1, "Add",
                SingleTargetCommandResult.CommandStatus.SUCCESSFUL,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Birdy Leow has been added successfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_addType_fail() {
        SingleTargetCommandResult command = new SingleTargetCommandResult(1, "Add",
                SingleTargetCommandResult.CommandStatus.FAILED,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Birdy Leow has been added unsuccessfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_addType_cancelled() {
        SingleTargetCommandResult command = new SingleTargetCommandResult(1, "Add",
                SingleTargetCommandResult.CommandStatus.CANCELLED,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Add operation on Birdy Leow has been cancelled.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_editType_success() {
        SingleTargetCommandResult command = new SingleTargetCommandResult(1, "Edit",
                SingleTargetCommandResult.CommandStatus.SUCCESSFUL,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Birdy Leow has been edited successfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_editType_fail() {
        SingleTargetCommandResult command = new SingleTargetCommandResult(1, "Edit",
                SingleTargetCommandResult.CommandStatus.FAILED,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Birdy Leow has been edited unsuccessfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_editType_cancelled() {
        SingleTargetCommandResult command = new SingleTargetCommandResult(1, "Edit",
                SingleTargetCommandResult.CommandStatus.CANCELLED,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Edit operation on Birdy Leow has been cancelled.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_deleteType_success() {
        SingleTargetCommandResult command = new SingleTargetCommandResult(1, "Delete",
                SingleTargetCommandResult.CommandStatus.SUCCESSFUL,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Birdy Leow has been deleted successfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_deleteType_fail() {
        SingleTargetCommandResult command = new SingleTargetCommandResult(1, "Delete",
                SingleTargetCommandResult.CommandStatus.FAILED,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Birdy Leow has been deleted unsuccessfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_deleteType_cancelled() {
        SingleTargetCommandResult command = new SingleTargetCommandResult(1, "Delete",
                SingleTargetCommandResult.CommandStatus.CANCELLED,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Delete operation on Birdy Leow has been cancelled.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }
}
