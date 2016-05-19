package address.events;

import address.model.UniqueData;

import java.util.List;

public class CloudChangeResultReturnedEvent extends BaseEvent {
    public enum Type {
        ADD, DELETE, EDIT
    }

    public List<UniqueData> affectedData;
    public boolean isSuccessful;
    public Type operationType;

    public CloudChangeResultReturnedEvent(Type operationType, List<UniqueData> affectedData, boolean isSuccessful) {
        this.affectedData = affectedData;
        this.isSuccessful = isSuccessful;
        this.operationType = operationType;
    }

    @Override
    public String toString() {
        return "Cloud change request result: " + (isSuccessful ? "success!" : "failure.");
    }
}
