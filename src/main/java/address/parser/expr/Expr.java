package address.parser.expr;

import address.model.Person;

public interface Expr {
    boolean satisfies(Person person);
}
