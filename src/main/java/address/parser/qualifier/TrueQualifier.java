package address.parser.qualifier;

import address.model.datatypes.person.Person;

public class TrueQualifier implements Qualifier {

    public TrueQualifier() {
    }

    @Override
    public boolean run(Person person) {
        return true;
    }
}
