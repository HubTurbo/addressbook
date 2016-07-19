package address.testutil;

import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import commons.DateTimeUtil;

import java.util.Arrays;

/**
 * A utility class to build Person objects using a fluent interface.
 */
public class PersonBuilder {
    private final Person person;

    /**
     * Creates a person builder with the given Person object.
     * The person object will be mutated during the building process.
     * @param initial
     */
    public PersonBuilder(Person initial) {
        this.person = initial;
    }

    public PersonBuilder withFirstName(String firstName) {
        person.setFirstName(firstName);
        return this;
    }

    public PersonBuilder withLastName(String lastName) {
        person.setLastName(lastName);
        return this;
    }

    public PersonBuilder withStreet(String street) {
        person.setStreet(street);
        return this;
    }

    public PersonBuilder withCity(String city) {
        person.setCity(city);
        return this;
    }

    public PersonBuilder withPostalCode(String postalCode){
        person.setPostalCode(postalCode);
        return this;
    }

    public PersonBuilder withBirthday(String birthday){
        person.setBirthday(DateTimeUtil.parse(birthday));
        return this;
    }

    public PersonBuilder withGithubUsername(String githubUsername){
        person.setGithubUsername(githubUsername);
        return this;
    }

    public PersonBuilder withTags(Tag... tags){
        person.setTags(Arrays.asList(tags));
        return this;
    }

    public Person build() {
        return person;
    }
}
