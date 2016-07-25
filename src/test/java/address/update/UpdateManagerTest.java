package address.update;

import address.events.BaseEvent;
import address.events.EventManager;
import address.events.update.ApplicationUpdateFailedEvent;
import address.events.update.ApplicationUpdateFinishedEvent;
import address.events.update.ApplicationUpdateInProgressEvent;
import address.util.ManifestFileReader;
import com.google.common.eventbus.Subscribe;
import commons.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ManifestFileReader.class, LocalUpdateSpecificationHelper.class, FileUtil.class, OsDetector.class})
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
        sleep(2000);

        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ApplicationUpdateFinishedEvent);
        assertEquals("Developer env detected; not updating", events.get(0).toString());
    }

    @Test
    public void startUpdate_failedToClearSpecificationFile_failUpdate() throws Exception {
        mockStatic(ManifestFileReader.class);
        when(ManifestFileReader.isRunFromJar()).thenReturn(true);
        mockStatic(LocalUpdateSpecificationHelper.class);
        doThrow(new IOException("Exception")).when(LocalUpdateSpecificationHelper.class, "clearLocalUpdateSpecFile");

        updateManager.start();
        sleep(2000);

        assertEquals(2, events.size());
        assertTrue(events.get(0) instanceof ApplicationUpdateInProgressEvent); // Clear specifcation file
        assertTrue(events.get(1) instanceof ApplicationUpdateFailedEvent);     // Fail clearing of specification file
    }

    @Test
    public void startUpdate_failedToCreateVersionDataFile_failUpdate() throws Exception {
        mockStatic(ManifestFileReader.class);
        when(ManifestFileReader.isRunFromJar()).thenReturn(true);
        mockStatic(LocalUpdateSpecificationHelper.class);
        mockStatic(FileUtil.class);
        doThrow(new IOException("Exception")).when(FileUtil.class, "createFile", any(File.class));

        updateManager.start();
        sleep(2000);

        assertEquals(3, events.size());
        assertTrue(events.get(0) instanceof ApplicationUpdateInProgressEvent);  // Clear specification file
        assertTrue(events.get(1) instanceof ApplicationUpdateInProgressEvent);  // Create specification file
        assertTrue(events.get(2) instanceof ApplicationUpdateFailedEvent);      // Fail to create a new file
    }

    @Test
    public void startUpdate_failedToReadVersionData_failUpdate() throws Exception {
        mockStatic(ManifestFileReader.class);
        when(ManifestFileReader.isRunFromJar()).thenReturn(true);
        mockStatic(LocalUpdateSpecificationHelper.class);
        mockStatic(FileUtil.class);
        doThrow(new IOException("Exception")).when(FileUtil.class, "deserializeObjectFromJsonFile", any(File.class), eq(VersionData.class));

        updateManager.start();
        sleep(2000);

        assertEquals(4, events.size());
        assertTrue(events.get(0) instanceof ApplicationUpdateInProgressEvent);  // Clear specification file
        assertTrue(events.get(1) instanceof ApplicationUpdateInProgressEvent);  // Create & download specification file
        assertTrue(events.get(2) instanceof ApplicationUpdateInProgressEvent);  // Read version data from file
        assertTrue(events.get(3) instanceof ApplicationUpdateFailedEvent);      // Failed to read version data
    }

    @Test
    public void startUpdate_noNewerVersion_finishUpdate() throws Exception {
        mockStatic(ManifestFileReader.class);
        when(ManifestFileReader.isRunFromJar()).thenReturn(true);
        mockStatic(LocalUpdateSpecificationHelper.class);
        VersionData versionDataToReturn = new VersionData();
        versionDataToReturn.setVersion(new Version(1, 1, 1, true).toString());
        mockStatic(FileUtil.class);
        when(FileUtil.deserializeObjectFromJsonFile(any(File.class), eq(VersionData.class))).thenReturn(versionDataToReturn);

        updateManager.start();
        sleep(2000);

        assertEquals(6, events.size());
        assertTrue(events.get(0) instanceof ApplicationUpdateInProgressEvent);  // Clear specification file
        assertTrue(events.get(1) instanceof ApplicationUpdateInProgressEvent);  // Create & download specification file
        assertTrue(events.get(2) instanceof ApplicationUpdateInProgressEvent);  // Read version data from file
        assertTrue(events.get(3) instanceof ApplicationUpdateInProgressEvent);  // Read version from version data
        assertTrue(events.get(4) instanceof ApplicationUpdateInProgressEvent);  // Check version
        assertTrue(events.get(5) instanceof ApplicationUpdateFinishedEvent);    // No newer version
    }

    @Test
    public void startUpdate_unsupportedOs_failUpdate() throws Exception {
        mockStatic(ManifestFileReader.class);
        when(ManifestFileReader.isRunFromJar()).thenReturn(true);
        mockStatic(LocalUpdateSpecificationHelper.class);
        VersionData versionDataToReturn = new VersionData();
        versionDataToReturn.setVersion(new Version(1, 1, 2, true).toString());
        mockStatic(FileUtil.class);
        when(FileUtil.deserializeObjectFromJsonFile(any(File.class), eq(VersionData.class))).thenReturn(versionDataToReturn);
        mockStatic(OsDetector.class);
        when(OsDetector.getOs()).thenReturn(OsDetector.Os.UNKNOWN);

        updateManager.start();
        sleep(2000);

        assertEquals(7, events.size());
        assertTrue(events.get(0) instanceof ApplicationUpdateInProgressEvent);  // Clear specification file
        assertTrue(events.get(1) instanceof ApplicationUpdateInProgressEvent);  // Create & download specification file
        assertTrue(events.get(2) instanceof ApplicationUpdateInProgressEvent);  // Read version data from file
        assertTrue(events.get(3) instanceof ApplicationUpdateInProgressEvent);  // Read version from version data
        assertTrue(events.get(4) instanceof ApplicationUpdateInProgressEvent);  // Check version
        assertTrue(events.get(5) instanceof ApplicationUpdateInProgressEvent);  // Get files for OS
        assertTrue(events.get(6) instanceof ApplicationUpdateFailedEvent);      // Fail due to unknown OS
    }

    @Test
    public void startUpdate_failedToCreateUpdateDir_failUpdate() throws Exception {
        mockStatic(ManifestFileReader.class);
        when(ManifestFileReader.isRunFromJar()).thenReturn(true);
        mockStatic(LocalUpdateSpecificationHelper.class);
        VersionData versionDataToReturn = new VersionData();
        versionDataToReturn.setVersion(new Version(1, 1, 2, true).toString());
        List<LibraryDescriptor> libraries = new ArrayList<>();
        libraries.add(new LibraryDescriptor("test", "http://www.google.com", OsDetector.Os.MAC));
        versionDataToReturn.setLibraries(libraries);
        mockStatic(FileUtil.class);
        when(FileUtil.deserializeObjectFromJsonFile(any(File.class), eq(VersionData.class))).thenReturn(versionDataToReturn);
        mockStatic(OsDetector.class);
        when(OsDetector.getOs()).thenReturn(OsDetector.Os.MAC);
        doThrow(new IOException("Exception")).when(FileUtil.class, "createDirs", any(File.class));

        updateManager.start();
        sleep(2000);

        assertEquals(8, events.size());
        assertTrue(events.get(0) instanceof ApplicationUpdateInProgressEvent);  // Clear specification file
        assertTrue(events.get(1) instanceof ApplicationUpdateInProgressEvent);  // Create & download specification file
        assertTrue(events.get(2) instanceof ApplicationUpdateInProgressEvent);  // Read version data from file
        assertTrue(events.get(3) instanceof ApplicationUpdateInProgressEvent);  // Read version from version data
        assertTrue(events.get(4) instanceof ApplicationUpdateInProgressEvent);  // Check version
        assertTrue(events.get(5) instanceof ApplicationUpdateInProgressEvent);  // Get files for OS
        assertTrue(events.get(6) instanceof ApplicationUpdateInProgressEvent);  // Create update directory
        assertTrue(events.get(7) instanceof ApplicationUpdateFailedEvent);      // Fail to create update directory
    }

    @Test
    public void startUpdate_failedToCreateSpecification_failUpdate() throws Exception {
        mockStatic(ManifestFileReader.class);
        when(ManifestFileReader.isRunFromJar()).thenReturn(true);
        mockStatic(LocalUpdateSpecificationHelper.class);
        VersionData versionDataToReturn = new VersionData();
        versionDataToReturn.setVersion(new Version(1, 1, 2, true).toString());
        List<LibraryDescriptor> libraries = new ArrayList<>();
        libraries.add(new LibraryDescriptor("test", "http://www.google.com", OsDetector.Os.MAC));
        versionDataToReturn.setLibraries(libraries);
        mockStatic(FileUtil.class);
        when(FileUtil.deserializeObjectFromJsonFile(any(File.class), eq(VersionData.class))).thenReturn(versionDataToReturn);
        mockStatic(OsDetector.class);
        when(OsDetector.getOs()).thenReturn(OsDetector.Os.MAC);
        doThrow(new IOException("Exception")).when(LocalUpdateSpecificationHelper.class, "saveLocalUpdateSpecFile", any(List.class));

        updateManager.start();
        sleep(2000);

        assertEquals(10, events.size());
        assertTrue(events.get(0) instanceof ApplicationUpdateInProgressEvent);  // Clear specification file
        assertTrue(events.get(1) instanceof ApplicationUpdateInProgressEvent);  // Create & download specification file
        assertTrue(events.get(2) instanceof ApplicationUpdateInProgressEvent);  // Read version data from file
        assertTrue(events.get(3) instanceof ApplicationUpdateInProgressEvent);  // Read version from version data
        assertTrue(events.get(4) instanceof ApplicationUpdateInProgressEvent);  // Check version
        assertTrue(events.get(5) instanceof ApplicationUpdateInProgressEvent);  // Get files for OS
        assertTrue(events.get(6) instanceof ApplicationUpdateInProgressEvent);  // Create update directory
        assertTrue(events.get(7) instanceof ApplicationUpdateInProgressEvent);  // Download files
        assertEquals(1.0, ((ApplicationUpdateInProgressEvent) events.get(7)).getProgress(), 0.01);  // Downloaded files
        assertTrue(events.get(8) instanceof ApplicationUpdateInProgressEvent);  // Finalizing, create local spec
        assertTrue(events.get(9) instanceof ApplicationUpdateFailedEvent);      // Fail to create local spec
    }

    @After
    public void tearDown() {
        EventManager.clearSubscribers();
    }
}
