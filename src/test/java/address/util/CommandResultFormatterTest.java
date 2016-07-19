package address.util;

import address.events.SingleTargetCommandResultEvent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the CommandResultFormatter methods
 */
public class CommandResultFormatterTest {

    @Test
    public void testGetStringRepresentation_addType_success() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Add",
                SingleTargetCommandResultEvent.CommandStatus.SUCCESSFUL,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Birdy Leow has been added successfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_addType_fail() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Add",
                SingleTargetCommandResultEvent.CommandStatus.FAILED,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Birdy Leow has been added unsuccessfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_addType_cancelled() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Add",
                SingleTargetCommandResultEvent.CommandStatus.CANCELLED,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Add operation on Birdy Leow has been cancelled.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_editType_success() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Edit",
                SingleTargetCommandResultEvent.CommandStatus.SUCCESSFUL,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Birdy Leow has been edited successfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_editType_fail() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Edit",
                SingleTargetCommandResultEvent.CommandStatus.FAILED,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Birdy Leow has been edited unsuccessfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_editType_cancelled() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Edit",
                SingleTargetCommandResultEvent.CommandStatus.CANCELLED,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Edit operation on Birdy Leow has been cancelled.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_deleteType_success() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Delete",
                SingleTargetCommandResultEvent.CommandStatus.SUCCESSFUL,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Birdy Leow has been deleted successfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_deleteType_fail() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Delete",
                SingleTargetCommandResultEvent.CommandStatus.FAILED,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Birdy Leow has been deleted unsuccessfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_deleteType_cancelled() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Delete",
                SingleTargetCommandResultEvent.CommandStatus.CANCELLED,
                "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Delete operation on Birdy Leow has been cancelled.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }
}
