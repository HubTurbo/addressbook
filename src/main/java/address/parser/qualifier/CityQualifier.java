package address.parser.qualifier;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.util.StringUtil;

public class CityQualifier implements Qualifier {
    private String city;

    public CityQualifier(String city) {
        this.city = city;
    }

    @Override
    public boolean run(ReadOnlyViewablePerson person) {
        return StringUtil.containsIgnoreCase(person.getCity(), city);
    }
}
