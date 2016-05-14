package address.exceptions;

/**
 * Signifies an attempt to add a duplicate Person
 */
public class DuplicatePersonException extends Exception {

    public final String identifier;

    public DuplicatePersonException(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return "Cannot add Person: " + identifier + " already exists!";
    }
}
