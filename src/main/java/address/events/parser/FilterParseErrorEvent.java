package address.events.parser;

import address.events.BaseEvent;

/**
 * Indicates that there was a parse error in a filter.
 */
public class FilterParseErrorEvent extends BaseEvent {

    public final String reason;

    public FilterParseErrorEvent(String reason){
        this.reason = reason;
    }

    @Override
    public String toString(){
        return reason;
    }
}
