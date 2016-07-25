package address.parser.qualifier;

import address.model.datatypes.person.ReadOnlyViewablePerson;

public class TrueQualifier implements Qualifier {

    public TrueQualifier() {
    }

    @Override
    public boolean run(ReadOnlyViewablePerson person) {
        return true;
    }

    @Override
    public String toString() {
        return "TRUE";
    }
}
