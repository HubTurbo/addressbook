package address.events;

import address.model.IModelData;

public class CloudChangeResultReturnedEvent {
    public enum Result {
        ADD, DELETE, EDIT
    }

    public IModelData affectedData;
    public boolean isSuccessful;
    public Result operationType;

    public CloudChangeResultReturnedEvent(Result operationType, IModelData affectedData, boolean isSuccessful) {
        this.affectedData = affectedData;
        this.isSuccessful = isSuccessful;
        this.operationType = operationType;
    }
}
