package address.model.datatypes;

/**
 * Same purpose as {@link ReadablePerson}, extended with additional status data viewable by user.
 */
public interface ReadableViewablePerson extends ReadablePerson {

    int getSecondsLeftInPendingState();
}
