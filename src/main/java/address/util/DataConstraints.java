package address.util;

import address.model.DataType;

import java.util.Collection;
import java.util.HashSet;

/**
 * Utility methods for enforcing data constraints
 */
public class DataConstraints {

    public static <D extends DataType> boolean itemsAreUnique(Collection<D> items) {
        return (new HashSet<D>(items)).size() == items.size();
    }
}
