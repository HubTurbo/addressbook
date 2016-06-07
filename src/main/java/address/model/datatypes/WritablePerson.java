package address.model.datatypes;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Defines all collection and value based setters for the Person domain object.
 */
public interface WritablePerson {

    /**
     * Updates this Person's attributes based on the argument Person's state.
     *
     * @param newDataSource snapshot of the new field values.
     * @return self
     */
    WritablePerson update(ReadablePerson newDataSource);

    void setFirstName(String firstName);
    void setLastName(String lastName);

    // TODO: consider a custom githubusername class that validates the string on creation
    /**
     * @param githubUserName must be validated as a parsable url in the form of
     *      https://www.github.com/ + githubUserName
     */
    void setGithubUserName(String githubUserName);

    void setStreet(String street);
    void setPostalCode(String postalCode);
    void setCity(String city);

    void setBirthday(LocalDate birthday);

    /**
     * Note: Will not defensively copy individual tag elements in the collection.
     * @param tags collection of tags.
     */
    void setTags(Collection<Tag> tags);
}
