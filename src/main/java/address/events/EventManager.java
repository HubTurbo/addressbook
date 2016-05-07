package address.events;

import com.google.common.eventbus.EventBus;

public class EventManager {
    private static EventBus eventBus;
    private static EventManager instance;

    public static EventManager getInstance(){
        if(instance == null){
            instance = new EventManager();
        }
        return instance;
    }

    private EventManager(){
        eventBus = new EventBus();
    }

    public void registerHandler(Object handler){
        eventBus.register(handler);
    }

    public void post(Object event){
        System.out.println("Event posted : " + event);
        eventBus.post(event);
    }

}
