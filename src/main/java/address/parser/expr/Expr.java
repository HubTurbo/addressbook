package address.parser.expr;

import address.model.datatypes.person.Person;

public interface Expr {
    boolean satisfies(Person person);
}
