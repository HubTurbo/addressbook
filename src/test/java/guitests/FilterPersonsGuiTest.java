package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import commons.StringUtil;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class FilterPersonsGuiTest extends GuiTestBase {
    private Person john, mary, annabelle, barney, charlie, danny;
    private Tag colleagues, friends, family;

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

        barney.setTags(Arrays.asList(colleagues, family));
        barney.setCity("California");
        barney.setBirthday(LocalDate.of(1990, 3, 25));

        charlie.setTags(Collections.singletonList(friends));
        charlie.setCity("Budapest");

        danny.setBirthday(LocalDate.of(1990, 3, 22));
        danny.setGithubUsername("dan1990");

        return new AddressBook(Arrays.asList(john, mary, annabelle, barney, charlie, danny),
                               Arrays.asList(colleagues, family, friends));

    }

    @Test
    public void filterPersons_singleSimpleQualifier() {
        personListPanel.enterFilterAndApply("tag:colleagues");
        assertTrue(personListPanel.isExactList(john, barney));
    }

    @Test
    public void filterPersons_multipleSimpleQualifiers() {
        personListPanel.enterFilterAndApply("tag:friends city:california");
        assertTrue(personListPanel.isExactList(
                filterByCity(filterByTag(getInitialData().getPersons(), "friends", false), "california", false)));

        personListPanel.enterFilterAndApply("tag:friends name:rogers");
        assertTrue(personListPanel.isExactList(
                filterByTag(filterByName(getInitialData().getPersons(), "rogers", false), "friends", false)));

        personListPanel.enterFilterAndApply("name:rogers name:danny id:6");
        assertTrue(personListPanel.isExactList(
                filterById(filterByName(filterByName(getInitialData().getPersons(), "rogers", false), "danny", false), 6, false)));

        personListPanel.enterFilterAndApply("name:rogers name:barney id:6");
        assertTrue(personListPanel.isExactList(
                filterById(filterByName(filterByName(getInitialData().getPersons(), "rogers", false), "barney", false), 6, false)));
    }

    @Test
    public void filterPersons_manyMatches() {
        personListPanel.enterFilterAndApply("tag:friends");
        assertTrue(personListPanel.isExactList(john, mary, annabelle, charlie));
    }

    @Test
    public void filterPersons_noMatches() {
        personListPanel.enterFilterAndApply("tag:enemies");
        assertTrue(personListPanel.isExactList());
    }

    @Test
    public void filterPersons_negatedQualifiers() {
        personListPanel.enterFilterAndApply("!tag:enemies");
        assertTrue(personListPanel.isExactList(john, mary, annabelle, barney, charlie, danny));

        personListPanel.enterFilterAndApply("!!tag:friends");
        assertTrue(personListPanel.isExactList(john, mary, annabelle, charlie));

        personListPanel.enterFilterAndApply("!!!city:Hawaii");
        // new matching entries are appended to reduce processing time
        assertTrue(personListPanel.isExactList(john, mary, charlie, barney, danny));
    }

    @Test
    public void filterPersons_multipleQualifiers() {
        personListPanel.enterFilterAndApply("tag:friends !city:california");
        assertTrue(personListPanel.isExactList(john, annabelle, charlie));

        personListPanel.enterFilterAndApply("!tag:friends city:california");
        assertTrue(personListPanel.isExactList(barney));

        personListPanel.enterFilterAndApply("id:2 !!!id:3 !id:5 !!!!!id:8");
        assertTrue(personListPanel.isExactList(mary));

        personListPanel.enterFilterAndApply("id:2 id:3 id:5 !id:4");
        assertTrue(personListPanel.isExactList());
    }

    private List<Person> filterByTag(List<Person> originalList, String partialTagName, boolean isNegative) {
        return originalList.stream()
                .filter(person -> isNegative != hasTag(person, partialTagName))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Person> filterByName(List<Person> originalList, String partialName, boolean isNegative) {
        return originalList.stream()
                .filter(person -> isNegative != hasName(person, partialName))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Person> filterById(List<Person> originalList, int id, boolean isNegative) {
        return originalList.stream()
                .filter(person -> isNegative != (person.getId() == id))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Person> filterByCity(List<Person> originalList, String partialCityName, boolean isNegative) {
        return originalList.stream()
                .filter(person -> isNegative != StringUtil.containsIgnoreCase(person.getCity(), partialCityName))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean hasName(Person person, String partialName) {
        return StringUtil.containsIgnoreCase(person.getFirstName(), partialName) ||
                StringUtil.containsIgnoreCase(person.getLastName(), partialName);
    }

    private boolean hasTag(Person person, String partialTagName) {
        for (Tag tag : person.getTags()) {
            if (StringUtil.containsIgnoreCase(tag.getName(), partialTagName)) return true;
        }
        return false;
    }
}
