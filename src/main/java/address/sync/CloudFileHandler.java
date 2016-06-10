package address.sync;

import address.exceptions.DataConversionException;
import address.sync.model.CloudAddressBook;
import address.util.XmlUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CloudFileHandler {
    private static final Logger logger = LogManager.getLogger(CloudFileHandler.class);

    private File getCloudDataFilePath(String addressBookName) {
        return new File("cloud/" + addressBookName);
    }

    public CloudAddressBook readCloudAddressBookFromFile(String addressBookName) throws FileNotFoundException, DataConversionException {
        File cloudFile = getCloudDataFilePath(addressBookName);
        try {
            logger.info("Reading from cloud file '{}'.", cloudFile.getName());
            CloudAddressBook cloudAddressBook = XmlUtil.getDataFromFile(cloudFile, CloudAddressBook.class);
            if (cloudAddressBook.getName() == null) throw new DataConversionException("AddressBook name is null.");
            return cloudAddressBook;
        } catch (FileNotFoundException e) {
            logger.warn("Cloud file '{}' not found.", cloudFile.getName());
            throw e;
        } catch (DataConversionException e) {
            logger.warn("Error reading from cloud file '{}'.", cloudFile.getName());
            e.printStackTrace();
            throw e;
        }
    }

    public void writeCloudAddressBookToFile(CloudAddressBook cloudAddressBook) throws FileNotFoundException, DataConversionException {
        String addressBookName = cloudAddressBook.getName();
        File cloudFile = getCloudDataFilePath(addressBookName);
        try {
            logger.info("Writing from cloud file '{}'.", cloudFile.getName());
            XmlUtil.saveDataToFile(cloudFile, cloudAddressBook);
        } catch (FileNotFoundException | DataConversionException e) {
            logger.warn("Error writing to cloud file '{}'.", cloudFile.getName());
            throw e;
        }
    }

    public void createCloudAddressBookFile(String addressBookName) throws IOException, DataConversionException {
        File cloudFile = getCloudDataFilePath(addressBookName);
        if (cloudFile.exists() || !cloudFile.createNewFile()) {
            logger.warn("Error creating addressbook '{}'.", addressBookName);
            throw new IllegalArgumentException("AddressBook '" + addressBookName + "' already exists!");
        }
        writeCloudAddressBookToFile(new CloudAddressBook(addressBookName));
    }
}
