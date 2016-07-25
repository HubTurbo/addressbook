package address.parser.qualifier;

import address.model.datatypes.person.ReadOnlyViewablePerson;

public interface Qualifier {
    boolean run(ReadOnlyViewablePerson person);
    String toString();
}
