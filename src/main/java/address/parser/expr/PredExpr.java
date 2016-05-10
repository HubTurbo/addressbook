package address.parser.expr;

import address.model.Person;
import address.parser.qualifier.Qualifier;
import address.parser.qualifier.TrueQualifier;

public class PredExpr implements Expr {

    public static final PredExpr TRUE = new PredExpr(new TrueQualifier());

    private final Qualifier qualifier;

    public PredExpr(Qualifier qualifier) {
        this.qualifier = qualifier;
    }

    @Override
    public boolean satisfies(Person person) {
        return qualifier.run(person);
    }
}
