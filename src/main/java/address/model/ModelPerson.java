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
        if (e.isSuccessful) {
            System.out.println("Change for '" + this.getFirstName() + " " + this.getLastName() + "'" +
                    " successful on cloud. Pending status now false.");
            if (e.affectedData.contains(this)) this.setPending(false);
        }
    }
}