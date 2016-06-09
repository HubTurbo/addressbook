package address.model.datatypes.person;

/**
 * Same purpose as {@link ReadablePerson}, extended with additional status data viewable by user.
 */
public interface ReadableViewablePerson extends ReadablePerson {

    int getSecondsLeftInPendingState();

    boolean isEdited();
    boolean isDeleted();
}
