package address.parser.qualifier;

import address.model.datatypes.person.ReadOnlyViewablePerson;

public class NameQualifier implements Qualifier {
    private final String name;

    public NameQualifier(String name) {
        this.name = name;
    }

    @Override
    public boolean run(ReadOnlyViewablePerson person) {
        Qualifier firstNameQualifier = new FirstNameQualifier(name);
        Qualifier lastNameQualifier = new LastNameQualifier(name);

        return firstNameQualifier.run(person) || lastNameQualifier.run(person);
    }
}
