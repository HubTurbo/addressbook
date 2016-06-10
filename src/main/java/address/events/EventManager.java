package address.events;

import address.util.LoggerManager;
import com.google.common.eventbus.EventBus;
import org.apache.logging.log4j.Logger;

/**
 * Manages the event dispatching of the app.
 */
public class EventManager {
    private static final Logger logger = LoggerManager.getLogger(EventManager.class);
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

    /**
     * Posts an event to the event bus.
     * @param event
     * @param <E>
     * @return
     */
    public <E extends BaseEvent> EventManager post(E event) {
        logger.info("{}: {}", event.getClass().getSimpleName(), event);
        return postEvent(event);
    }

    private <E extends BaseEvent> EventManager postEvent(E event) {
        eventBus.post(event);
        return this;
    }

    /**
     * Similar to {@link #post} event, but logs at debug level.
     * To be used for less important events.
     * @param event
     * @param <E>
     * @return
     */
    public <E extends BaseEvent> EventManager postPotentialEvent(E event) {
        logger.debug("{}: {}", event.getClass().getSimpleName(), event);
        return postEvent((E) event);
    }

}
