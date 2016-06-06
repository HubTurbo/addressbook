package address.unittests;

import address.sync.CloudSimulator;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CloudSimulatorTest {

    @Test
    public void testAdd() {
        CloudSimulator cloudSimulator = mock(CloudSimulator.class);
        cloudSimulator.createAddressBook("Test");
        verify(cloudSimulator).createAddressBook("Test");
    }
}
