package address.parser.qualifier;

import address.model.datatypes.person.ReadOnlyViewablePerson;

public class NameQualifier implements Qualifier {
    private Qualifier firstNameQualifier;
    private Qualifier lastNameQualifier;

    public NameQualifier(String name) {
        firstNameQualifier = new FirstNameQualifier(name);
        lastNameQualifier = new LastNameQualifier(name);
    }

    @Override
    public boolean run(ReadOnlyViewablePerson person) {

        return firstNameQualifier.run(person) || lastNameQualifier.run(person);
    }

    @Override
    public String toString() {
        return "(" + firstNameQualifier + " OR " + lastNameQualifier + ")";
    }
}
