package address.storage;

import address.model.datatypes.AddressBook;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class XmlFileStorageTest {

    private static final File DUMMY_FILE = new File("dummy");
    private static final StorageAddressBook EMPTY_ADDRESSBOOK = new StorageAddressBook(new AddressBook());

    @Test
    public void getDataFromFile()throws Exception{
        assertEquals(EMPTY_ADDRESSBOOK.toString(),
                XmlFileStorage.loadDataFromSaveFile(DUMMY_FILE).toString());
        //TODO: use equality instead of string comparison
    }
}
