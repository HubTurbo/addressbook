package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import address.sync.cloud.model.CloudAddressBook;
import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class FilterPersonsGuiTest extends GuiTestBase {
    Person john, mary, annabelle, barney, charlie, danny;
    Tag colleagues, friends, family;
    @Override
    public AddressBook getInitialData() {
        john = new Person("John", "Tan", 1);
        mary = new Person("Mary", "Jones", 2);
        annabelle = new Person("Annabelle", "Muellers", 3);
        barney = new Person("Barney", "Rogers", 4);
        charlie = new Person("Charlie", "Chip", 5);
        danny = new Person("Danny", "Rogers", 6);
        colleagues = new Tag("colleagues");
        friends = new Tag("friends");
        family = new Tag("family");

        john.setTags(Arrays.asList(colleagues, friends));
        john.setCity("Singapore");
        john.setGithubUsername("john123");
        john.setBirthday(LocalDate.of(1995, 10, 10));
        john.setStreet("Beach Road");

        mary.setTags(Collections.singletonList(friends));
        mary.setCity("California");
        mary.setBirthday(LocalDate.of(1960, 9, 20));

        annabelle.setTags(Collections.singletonList(friends));
        annabelle.setCity("Hawaii");
        annabelle.setBirthday(LocalDate.of(1998, 1, 30));

        return new AddressBook(Arrays.asList(john, mary, annabelle, barney, charlie, danny),
                               Arrays.asList(colleagues, family, friends));

    }

    @Override
    public CloudAddressBook getInitialCloudData() {
        CloudPerson john = new CloudPerson("John", "Tan", 1);
        CloudPerson mary = new CloudPerson("Mary", "Jones", 2);
        CloudPerson annabelle = new CloudPerson("Annabelle", "Muellers", 3);
        CloudPerson barney = new CloudPerson("Barney", "Rogers", 4);
        CloudPerson charlie = new CloudPerson("Charlie", "Chip", 5);
        CloudPerson danny = new CloudPerson("Danny", "Rogers", 6);
        CloudTag colleagues = new CloudTag("colleagues");
        CloudTag friends = new CloudTag("friends");
        CloudTag family = new CloudTag("family");

        john.setTags(Arrays.asList(colleagues, friends));
        john.setCity("Singapore");
        john.setGithubUsername("john123");
        john.setBirthday(LocalDate.of(1995, 10, 10));
        john.setStreet("Beach Road");

        mary.setTags(Collections.singletonList(friends));
        mary.setCity("California");
        mary.setBirthday(LocalDate.of(1960, 9, 20));

        annabelle.setTags(Collections.singletonList(friends));
        annabelle.setCity("Hawaii");
        annabelle.setBirthday(LocalDate.of(1998, 1, 30));

        return new CloudAddressBook("Test", Arrays.asList(john, mary, annabelle, barney, charlie, danny),
                Arrays.asList(colleagues, family, friends));
    }

    @Test
    public void filterPersons_singleQualifier() throws InterruptedException {
        personListPanel.enterFilterAndApply("tag:colleagues");
        assertTrue(personListPanel.isExactList(john));
    }
}
