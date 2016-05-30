package address.util;

import address.model.*;
import address.model.datatypes.Person;
import address.model.datatypes.Tag;
import address.sync.model.CloudAddressBook;
import address.sync.model.CloudTag;
import address.sync.model.CloudPerson;
import address.updater.model.UpdateData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;

/**
 * Helps with reading from and writing to XML files.
 */
public class XmlFileHelper {

    public static CloudAddressBook getCloudDataFromFile(File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(CloudAddressBook.class);
        Unmarshaller um = context.createUnmarshaller();

        // Reading XML from the file and unmarshalling.
        return ((CloudAddressBook) um.unmarshal(file));
    }

    public static void saveCloudDataToFile(File file, List<CloudPerson> personData, List<CloudTag> tagData) throws JAXBException {
        // Wrapping our person data.
        CloudAddressBook cloudAddressBook = new CloudAddressBook(personData, tagData);

        JAXBContext context = JAXBContext.newInstance(CloudAddressBook.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Marshalling and saving XML to the file.
        m.marshal(cloudAddressBook, file);
    }

    public static AddressBook getDataFromFile(File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(AddressBook.class);
        Unmarshaller um = context.createUnmarshaller();

        // Reading XML from the file and unmarshalling.
        return ((AddressBook) um.unmarshal(file));
    }

    public static void saveModelToFile(File file, List<Person> personData, List<Tag> tagData)
            throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(AddressBook.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Wrapping our person data.
        AddressBook wrapper = new AddressBook();
        wrapper.setPersons(personData);
        wrapper.setTags(tagData);

        // Marshalling and saving XML to the file.
        m.marshal(wrapper, file);
    }

    public static void saveDataToFile(File file, List<Person> personData, List<Tag> tagData)
            throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(AddressBook.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Wrapping our person data.
        AddressBook wrapper = new AddressBook();
        wrapper.setPersons(personData);
        wrapper.setTags(tagData);

        // Marshalling and saving XML to the file.
        m.marshal(wrapper, file);
    }

    public static UpdateData getUpdateDataFromFile(File file) throws JAXBException {
        JAXBContext context = JAXBContext
                .newInstance(UpdateData.class);
        Unmarshaller um = context.createUnmarshaller();

        // Reading XML from the file and unmarshalling.
        return ((UpdateData) um.unmarshal(file));
    }

    public static void saveUpdateDataToFile(File file, UpdateData updateData)
            throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(UpdateData.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Marshalling and saving XML to the file.
        m.marshal(updateData, file);
    }
}
