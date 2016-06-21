package address.storage;

import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import address.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serialisable address book class for local storage
 */
@XmlRootElement(name = "addressbook")
public class StorageAddressBook implements ReadOnlyAddressBook {

    @XmlElement
    private List<StoragePerson> persons;
    @XmlElement
    private List<Tag> tags;

    {
        persons = new ArrayList<>();
        tags = new ArrayList<>();
    }

    public StorageAddressBook() {}

    public StorageAddressBook(ReadOnlyAddressBook src) {
        persons.addAll(src.getPersonList().stream().map(StoragePerson::new).collect(Collectors.toList()));
        tags = src.getTagList();
    }

    @Override
    public List<ReadOnlyPerson> getPersonList() {
        return Collections.unmodifiableList(persons);
    }

    @Override
    public List<Tag> getTagList() {
        return Collections.unmodifiableList(tags);
    }

    public static void main(String... args) throws Exception {
        JAXBContext context = JAXBContext.newInstance(StorageAddressBook.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        AddressBook data1 = new AddressBook();
        data1.addPerson(new Person("long", "potato", 1));
        Person p = new Person("tagged", "lel", 2);
        Tag t1 = new Tag("wow");
        Tag t2 = new Tag("omg");
        p.setTags(Arrays.asList(t1, t2));
        data1.addPerson(p);
        data1.addTag(t1);
        data1.addTag(t2);
        StorageAddressBook data = new StorageAddressBook(data1);

//        m.marshal(data, System.out);
        System.out.println(JsonUtil.toJsonString(data));
    }
}
