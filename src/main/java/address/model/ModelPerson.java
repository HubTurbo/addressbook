package address.model;

public class ModelPerson extends Person implements IModelData {
    private boolean isPending;

    public ModelPerson(boolean isPending) {
        super();
        this.isPending = isPending;
    }

    public ModelPerson(Person person, boolean isPending) {
        super(person);
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
