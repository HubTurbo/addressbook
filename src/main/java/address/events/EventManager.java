package address.events;

import com.google.common.eventbus.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the event dispatching of the app.
 */
public class EventManager {
    private static final Logger logger = LogManager.getLogger(EventManager.class);
    private final EventBus eventBus;
    private static EventManager instance;

    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public static void clearSubscribers() {
        instance = null;
    }

    private EventManager() {
        eventBus = new EventBus();
    }

    public EventManager registerHandler(Object handler){
        eventBus.register(handler);
        return this;
    }

    public <E extends BaseEvent> EventManager post(E event) {
        logger.info("{}: {}", event.getClass().getSimpleName(), event);
        eventBus.post(event);
        return this;
    }

}
