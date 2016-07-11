package address.sync.cloud.model;

import address.util.XmlUtil.LocalDateTimeAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;

@XmlRootElement(name = "cloudtag")
public class CloudTag {
    private String name;

    private LocalDateTime lastUpdatedAt;

    public CloudTag() {
        setLastUpdatedAt(LocalDateTime.now());
    }

    public CloudTag(String name) {
        this.name = name;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public CloudTag(CloudTag cloudTag) {
        updatedBy(cloudTag);
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public void updatedBy(CloudTag updatedTag) {
        this.name = updatedTag.name;
        setLastUpdatedAt(LocalDateTime.now());
    }

    @XmlElement(name = "lastUpdatedAt")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    private void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public boolean isValid() {
        return name != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CloudTag cloudTag = (CloudTag) o;

        return name != null ? name.equals(cloudTag.name) : cloudTag.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
