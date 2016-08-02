package address.util;

import org.junit.Test;

import static address.model.ChangeObjectInModelCommand.CommandState.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests the CommandResultFormatter methods
 */
public class CommandResultFormatterTest {

    @Test
    public void testGetStringRepresentation_addType_success() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Add",
                SUCCESSFUL, "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Add Person [ Birdy Leow ] completed successfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_addType_fail() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Add",
                REQUEST_FAILED, "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Add Person [ Birdy Leow ] failed when making the change on the remote server.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_addType_cancelled() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Add",
                CANCELLED, "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Add Person [ Birdy Leow ] was cancelled.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_editType_success() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Edit",
                SUCCESSFUL, "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Edit Person [ Birdy Leow ] completed successfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_editType_fail() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Edit",
                REQUEST_FAILED, "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Edit Person [ Birdy Leow ] failed when making the change on the remote server.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_editType_cancelled() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Edit",
                CANCELLED, "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Edit Person [ Birdy Leow ] was cancelled.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_deleteType_success() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Delete",
                SUCCESSFUL, "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Delete Person [ Birdy Leow ] completed successfully.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_deleteType_fail() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Delete",
                REQUEST_FAILED, "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Delete Person [ Birdy Leow ] failed when making the change on the remote server.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testGetStringRepresentation_deleteType_cancelled() {
        SingleTargetCommandResultEvent command = new SingleTargetCommandResultEvent(1, "Delete",
                CANCELLED, "Person", "1", "Birdy Leow", "Birdy Leow");
        String expectedOutput = "Delete Person [ Birdy Leow ] was cancelled.";
        String output = CommandResultFormatter.getStringRepresentation(command);
        assertEquals(expectedOutput, output);
    }
}
