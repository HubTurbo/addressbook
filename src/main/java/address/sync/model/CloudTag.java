package address.sync.model;

public class CloudTag {
    String name;

    public CloudTag() {
    }

    public CloudTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void updatedBy(CloudTag updatedTag) {
        this.name = updatedTag.name;
    }
}
