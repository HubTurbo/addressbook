package address.util;

import address.model.*;
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
public class XmlHelper {

    public static AddressBook getDataFromFile(File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(AddressBook.class);
        Unmarshaller um = context.createUnmarshaller();

        // Reading XML from the file and unmarshalling.
        return ((AddressBook) um.unmarshal(file));
    }

    public static void saveModelToFile(File file, List<ModelPerson> personData, List<ModelContactGroup> groupData)
            throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ModelAddressBook.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Wrapping our person data.
        ModelAddressBook wrapper = new ModelAddressBook(false);
        wrapper.setModelPersons(personData);
        wrapper.setModelGroups(groupData);

        // Marshalling and saving XML to the file.
        m.marshal(wrapper, file);
    }

    public static void saveDataToFile(File file, List<Person> personData, List<ContactGroup> groupData)
            throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(AddressBook.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Wrapping our person data.
        AddressBook wrapper = new AddressBook();
        wrapper.setPersons(personData);
        wrapper.setGroups(groupData);

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
