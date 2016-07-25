package address.update;

import address.events.BaseEvent;
import address.events.EventManager;
import address.events.update.ApplicationUpdateFailedEvent;
import address.events.update.ApplicationUpdateFinishedEvent;
import address.events.update.ApplicationUpdateInProgressEvent;
import address.util.ManifestFileReader;
import com.google.common.eventbus.Subscribe;
import commons.FileUtil;
import commons.LocalUpdateSpecificationHelper;
import commons.Version;
import commons.VersionData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ManifestFileReader.class, LocalUpdateSpecificationHelper.class, FileUtil.class})
@PowerMockIgnore({"javax.management.*"})// Defer loading of javax.management.* in log4j to system class loader
public class UpdateManagerTest {
    private UpdateManager updateManager;
    private List<BaseEvent> events;

    @Subscribe
    private void handleApplicationUpdateFinishedEvent(ApplicationUpdateFinishedEvent e) {
        events.add(e);
    }

    @Subscribe
    private void handleApplicationUpdateFailedEvent(ApplicationUpdateFailedEvent e) {
        events.add(e);
    }

    @Subscribe
    private void handleApplicationUpdateInProgressEvent(ApplicationUpdateInProgressEvent e) {
        events.add(e);
    }

    @Before
    public void setup() {
        EventManager.getInstance().registerHandler(this);
        Version version = new Version(1, 1, 1, true);
        updateManager = new UpdateManager(version);
        events = new ArrayList<>();
    }

    @Test
    public void startUpdate_runFromJar_noUpdate() throws InterruptedException {
        mockStatic(ManifestFileReader.class);
        when(ManifestFileReader.isRunFromJar()).thenReturn(false);

        updateManager.start();
        sleep(1000);

        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ApplicationUpdateFinishedEvent);
        assertEquals("Developer env detected; not updating", events.get(0).toString());
    }

    @Test
    public void startUpdate_failedToClearSpecificationFile_failedUpdate() throws Exception {
        mockStatic(ManifestFileReader.class);
        when(ManifestFileReader.isRunFromJar()).thenReturn(true);
        mockStatic(LocalUpdateSpecificationHelper.class);
        doThrow(new IOException("Exception")).when(LocalUpdateSpecificationHelper.class, "clearLocalUpdateSpecFile");

        updateManager.start();
        sleep(1000);

        assertEquals(2, events.size());
        assertTrue(events.get(0) instanceof ApplicationUpdateInProgressEvent);
        assertTrue(events.get(1) instanceof ApplicationUpdateFailedEvent);
    }

    @Test
    public void startUpdate_failedToCreateSpecificationFile_failedUpdate() throws Exception {
        mockStatic(ManifestFileReader.class);
        when(ManifestFileReader.isRunFromJar()).thenReturn(true);
        mockStatic(FileUtil.class);
        doThrow(new IOException("Exception")).when(FileUtil.class, "createFile", any(File.class));

        updateManager.start();
        sleep(2000);

        assertEquals(3, events.size());
        assertTrue(events.get(0) instanceof ApplicationUpdateInProgressEvent);
        assertTrue(events.get(1) instanceof ApplicationUpdateInProgressEvent);
        assertTrue(events.get(2) instanceof ApplicationUpdateFailedEvent);
    }

    @Test
    public void startUpdate_failedToReadVersionData_failedUpdate() throws Exception {
        mockStatic(ManifestFileReader.class);
        when(ManifestFileReader.isRunFromJar()).thenReturn(true);
        mockStatic(FileUtil.class);
        doThrow(new IOException("Exception")).when(FileUtil.class, "deserializeObjectFromJsonFile", any(File.class), eq(VersionData.class));

        updateManager.start();
        sleep(2000);

        assertEquals(4, events.size());
        assertTrue(events.get(0) instanceof ApplicationUpdateInProgressEvent);
        assertTrue(events.get(1) instanceof ApplicationUpdateInProgressEvent);
        assertTrue(events.get(2) instanceof ApplicationUpdateInProgressEvent);
        assertTrue(events.get(3) instanceof ApplicationUpdateFailedEvent);
    }

    @After
    public void tearDown() {
        EventManager.clearSubscribers();
    }
}
