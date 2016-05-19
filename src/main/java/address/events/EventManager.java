package address.events;

import com.google.common.eventbus.EventBus;

/**
 * Manages the event dispatching of the app.
 */
public class EventManager {
    private final EventBus eventBus;
    private static EventManager instance;

    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    private EventManager() {
        eventBus = new EventBus();
    }

    public EventManager registerHandler(Object handler){
        eventBus.register(handler);
        return this;
    }

    public <E extends BaseEvent> EventManager post(E event) {
        System.out.println(event);
        eventBus.post(event);
        return this;
    }

}
