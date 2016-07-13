package address.testutil;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;

/**
 * Typical data set used for testing
 */
public class TypicalTestData {

    /*
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
    //TODO: add more details to these persons.

    public Tag colleagues = new Tag("colleagues");
    public Tag friends = new Tag("friends");

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

}
