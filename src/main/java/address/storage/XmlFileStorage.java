package address.storage;

import address.exceptions.DataConversionException;
import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.util.XmlUtil;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Stores addressbook data in an XML file
 */
public class XmlFileStorage {
    /**
     * Saves the given addressbook data to the specified file.
     */
    public static void saveDataToFile(File file, StorageAddressBook addressBook)
            throws DataConversionException, FileNotFoundException {
        XmlUtil.saveDataToFile(file, addressBook);
    }

    /**
     * Returns address book in the file or an empty address book
     */
    public static StorageAddressBook loadDataFromSaveFile(File file) throws DataConversionException, FileNotFoundException {
        return XmlUtil.getDataFromFile(file, StorageAddressBook.class);
    }

}
