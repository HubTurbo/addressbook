package address.events.parser;

import address.events.BaseEvent;

/**
 * Indicates that a filter was successfully applied
 */
public class FilterSuccessEvent extends BaseEvent {
    public FilterSuccessEvent() {}

    @Override
    public String toString(){
        return "Success!";
    }
}
