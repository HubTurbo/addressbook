package address.parser.expr;

import address.model.datatypes.Person;

public interface Expr {
    boolean satisfies(Person person);
}
