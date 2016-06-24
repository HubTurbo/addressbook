package address.parser.qualifier;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.util.StringUtil;

public class StreetQualifier implements Qualifier {

    private final String street;

    public StreetQualifier(String street) {
        this.street = street;
    }

    @Override
    public boolean run(ReadOnlyViewablePerson person) {
        return StringUtil.containsIgnoreCase(person.getStreet(), street);
    }
}
