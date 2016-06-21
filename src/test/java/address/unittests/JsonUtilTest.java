package address.unittests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.person.Person;
import address.util.JsonUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    @Test
    public void jsonUtil_readJsonStringToObjectInstance_correctObject() throws IOException {
        String jsonString = "{\n" +
                "  \"persons\" : [ {\n" +
                "    \"id\" : 1,\n" +
                "    \"firstName\" : \"First\",\n" +
                "    \"lastName\" : \"Last\",\n" +
                "    \"street\" : \"\",\n" +
                "    \"postalCode\" : \"123456\",\n" +
                "    \"city\" : \"Singapore\",\n" +
                "    \"githubUsername\" : \"FirstLast\",\n" +
                "    \"birthday\" : \"1980-03-18\",\n" +
                "    \"tags\" : [ {\n" +
                "      \"name\" : \"Tag\"\n" +
                "    } ],\n" +
                "    \"birthday\" : \"1980-03-18\"\n" +
                "  } ],\n" +
                "  \"tags\" : [ {\n" +
                "    \"name\" : \"Tag\"\n" +
                "  } ]\n" +
                "}";
        AddressBook addressBook = JsonUtil.fromJsonString(jsonString, AddressBook.class);
        assertEquals(1, addressBook.getPersons().size());
        assertEquals(1, addressBook.getTags().size());

        Person person = addressBook.getPersons().get(0);
        Tag tag = addressBook.getTags().get(0);

        assertEquals(1, person.getID());
        assertEquals("First", person.getFirstName());
        assertEquals("Last", person.getLastName());
        assertEquals("Singapore", person.getCity());
        assertEquals("123456", person.getPostalCode());
        assertEquals(tag, person.getTagList().get(0));
        assertEquals("Tag", person.getTagList().get(0).getName());
        assertEquals(LocalDate.of(1980, 3, 18), person.getBirthday());
        assertEquals("FirstLast", person.getGithubUserName());
    }

    @Test
    public void jsonUtil_writeThenReadObjectToJson_correctObject() throws IOException {
        // Write
        Tag sampleTag = new Tag("Tag");
        Person samplePerson = new Person("First", "Last", 1);
        samplePerson.setCity("Singapore");
        samplePerson.setPostalCode("123456");
        List<Tag> tag = new ArrayList<>();
        tag.add(sampleTag);
        samplePerson.setTags(tag);
        samplePerson.setBirthday(LocalDate.of(1980, 3, 18));
        samplePerson.setGithubUserName("FirstLast");

        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(Arrays.asList(samplePerson));
        addressBook.setTags(Arrays.asList(sampleTag));

        String jsonString = JsonUtil.toJsonString(addressBook);
        System.out.println(jsonString);

        // Read
        AddressBook addressBookRead = JsonUtil.fromJsonString(jsonString, AddressBook.class);
        assertEquals(1, addressBookRead.getPersons().size());
        assertEquals(1, addressBookRead.getTags().size());

        Person person = addressBookRead.getPersons().get(0);
        Tag tagRead = addressBookRead.getTags().get(0);

        assertEquals(1, person.getID());
        assertEquals("First", person.getFirstName());
        assertEquals("Last", person.getLastName());
        assertEquals("Singapore", person.getCity());
        assertEquals("123456", person.getPostalCode());
        assertEquals(tagRead, person.getTagList().get(0));
        assertEquals("Tag", person.getTagList().get(0).getName());
        assertEquals(LocalDate.of(1980, 3, 18), person.getBirthday());
        assertEquals("FirstLast", person.getGithubUserName());
    }
}
