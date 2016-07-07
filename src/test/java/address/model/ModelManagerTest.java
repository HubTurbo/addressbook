package address.model;

import address.model.datatypes.person.ReadOnlyPerson;
import address.util.Config;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModelManagerTest {

    @Mock
    ModelManager modelMock;
    @Mock
    Config config;
    ModelManager modelSpy;

    @Before
    public void setup() {
        when(config.getLocalDataFilePath()).thenReturn("MyAddressBook");
        modelSpy = spy(new ModelManager(config));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createPersonThroughUi_spawnsNewAddCommand() {
        doCallRealMethod().when(modelMock).createPersonThroughUI(any());
        Callable<Optional<ReadOnlyPerson>> addInputRetriever = Optional::empty;
        modelMock.createPersonThroughUI(addInputRetriever);
        verify(modelMock).execNewAddPersonCommand(notNull(Supplier.class));
    }

    @Test
    public void editPersonThroughUi_spawnsNewEditCommand_ifTargetHasNoChangeInProgress() {
//        doCallRealMethod().when(modelMock).editPersonThroughUI(any());

    }
}
