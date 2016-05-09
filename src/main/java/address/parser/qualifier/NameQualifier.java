package address.parser.qualifier;

import address.model.Person;
import address.util.StringUtil;

public class NameQualifier implements Qualifier {

    public final String name;

    public NameQualifier(String name) {
        this.name = name;
    }

    @Override
    public boolean run(Person person) {
        return StringUtil.containsIgnoreCase(person.getFirstName(), name) ||
            StringUtil.containsIgnoreCase(person.getLastName(), name);
    }
}
