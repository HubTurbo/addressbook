package address.parser.expr;

import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyViewablePerson;

public class AndExpr implements Expr {

    private final Expr left;
    private final Expr right;

    public AndExpr(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean satisfies(ReadOnlyViewablePerson person) {
        return left.satisfies(person) && right.satisfies(person);
    }

    @Override
    public String toString() {
        return "(" + left + ") AND (" + right + ")";
    }
}
