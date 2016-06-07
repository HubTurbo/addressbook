package address.sync;

import address.exceptions.DataConversionException;
import address.sync.model.CloudAddressBook;
import address.util.XmlUtil;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CloudFileHandler {

    private File getCloudDataFilePath(String addressBookName) {
        return new File("/cloud/" + addressBookName);
    }

    public CloudAddressBook readCloudAddressBookFromFile(String addressBookName) throws FileNotFoundException, DataConversionException {
        File cloudFile = getCloudDataFilePath(addressBookName);
        System.out.println("Reading from cloudFile: " + cloudFile.canRead());
        try {
            return XmlUtil.getDataFromFile(cloudFile, CloudAddressBook.class);
        } catch (FileNotFoundException | DataConversionException e) {
            System.out.println("Error reading from cloud file.");
            throw e;
        }
    }

    public void writeCloudAddressBookToFile(CloudAddressBook cloudAddressBook) throws FileNotFoundException, DataConversionException {
        String addressBookName = cloudAddressBook.getName();
        File cloudFile = getCloudDataFilePath(addressBookName);
        System.out.println("Writing to cloudFile: " + cloudFile.canRead());
        try {
            XmlUtil.saveDataToFile(cloudFile, cloudAddressBook);
        } catch (FileNotFoundException | DataConversionException e) {
            System.out.println("Error writing to cloud file.");
            throw e;
        }
    }

    public void createCloudAddressBookFile(String addressBookName) throws IOException, DataConversionException {
        File cloudFile = getCloudDataFilePath(addressBookName);
        if (cloudFile.exists() || !cloudFile.createNewFile()) {
            throw new IllegalArgumentException("AddressBook '" + addressBookName + "' already exists!");
        }
        writeCloudAddressBookToFile(new CloudAddressBook(addressBookName));
    }
}
