package address.parser.expr;

import address.model.datatypes.person.Person;

public class AndExpr implements Expr {

    private final Expr left;
    private final Expr right;

    public AndExpr(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean satisfies(Person person) {
        return left.satisfies(person) && right.satisfies(person);
    }
}
