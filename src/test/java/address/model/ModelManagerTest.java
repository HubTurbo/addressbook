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

}
