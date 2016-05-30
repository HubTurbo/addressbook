package address.parser.qualifier;

import address.model.datatypes.Person;

public interface Qualifier {
    boolean run(Person person);
}
