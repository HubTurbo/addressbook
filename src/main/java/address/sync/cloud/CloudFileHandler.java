package address.sync.cloud;

import address.exceptions.DataConversionException;
import address.sync.cloud.model.CloudAddressBook;
import address.util.AppLogger;
import address.util.LoggerManager;
import commons.XmlUtil;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CloudFileHandler {
    private static final AppLogger logger = LoggerManager.getLogger(CloudFileHandler.class);
    private static final String CLOUD_DIRECTORY = "cloud/";

    public CloudAddressBook readCloudAddressBookFromExternalFile(String cloudDataFilePath) throws FileNotFoundException,
            DataConversionException {
        File cloudFile = new File(cloudDataFilePath);
        return readFromCloudFile(cloudFile);
    }

    public CloudAddressBook readCloudAddressBook(String addressBookName) throws FileNotFoundException,
            DataConversionException {
        File cloudFile = getCloudDataFile(addressBookName);
        return readFromCloudFile(cloudFile);
    }

    public void writeCloudAddressBook(CloudAddressBook cloudAddressBook) throws FileNotFoundException,
            DataConversionException {
        String addressBookName = cloudAddressBook.getName();
        File cloudFile = getCloudDataFile(addressBookName);
        try {
            logger.info("Writing to cloud file '{}'.", cloudFile.getName());
            XmlUtil.saveDataToFile(cloudFile, cloudAddressBook);
        } catch (FileNotFoundException e) {
            logger.warn("Error writing to cloud file '{}'.", cloudFile.getName());
            throw e;
        } catch (JAXBException e) {
            logger.warn("Error writing to cloud file '{}'.", cloudFile.getName());
            throw new DataConversionException(e);
        }
    }

    /**
     * Attempts to create a file with an empty address book
     * Deletes any existing file on the same path
     *
     * @param addressBookName
     * @throws IOException
     * @throws DataConversionException
     */
    public void initializeAddressBook(String addressBookName) throws IOException, DataConversionException {
        File cloudFile = getCloudDataFile(addressBookName);
        if (cloudFile.exists()) {
            cloudFile.delete();
        }

        createCloudFile(new CloudAddressBook(addressBookName));
    }

    /**
     * Attempts to create an empty address book on the cloud
     * Fails if address book already exists
     *
     * @param addressBookName
     * @throws IOException
     * @throws DataConversionException
     * @throws IllegalArgumentException if cloud file already exists
     */
    public void createAddressBook(String addressBookName) throws IOException, DataConversionException,
            IllegalArgumentException {
        createCloudFile(new CloudAddressBook(addressBookName));
    }

    /**
     * Attempts to create an empty address book on the cloud if it does not exist
     *
     * @param addressBookName
     * @throws IOException
     * @throws DataConversionException
     * @throws IllegalArgumentException
     */
    public void createAddressBookIfAbsent(String addressBookName) throws IOException, DataConversionException,
            IllegalArgumentException {
        File cloudFile = getCloudDataFile(addressBookName);
        if (cloudFile.exists()) return;
        try {
            createCloudFile(new CloudAddressBook(addressBookName));
        } catch (IllegalArgumentException e) {
            assert false : "Error in logic: createCloudFile should not be called since address book is present";
        }
    }

    private CloudAddressBook readFromCloudFile(File cloudFile) throws FileNotFoundException, DataConversionException {
        try {
            logger.debug("Reading from cloud file '{}'.", cloudFile.getName());
            CloudAddressBook cloudAddressBook = XmlUtil.getDataFromFile(cloudFile, CloudAddressBook.class);
            if (cloudAddressBook.getName() == null) throw new DataConversionException("AddressBook name is null.");
            return cloudAddressBook;
        } catch (FileNotFoundException e) {
            logger.warn("Cloud file '{}' not found.", cloudFile.getName());
            throw e;
        } catch (JAXBException e) {
            logger.warn("Error reading from cloud file '{}'.", cloudFile.getName());
            throw new DataConversionException(e);
        }
    }

    /**
     * Attempts to create the cloud file in the cloud directory, containing an empty address book
     * File will be named the same as the address book
     *
     * The cloud directory will also be created if it does not exist
     *
     * @param cloudAddressBook
     * @throws IOException
     * @throws DataConversionException
     * @throws IllegalArgumentException if cloud file already exists
     */
    private void createCloudFile(CloudAddressBook cloudAddressBook) throws IOException, DataConversionException, IllegalArgumentException {
        File cloudFile = getCloudDataFile(cloudAddressBook.getName());
        if (cloudFile.exists()) {
            logger.warn("Cannot create an address book that already exists: '{}'.", cloudAddressBook.getName());
            throw new IllegalArgumentException("AddressBook '" + cloudAddressBook.getName() + "' already exists!");
        }


        File cloudDirectory = new File(CLOUD_DIRECTORY);
        if (!cloudDirectory.exists() && !cloudDirectory.mkdir()) {
            logger.warn("Error creating directory: '{}'", CLOUD_DIRECTORY);
            throw new IOException("Error creating directory: " + CLOUD_DIRECTORY);
        }


        if (!cloudFile.createNewFile()) {
            logger.warn("Error creating cloud file: '{}'", getCloudDataFilePath(cloudAddressBook.getName()));
            throw new IOException("Error creating cloud file for address book: " + getCloudDataFilePath(cloudAddressBook.getName()));
        }

        writeCloudAddressBook(cloudAddressBook);
    }

    private File getCloudDataFile(String addressBookName) {
        return new File(getCloudDataFilePath(addressBookName));
    }

    private String getCloudDataFilePath(String addressBookName) {
        return CLOUD_DIRECTORY + addressBookName;
    }
}
