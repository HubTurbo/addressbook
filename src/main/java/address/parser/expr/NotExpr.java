package address.parser.expr;

import address.model.datatypes.person.ReadOnlyViewablePerson;

public class NotExpr implements Expr {
    Expr expr;

    public NotExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public boolean satisfies(ReadOnlyViewablePerson person) {
        return !expr.satisfies(person);
    }

    @Override
    public String toString() {
        return "NOT (" + expr + ")";
    }
}
