package address.model.datatypes;

import address.model.ModelManager;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Defines all collection and value based accessors for the Person domain object.
 */
public interface ReadablePerson {

    /**
     * @param subtypeList source list of element type: subclasses of ReadablePersons
     * @param collectionBuilder desired collection implementation of returned collection
     * @see ModelManager#upcastToBoundCollection(ObservableList, Supplier)
     * @return an upcasted read-only collection with element type {@code ReadablePerson}
     */
    static <R extends Collection<ReadablePerson>> R readOnlyCollectionCast(
            ObservableList<? extends ReadablePerson> subtypeList, Supplier<R> collectionBuilder) {
        return ModelManager.upcastToBoundCollection(subtypeList, collectionBuilder);
    }


    String getFirstName();
    String getLastName();
    /**
     * @return first-last format full name
     */
    String fullName();

    String getGithubUserName();
    /**
     * @return github profile url
     */
    String githubProfilePageUrl();
    String githubProfilePicUrl();

    String getStreet();
    String getPostalCode();
    String getCity();

    LocalDate getBirthday();
    /**
     * @return birthday date-formatted as string
     */
    String birthdayString();

    /**
     * @see ObservablePerson#getTags()
     * @return a List view of this Person's tags
     */
    List<Tag> getTags();
    /**
     * @return string representation of this Person's tags
     */
    String tagsString();

}
