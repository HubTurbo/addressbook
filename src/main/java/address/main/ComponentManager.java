package address.main;

import address.events.BaseEvent;
import address.events.EventManager;

/**
 * Base class for *Manager classes
 */
public class ComponentManager {
    protected EventManager eventManager;

    public ComponentManager(EventManager eventManager){
        this.eventManager = eventManager;
        eventManager.registerHandler(this);
    }

    protected void raise(BaseEvent event){
        eventManager.post(event);
    }
}
