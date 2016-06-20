package address.sync.cloud;

import address.exceptions.DataConversionException;
import address.sync.cloud.model.CloudAddressBook;
import address.util.AppLogger;
import address.util.LoggerManager;
import address.util.XmlUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CloudFileHandler {
    private static final AppLogger logger = LoggerManager.getLogger(CloudFileHandler.class);

    private File getCloudDataFilePath(String addressBookName) {
        return new File("cloud/" + addressBookName);
    }

    public CloudAddressBook readCloudAddressBookFromFile(String addressBookName) throws FileNotFoundException, DataConversionException {
        File cloudFile = getCloudDataFilePath(addressBookName);
        try {
            logger.debug("Reading from cloud file '{}'.", cloudFile.getName());
            CloudAddressBook CloudAddressBook = XmlUtil.getDataFromFile(cloudFile, CloudAddressBook.class);
            if (CloudAddressBook.getName() == null) throw new DataConversionException("AddressBook name is null.");
            return CloudAddressBook;
        } catch (FileNotFoundException e) {
            logger.warn("Cloud file '{}' not found.", cloudFile.getName());
            throw e;
        } catch (DataConversionException e) {
            logger.warn("Error reading from cloud file '{}'.", cloudFile.getName());
            throw e;
        }
    }

    public void writeCloudAddressBookToFile(CloudAddressBook CloudAddressBook) throws FileNotFoundException, DataConversionException {
        String addressBookName = CloudAddressBook.getName();
        File cloudFile = getCloudDataFilePath(addressBookName);
        try {
            logger.info("Writing to cloud file '{}'.", cloudFile.getName());
            XmlUtil.saveDataToFile(cloudFile, CloudAddressBook);
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
