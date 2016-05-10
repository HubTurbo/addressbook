package address.events;

/**
 * Indicates that a filter was committed by the user.
 */
public class FilterCommittedEvent {

    public final String filter;

    public FilterCommittedEvent(String filter){
        this.filter = filter;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + " : " + filter;
    }
}
