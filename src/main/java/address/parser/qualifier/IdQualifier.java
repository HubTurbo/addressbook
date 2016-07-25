package address.parser.qualifier;

import address.model.datatypes.person.ReadOnlyViewablePerson;

public class IdQualifier implements Qualifier {
    private final int id;

    public IdQualifier(int id) {
        this.id = id;
    }

    @Override
    public boolean run(ReadOnlyViewablePerson person) {
        return person.getId() == id;
    }

    @Override
    public String toString() {
        return "id=" + id;
    }
}
