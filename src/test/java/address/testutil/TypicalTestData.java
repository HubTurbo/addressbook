package address.testutil;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Typical data set used for testing
 */
public class TypicalTestData {

    public Tag colleagues = new Tag("colleagues");
    public Tag friends = new Tag("friends");

    /*
     * TODO: add more details to these persons.
     * Note that staring letter of names have some pattern.
     * First person's name start with A and her second name starts with B.
     * The Second person's name starts with B and his other names start with C and D.
     * And so on.
     */
    public Person alice = new Person("Alice", "Brown", 1);
    public Person benson = new Person("Benson", "Christopher Dean", 2);
    public Person charlie = new Person("Charlie", "Davidson", 3);
    public Person dan = new Person("Dan", "Edwards", 4);
    public Person elizabeth = new Person("Elizabeth", "F. Green", 5);
    public Person fiona = new PersonBuilder("Fiona", "Wong", 6).withStreet("51th street").withCity("New York")
                                                               .withPostalCode("51245").withBirthday("01.01.1980")
                                                               .withGithubUsername("fiona").withTags(friends).build();
    public Person george = new PersonBuilder("George", "David", 7).withStreet("8th Ave").withCity("Chicago")
                                                                  .withPostalCode("25614").withBirthday("01.12.1988")
                                                                  .withGithubUsername("george").withTags(friends, colleagues)
                                                                  .build();

    public AddressBook book;

    public TypicalTestData() {
        book = new AddressBook();
        book.addPerson(alice);
        book.addPerson(benson);
        book.addPerson(charlie);
        book.addPerson(dan);
        book.addPerson(elizabeth);
        book.addTag(colleagues);
        book.addTag(friends);
    }

    public Person[] getTestData() {
        return new Person[] {alice, benson, charlie, dan, elizabeth};
    }

}
