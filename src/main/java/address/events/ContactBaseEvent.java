package address.events;

import address.model.Person;

/**
 * Created by YL Lim on 26/5/2016.
 */
public abstract class ContactBaseEvent extends BaseEvent {

    protected Person person;

    public ContactBaseEvent(Person person) {
        this.person = person;
    }
}
