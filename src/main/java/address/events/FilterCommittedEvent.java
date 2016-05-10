package address.events;

import address.parser.expr.Expr;

/**
 * Indicates that a filter was committed by the user.
 */
public class FilterCommittedEvent {

    public final Expr filterExpression;

    public FilterCommittedEvent(Expr filterExpression) {
        this.filterExpression = filterExpression;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " : " + filterExpression;
    }
}
