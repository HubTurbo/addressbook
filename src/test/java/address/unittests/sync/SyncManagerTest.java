package address.unittests.sync;

import address.events.CreatePersonOnRemoteRequestEvent;
import address.events.EventManager;
import address.events.SyncFailedEvent;
import address.model.datatypes.person.Person;
import address.sync.RemoteManager;
import address.sync.SyncManager;
import address.sync.task.CreatePersonOnRemoteTask;
import address.util.Config;
import com.google.common.eventbus.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.*;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CreatePersonOnRemoteTask.class, SyncManager.class})
public class SyncManagerTest {
    RemoteManager remoteManager;
    SyncManager syncManager;
    ExecutorService executorService;
    ScheduledExecutorService scheduledExecutorService;
    Config config;

    private int syncFailedEventCount;

    @Subscribe
    public void handleSyncFailedEvent(SyncFailedEvent sfe) {
        syncFailedEventCount++;
    }

    @Before
    public void setup() {
        EventManager.getInstance().registerHandler(this);
        syncFailedEventCount = 0;
        remoteManager = mock(RemoteManager.class);
        executorService = spy(Executors.newCachedThreadPool());
        scheduledExecutorService = mock(ScheduledExecutorService.class);
        config = new Config();
        config.simulateUnreliableNetwork = false;
        config.updateInterval = 1;

        syncManager = new SyncManager(config, remoteManager, executorService, scheduledExecutorService, null);
    }

    @Test
    public void createPerson_raiseEvent_correctPersonReturned() throws Exception {
        Person createdPerson = new Person("firstName", "lastName", 1);

        CreatePersonOnRemoteTask createPersonOnRemoteTask = mock(CreatePersonOnRemoteTask.class);
        whenNew(CreatePersonOnRemoteTask.class).withArguments(any(), any(), any()).thenReturn(createPersonOnRemoteTask);
        doReturn(createdPerson).when(createPersonOnRemoteTask).call();
        CompletableFuture<Person> resultContainer = new CompletableFuture<>();
        EventManager.getInstance().post(new CreatePersonOnRemoteRequestEvent(resultContainer, "addressBook", createdPerson));

        sleep(1000);
        assertTrue(resultContainer.isDone());
        assertEquals(createdPerson, resultContainer.get());
    }

    @Test
    public void getUpdates_noActiveAddressBook_syncFailed() {
        when(scheduledExecutorService.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Runnable task = (Runnable) args[0];
            task.run();
            return null;
        });
        syncManager.start();
        assertEquals(1, syncFailedEventCount);
    }

    @After
    public void tearDown() {
        EventManager.clearSubscribers();
    }
}
