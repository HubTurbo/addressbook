package address.unittests;

import address.model.AddressBook;
import address.model.ContactGroup;
import address.model.Person;
import address.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

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
    /**
     * Due to updatedAt field, we can check JSON string correct value. As such, we just confirm there is no exception
     */
    public void jsonUtil_getJsonStringObjectRepresentation_correctJsonObject() throws JsonProcessingException {
        ContactGroup sampleContactGroup = new ContactGroup("Group");
        Person samplePerson = new Person("First", "Last");
        samplePerson.setCity("Singapore");
        samplePerson.setPostalCode("123456");
        List<ContactGroup> group = new ArrayList<>();
        group.add(sampleContactGroup);
        samplePerson.setContactGroups(group);
        samplePerson.setBirthday(LocalDate.of(1980, 3, 18));
        samplePerson.setGithubUserName("FirstLast");

        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(Arrays.asList(samplePerson));
        addressBook.setGroups(Arrays.asList(sampleContactGroup));

        JsonUtil.toJsonString(addressBook);
    }

    @Test
    public void jsonUtil_readJsonStringToObjectInstance_correctObject() throws IOException {
        String jsonString = "{\n" +
                "  \"persons\" : [ {\n" +
                "    \"firstName\" : \"First\",\n" +
                "    \"lastName\" : \"Last\",\n" +
                "    \"street\" : \"\",\n" +
                "    \"postalCode\" : \"123456\",\n" +
                "    \"city\" : \"Singapore\",\n" +
                "    \"githubUserName\" : \"FirstLast\",\n" +
                "    \"contactGroups\" : [ {\n" +
                "      \"name\" : \"Group\",\n" +
                "      \"updatedAt\" : \"2016-05-26T16:40:09.157\"\n" +
                "    } ],\n" +
                "    \"birthday\" : \"1980-03-18\",\n" +
                "    \"updatedAt\" : \"2016-05-26T16:40:09.244\"\n" +
                "  } ],\n" +
                "  \"groups\" : [ {\n" +
                "    \"name\" : \"Group\",\n" +
                "    \"updatedAt\" : \"2016-05-26T16:40:09.157\"\n" +
                "  } ]\n" +
                "}";
        AddressBook addressBook = JsonUtil.fromJsonString(jsonString, AddressBook.class);
        assertEquals(1, addressBook.getPersons().size());
        assertEquals(1, addressBook.getGroups().size());

        Person person = addressBook.getPersons().get(0);
        ContactGroup group = addressBook.getGroups().get(0);

        assertEquals("Group", group.getName());

        assertEquals("First", person.getFirstName());
        assertEquals("Last", person.getLastName());
        assertEquals("Singapore", person.getCity());
        assertEquals("123456", person.getPostalCode());
        assertEquals(group, person.getContactGroups().get(0));
        assertEquals(LocalDate.of(1980, 3, 18), person.getBirthday());
        assertEquals("FirstLast", person.getGithubUserName());
    }
}
