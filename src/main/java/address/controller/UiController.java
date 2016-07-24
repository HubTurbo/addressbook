package address.controller;

import address.events.BaseEvent;
import address.events.EventManager;
import address.events.KeyBindingEvent;
import javafx.stage.Stage;

/**
 * Parent class for all controllers.
 */
public class UiController {  //TODO: Rename to BaseUiController
    protected Stage primaryStage;

    public UiController(){
        EventManager.getInstance().registerHandler(this);
    }

    protected void raise(BaseEvent event){
        EventManager.getInstance().post(event);
    }

    protected void raisePotentialEvent(BaseEvent event) {
        EventManager.getInstance().postPotentialEvent(event);
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
