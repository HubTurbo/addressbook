package address.parser.expr;

import address.model.datatypes.person.ReadOnlyViewablePerson;

public interface Expr {
    boolean satisfies(ReadOnlyViewablePerson person);
    String toString();
}
