package address.model;

import address.events.CloudChangeResultReturnedEvent;
import address.events.EventManager;
import com.google.common.eventbus.Subscribe;

public class ModelPerson extends Person implements IModelData {
    private boolean isPending;

    public ModelPerson(boolean isPending) {
        super();
        this.isPending = isPending;
        EventManager.getInstance().registerHandler(this);
    }

    public ModelPerson(Person person, boolean isPending) {
        super(person);
        this.isPending = isPending;
        EventManager.getInstance().registerHandler(this);
    }

    @Override
    public boolean isPending() {
        return isPending;
    }

    @Override
    public void setPending(boolean isPending) {
        this.isPending = isPending;
    }

    @Subscribe
    public void handleCloudChangeResultReturnedEvent(CloudChangeResultReturnedEvent e) {
        if (e.isSuccessful && e.operationType == CloudChangeResultReturnedEvent.Type.EDIT) {
            System.out.println("Change for '" + this.getFirstName() + " " + this.getLastName() + "'" +
                    " successful on cloud. Pending status now false.");
            if (e.affectedData.contains(this)) this.setPending(false);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof ModelPerson)) return false;
        return super.equals(obj) && this.isPending == ((ModelPerson) obj).isPending;
    }

    @Override
    public int hashCode() {
        // hint that this class should not have been a subclass of Person
        return super.hashCode() + 43;
    }
}
