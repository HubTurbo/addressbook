package address.parser.qualifier;

import address.model.datatypes.person.Person;
import address.util.StringUtil;

public class LastNameQualifier implements Qualifier {
    private String lastName;

    public LastNameQualifier(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean run(Person person) {
        return StringUtil.containsIgnoreCase(person.getLastName(), lastName);
    }
}
