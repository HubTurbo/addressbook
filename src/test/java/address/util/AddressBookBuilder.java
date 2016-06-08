package address.util;

import address.model.AddressBook;
import address.model.datatypes.Person;
import address.model.datatypes.Tag;

/**
 * A utility class to help with building Addressbook objects.
 * Example usage: <br>
 *     {@code AddressBook ab = new AddressBookBuilder().withPerson("John", "Doe").withTag("Friend").build();}
 */
public class AddressBookBuilder {

    private AddressBook addressBook;

    public AddressBookBuilder(){
        addressBook = new AddressBook();
    }

    public AddressBookBuilder(AddressBook addressBook){
        this.addressBook = addressBook;
    }

    public AddressBookBuilder withPerson(String firstName, String lastName){
        addressBook.addPerson(new Person(firstName, lastName));
        return this;
    }

    public AddressBookBuilder withTag(String tagName){
        addressBook.addTag(new Tag(tagName));
        return this;
    }

    public AddressBook build(){
        return addressBook;
    }
}
