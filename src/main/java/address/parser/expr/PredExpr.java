package address.parser.expr;

import address.model.Person;
import address.parser.qualifier.Qualifier;

public class PredExpr implements Expr {

    private final Qualifier qualifier;

    public PredExpr(Qualifier qualifier) {
        this.qualifier = qualifier;
    }

    @Override
    public boolean satisfies(Person person) {
        return qualifier.run(person);
    }
}
