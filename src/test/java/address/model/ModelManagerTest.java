package address.model;

import address.model.datatypes.person.ReadOnlyPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModelManagerTest {

    @Mock
    ModelManager modelMock;
    ModelManager modelSpy;

    @Before
    public void setup() {
        modelSpy = spy(new ModelManager(null));
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
