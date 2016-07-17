package address.updater;

import address.testutil.TestUtil;
import hubturbo.updater.LocalUpdateSpecificationHelper;
import commons.FileUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

public class LocalUpdateSpecificationHelperTest {

    private static final File DUMMY_LOCAL_UPDATE_DATA_FILE = new File(
            TestUtil.appendToSandboxPath("dummyUpdateSpecification"));

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException, IOException {
        FileUtil.deleteFileIfExists(DUMMY_LOCAL_UPDATE_DATA_FILE);
        TestUtil.setFinalStatic(LocalUpdateSpecificationHelper.class.getDeclaredField("LOCAL_UPDATE_DATA_FILE"),
                DUMMY_LOCAL_UPDATE_DATA_FILE.getPath());
    }

    @Test
    public void getLocalUpdateSpecFile_readsCorrectValue() {
        assertEquals(DUMMY_LOCAL_UPDATE_DATA_FILE.getPath(),
                LocalUpdateSpecificationHelper.getLocalUpdateSpecFilepath());
    }

    @Test
    public void clearLocalUpdateSpecFile_fileExists_noExceptionThrown() throws IOException {
        FileUtil.createFile(DUMMY_LOCAL_UPDATE_DATA_FILE);
        assertTrue(FileUtil.isFileExists(DUMMY_LOCAL_UPDATE_DATA_FILE));

        LocalUpdateSpecificationHelper.clearLocalUpdateSpecFile();

        assertFalse(FileUtil.isFileExists(DUMMY_LOCAL_UPDATE_DATA_FILE));
    }

    @Test
    public void clearLocalUpdateSpecFile_fileInexistent_noExceptionThrown() throws IOException {
        assertFalse(FileUtil.isFileExists(DUMMY_LOCAL_UPDATE_DATA_FILE));

        LocalUpdateSpecificationHelper.clearLocalUpdateSpecFile();

        assertFalse(FileUtil.isFileExists(DUMMY_LOCAL_UPDATE_DATA_FILE));
    }

    @Test
    public void saveLocalUpdateSpecFile_noExceptionThrown() throws IOException {
        saveDummyLocalUpdateSpecFile();

        assertTrue(FileUtil.isFileExists(DUMMY_LOCAL_UPDATE_DATA_FILE));
    }

    @Test
    public void readLocalUpdateSpecFile() throws IOException {
        saveDummyLocalUpdateSpecFile();
        List<String> listFromFile =
                LocalUpdateSpecificationHelper.readLocalUpdateSpecFile(DUMMY_LOCAL_UPDATE_DATA_FILE.getPath());
        assertEquals(getDummyList(), listFromFile);
    }

    private void saveDummyLocalUpdateSpecFile() throws IOException {
        LocalUpdateSpecificationHelper.saveLocalUpdateSpecFile(getDummyList());
    }

    private List<String> getDummyList() {
        List<String> affectedFiles = new ArrayList<>();
        affectedFiles.add("file 1");
        affectedFiles.add("file 2");
        affectedFiles.add("file 3");

        return affectedFiles;
    }
}
