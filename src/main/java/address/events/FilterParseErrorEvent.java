package address.events;

/**
 * Indicates that there was a parse error in a filter.
 */
public class FilterParseErrorEvent {

    public final String reason;

    public FilterParseErrorEvent(String reason){
        this.reason = reason;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + " : " + reason;
    }
}
