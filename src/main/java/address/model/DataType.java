package address.model;

/**
 * Indicates data container classes
 */
public abstract class DataType {
    // force implementation of custom equals and hashcode
    @Override
    public abstract boolean equals(Object other);
    @Override
    public abstract int hashCode();

    // force custom toString
    @Override
    public abstract String toString();
}
