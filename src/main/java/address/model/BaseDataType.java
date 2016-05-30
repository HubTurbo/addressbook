package address.model;

/**
 * All domain data-type classes should extend this.
 */
public abstract class BaseDataType extends UniqueData implements PropertyLister, Cloneable {
    /**
     * Subclasses should override clone's return value to return their own types
     * @return deep copy of self
     */
    public abstract BaseDataType clone();
}
