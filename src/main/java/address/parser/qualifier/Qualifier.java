package address.parser.qualifier;

import address.model.datatypes.person.Person;

public interface Qualifier {
    boolean run(Person person);
}
