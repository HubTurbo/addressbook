package address.parser.qualifier;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import commons.StringUtil;

public class FirstNameQualifier implements Qualifier {
    private String firstName;

    public FirstNameQualifier(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public boolean run(ReadOnlyViewablePerson person) {
        return StringUtil.containsIgnoreCase(person.getFirstName(), firstName);
    }
}
