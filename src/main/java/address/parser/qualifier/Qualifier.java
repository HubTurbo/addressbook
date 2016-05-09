package address.parser.qualifier;

import address.model.Person;

public interface Qualifier {
    boolean run(Person person);
}
