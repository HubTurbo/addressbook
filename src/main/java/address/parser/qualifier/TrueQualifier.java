package address.parser.qualifier;

import address.model.Person;

public class TrueQualifier implements Qualifier {

    public TrueQualifier() {
    }

    @Override
    public boolean run(Person person) {
        return true;
    }
}
