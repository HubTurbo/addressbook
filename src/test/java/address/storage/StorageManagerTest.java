package address.storage;


import address.testutil.SerializableTestClass;
import address.testutil.TestUtil;
import commons.FileUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class StorageManagerTest {
    private static final File SERIALIZATION_FILE = new File(TestUtil.appendToSandboxPath("serialize.json"));

    @Test
    public void recreateFile() {} // This is not implemented as it requires reflection
    @Test
    public void createAndWriteToConfigFile() {} // This is not implemented as it requires reflection
    @Test
    public void deleteConfigFileIfExists() {} // This is not implemented as it requires reflection
    @Test
    public void readFromConfigFile() {} // This is not implemented as it requires reflection

    /**
     * This is not implemented due to the need to mock static methods of StorageManager which will prevent some
     * real methods to be called, hence leaving other unrelated methods untested
     */
    @Test
    public void saveDataToFile() {}

    /**
     * This is not implemented due to the need to mock static methods of StorageManager which will prevent some
     * real methods to be called, hence leaving other unrelated methods untested
     */
    @Test
    public void savePrefsToFile_correspondingMethodCalled() {}

    @Test
    public void loadDataFromFile() {} // This is not implemented as it requires reflection


    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        FileUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = FileUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }
}
