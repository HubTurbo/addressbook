package address.model;

import address.events.sync.CreatePersonOnRemoteRequestEvent;
import address.model.ChangeObjectInModelCommand.CommandState;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.Config;
import address.testutil.TestUtil;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AddPersonCommandTest {

    public static final String ADDRESSBOOK_NAME = "ADDRESSBOOK NAME";

    private static class InterruptAndTerminateException extends RuntimeException {}

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    ModelManager modelManagerMock;
    ModelManager modelManagerSpy;
    @Mock
    Config config;
    EventBus events;

    final Supplier<Optional<ReadOnlyPerson>> returnValidEmptyInput = () -> Optional.of(Person.createPersonDataContainer());

    private static Supplier<Optional<ReadOnlyPerson>> inputRetrieverWrapper(ReadOnlyPerson inputValue) {
        return () -> Optional.of(inputValue);
    }

    @BeforeClass
    public static void beforeSetup() throws TimeoutException {
        TestUtil.initRuntime();
    }

    @AfterClass
    public static void teardown() throws Exception {
        TestUtil.tearDownRuntime();
    }

    @Before
    public void setup() {
        when(config.getAddressBookName()).thenReturn(ADDRESSBOOK_NAME);
        modelManagerSpy = spy(new ModelManager(config));
        events = new EventBus();

        events.register(new Object() {
            @Subscribe
            public void fakeAddToRemote(CreatePersonOnRemoteRequestEvent e) {
                e.getReturnedPersonContainer().complete(e.getCreatedPerson());
            }
        });

    }

    @Test
    public void getTargetPersonId_throws_whenViewableNotCreatedYet() {
        final AddPersonCommand apc = new AddPersonCommand(0, null, 0, null, modelManagerMock, ADDRESSBOOK_NAME);
        thrown.expect(IllegalStateException.class);
        apc.getTargetPersonId();
    }

    @Test
    public void getTargetPersonId_returnsTempId_afterResultSimulatedAndBeforeRemoteChange() {
        final ViewablePerson createdViewable = ViewablePerson.withoutBacking(Person.createPersonDataContainer());
        final int CORRECT_ID = createdViewable.getId();
        final AddPersonCommand apc = spy(new AddPersonCommand(0, returnValidEmptyInput, 0, null, modelManagerMock, ADDRESSBOOK_NAME));

        when(modelManagerMock.addViewablePersonWithoutBacking(notNull(ReadOnlyPerson.class))).thenReturn(createdViewable);

        doThrow(InterruptAndTerminateException.class).when(apc).afterState(CommandState.SIMULATING_RESULT);
        thrown.expect(InterruptAndTerminateException.class);

        apc.run();
        assertEquals(apc.getTargetPersonId(), CORRECT_ID);
    }

    @Test
    public void getTargetPersonId_returnsFinalId_AfterSuccess() {
        final int CORRECT_ID = 1;
        events = new EventBus();
        events.register(new Object() {
            @Subscribe
            public void fakeAddToRemote(CreatePersonOnRemoteRequestEvent e) {
                e.getReturnedPersonContainer().complete(
                        new Person(CORRECT_ID).update(e.getCreatedPerson()));
            }
        });
        final AddPersonCommand apc = new AddPersonCommand(0, returnValidEmptyInput, 0, events::post, modelManagerSpy, ADDRESSBOOK_NAME);

        apc.run();
        assertEquals(apc.getTargetPersonId(), CORRECT_ID);
    }

    @Test
    public void retrievingInput_cancelsCommand_whenEmptyInputOptionalRetrieved() {
        final AddPersonCommand apc = new AddPersonCommand(0, Optional::empty, 0, e -> {}, modelManagerMock, ADDRESSBOOK_NAME);
        apc.run();
        assertEquals(apc.getState(), CommandState.CANCELLED);
    }

    @Test
    public void optimisticUiUpdate_simulatesCorrectData() {
        final ReadOnlyPerson inputData = TestUtil.generateSamplePersonWithAllData(0);
        final AddPersonCommand apc = spy(new AddPersonCommand(0, inputRetrieverWrapper(inputData), 0, null, modelManagerSpy, ADDRESSBOOK_NAME));

        // to stop the run at start of grace period (right after simulated change)
        doThrow(new InterruptAndTerminateException()).when(apc).afterState(CommandState.SIMULATING_RESULT);
        thrown.expect(InterruptAndTerminateException.class);

        apc.run();

        assertTrue(apc.getViewable().dataFieldsEqual(inputData)); // same data as input
        assertEquals(modelManagerSpy.visibleModel().getPersonList().size(), 1); // only 1 viewable
        assertTrue(modelManagerSpy.backingModel().getPersonList().isEmpty()); // simulation wont affect backing
        assertSame(modelManagerSpy.visibleModel().getPersonList().get(0), apc.getViewable()); // same ref
    }

    @Test
    public void successfulAdd_updatesBackingModelCorrectly() {
        final ReadOnlyPerson inputData = TestUtil.generateSamplePersonWithAllData(0);
        final AddPersonCommand apc = new AddPersonCommand(0, inputRetrieverWrapper(inputData), 0, events::post, modelManagerSpy, ADDRESSBOOK_NAME);

        apc.run();
        assertFalse(modelManagerSpy.personHasOngoingChange(apc.getViewable()));
        assertFinalStatesCorrectForSuccessfulAdd(apc, modelManagerSpy, inputData);
    }


    private void assertFinalStatesCorrectForSuccessfulAdd(AddPersonCommand command, ModelManager model, ReadOnlyPerson resultData) {
        assertEquals(command.getState(), CommandState.SUCCESSFUL);
        assertEquals(model.visibleModel().getPersonList().size(), 1); // only 1 viewable
        assertEquals(model.backingModel().getPersonList().size(), 1); // only 1 backing

        final ViewablePerson viewablePersonFromModel = model.visibleModel().getPersons().get(0);
        final Person backingPersonFromModel = model.backingModel().getPersons().get(0);
        assertSame(viewablePersonFromModel, command.getViewable()); // reference check
        assertSame(viewablePersonFromModel.getBacking(), backingPersonFromModel); // backing connected properly to visible
        assertTrue(viewablePersonFromModel.dataFieldsEqual(resultData));
        assertTrue(backingPersonFromModel.dataFieldsEqual(resultData));
    }

}
