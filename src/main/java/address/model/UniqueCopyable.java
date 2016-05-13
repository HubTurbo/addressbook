package address.model;

/**
 * Base class for all data types that will require a uniqueness check and easy copying.
 * Forces implementation of methods required for a proper uniqueness check
 *
 * @param <Self> Self should be the implementing subclass. This is a hacky substitute for self-referencing
 *              generic types which java lacks.
 *
 *              Eg. class MyClass extend UniqueCopyable<MyClass> {}
 *              This allows for compile time static type checking on clone()
 *              and
 *
 * IMPLEMENTING CONCRETE CLASSES MUST BE DECLARED FINAL.
 */
public interface UniqueCopyable<Self> {

    /**
     * This method signature allows compile time static type checking 
     * and inference for the return value of clone().
     * @return cloned deep copy
     */
    Self clone();

    Self update(Self newData);

    @Override
    boolean equals(Object other);

    @Override
    int hashCode();
}
