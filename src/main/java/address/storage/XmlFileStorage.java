package address.storage;

import address.model.AddressBook;
import address.util.XmlUtil;

import javax.xml.bind.JAXBException;
import java.io.File;

/**
 * Stores addressbook data in an XML file
 */
public class XmlFileStorage {
    /**
     * Saves the given addressbook data to the specified file.
     */
    public static void saveDataToFile(File file, AddressBook addressBook) throws JAXBException {
        XmlUtil.saveDataToFile(file, addressBook);
    }

    /**
     * Returns address book in the file or an empty address book
     */
    public static AddressBook loadDataFromSaveFile(File file) throws JAXBException {
        return XmlUtil.getDataFromFile(file, AddressBook.class);
    }

}
