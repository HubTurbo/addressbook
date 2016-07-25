package address.parser.qualifier;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import commons.StringUtil;

public class LastNameQualifier implements Qualifier {
    private String lastName;

    public LastNameQualifier(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean run(ReadOnlyViewablePerson person) {
        return StringUtil.containsIgnoreCase(person.getLastName(), lastName);
    }

    @Override
    public String toString() {
        return "lastName=" + lastName;
    }
}
