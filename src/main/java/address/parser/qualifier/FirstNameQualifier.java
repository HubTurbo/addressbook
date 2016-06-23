package address.parser.qualifier;

import address.model.datatypes.person.Person;
import address.util.StringUtil;

public class FirstNameQualifier implements Qualifier {
    private String firstName;

    public FirstNameQualifier(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public boolean run(Person person) {
        return StringUtil.containsIgnoreCase(person.getFirstName(), firstName);
    }
}
