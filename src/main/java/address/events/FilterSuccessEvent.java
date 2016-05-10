package address.events;

/**
 * Indicates that a filter was successfully applied
 */
public class FilterSuccessEvent {
    public FilterSuccessEvent() {}

    @Override
    public String toString(){
        return this.getClass().getSimpleName();
    }
}
