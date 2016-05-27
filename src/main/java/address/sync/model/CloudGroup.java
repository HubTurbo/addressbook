package address.sync.model;

public class CloudGroup {
    String name;

    public CloudGroup() {
    }

    public CloudGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void updatedBy(CloudGroup updatedGroup) {
        this.name = updatedGroup.name;
    }
}
