package address.model;

public class ModelContactGroup extends ContactGroup implements IModelData {
    private boolean isPending;

    public ModelContactGroup(boolean isPending) {
        super();
        this.isPending = isPending;
    }

    public ModelContactGroup(ContactGroup contactGroup, boolean isPending) {
        super(contactGroup);
        this.isPending = isPending;
    }

    @Override
    public boolean isPending() {
        return isPending;
    }

    @Override
    public void setPending(boolean isPending) {
        this.isPending = isPending;
    }
}
