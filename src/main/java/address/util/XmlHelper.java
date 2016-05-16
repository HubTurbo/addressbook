package address.util;

import address.model.ContactGroup;
import address.model.Person;
import address.model.AddressBookWrapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;

/**
 * Helps with reading from and writing to the XML file.
 */
public class XmlHelper {

    public static AddressBookWrapper getDataFromFile(File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(AddressBookWrapper.class);
        Unmarshaller um = context.createUnmarshaller();

        // Reading XML from the file and unmarshalling.
        return ((AddressBookWrapper) um.unmarshal(file));
    }

    public static void saveToFile(File file, List<Person> personData, List<ContactGroup> groupData)
            throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(AddressBookWrapper.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Wrapping our person data.
        AddressBookWrapper wrapper = new AddressBookWrapper();
        wrapper.setPersons(personData);
        wrapper.setGroups(groupData);

        // Marshalling and saving XML to the file.
        m.marshal(wrapper, file);
    }
}
