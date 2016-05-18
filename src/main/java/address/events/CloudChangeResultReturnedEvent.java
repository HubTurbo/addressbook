package address.events;

import address.model.UniqueData;

import java.util.List;

public class CloudChangeResultReturnedEvent {
    public enum Result {
        ADD, DELETE, EDIT
    }

    public List<UniqueData> affectedData;
    public boolean isSuccessful;
    public Result operationType;

    public CloudChangeResultReturnedEvent(Result operationType, List<UniqueData> affectedData, boolean isSuccessful) {
        this.affectedData = affectedData;
        this.isSuccessful = isSuccessful;
        this.operationType = operationType;
    }
}
