package address.storage;

import address.model.datatypes.AddressBook;
import commons.XmlUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(XmlUtil.class)

public class XmlFileStorageTest {

    private static final File DUMMY_FILE = new File("dummy");
    private static final StorageAddressBook EMPTY_ADDRESSBOOK = new StorageAddressBook(new AddressBook());

    @Before
    public void setUp() throws Exception{
        PowerMockito.mockStatic(XmlUtil.class);
        PowerMockito.when(XmlUtil.getDataFromFile(DUMMY_FILE, StorageAddressBook.class)).thenReturn(EMPTY_ADDRESSBOOK);
    }


    @Test
    public void getDataFromFile()throws Exception{
        assertEquals(EMPTY_ADDRESSBOOK.toString(),
                XmlFileStorage.loadDataFromSaveFile(DUMMY_FILE).toString());
        //TODO: use equality instead of string comparison
    }

    @Test
    public void saveDataToFile()throws Exception{
        XmlFileStorage.saveDataToFile(DUMMY_FILE, EMPTY_ADDRESSBOOK);

        //Verify dependent method was called with the right parameters
        PowerMockito.verifyStatic();
        XmlUtil.saveDataToFile(DUMMY_FILE, EMPTY_ADDRESSBOOK);

    }
}
