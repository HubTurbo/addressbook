package address.model.datatypes;

import java.time.LocalDate;
import java.util.List;

/**
 * Defines all collection and value based accessors for the Person domain object.
 */
public interface ReadablePerson {

    String getFirstName();
    String getLastName();
    /**
     *
     * @return
     */
    String fullName();

    String getGithubUserName();
    /**
     *
     * @return
     */
    String profilePageUrl();

    String getStreet();
    String getPostalCode();
    String getCity();

    LocalDate getBirthday();
    /**
     *
     * @return
     */
    String birthdayString();

    /**
     * @see ObservablePerson#getTags()
     * @return a List view of this Person's tags
     */
    List<Tag> getTags();
    /**
     *
     * @return
     */
    String tagsString();
}
