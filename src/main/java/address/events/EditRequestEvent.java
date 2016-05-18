package address.events;

/**
 * Indicates a request for editing a person
 */
public class EditRequestEvent {


    @Override
    public String toString(){
        return this.getClass().getSimpleName();
    }
}
