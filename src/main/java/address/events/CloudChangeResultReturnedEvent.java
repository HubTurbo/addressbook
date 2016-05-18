package address.events;

import address.model.IModelData;
import address.model.ModelPerson;

import java.util.List;

public class CloudChangeResultReturnedEvent {
    public enum Result {
        ADD, DELETE, EDIT
    }

    public List<ModelPerson> affectedData;
    public boolean isSuccessful;
    public Result operationType;

    public CloudChangeResultReturnedEvent(Result operationType, List<ModelPerson> affectedData, boolean isSuccessful) {
        this.affectedData = affectedData;
        this.isSuccessful = isSuccessful;
        this.operationType = operationType;
    }
}
