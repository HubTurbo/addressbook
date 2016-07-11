package address.sync.cloud.model;

import address.util.XmlUtil.LocalDateAdapter;
import address.util.XmlUtil.LocalDateTimeAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "cloudperson")
public class CloudPerson {
    private int id;
    private String firstName;
    private String lastName;
    private String street;
    private String city;
    private String postalCode;
    private String githubUsername;
    private List<CloudTag> tags;
    private boolean isDeleted;

    private LocalDateTime lastUpdatedAt;

    private LocalDate birthday;

    public CloudPerson() {
        this.id = 0;
        this.tags = new ArrayList<>();
        this.firstName = "";
        this.lastName = "";
        this.street = "";
        this.city = "";
        this.postalCode = "";
        this.githubUsername = "";
        this.isDeleted = false;
    }

    public CloudPerson(String firstName, String lastName) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public CloudPerson(String firstName, String lastName, int id) {
        this(firstName, lastName);
        setId(id);
    }

    public CloudPerson(CloudPerson cloudPerson) {
        updatedBy(cloudPerson);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        setLastUpdatedAt(LocalDateTime.now());
    }


    @XmlElement(name = "firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        setLastUpdatedAt(LocalDateTime.now());
    }

    @XmlElement(name = "lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        setLastUpdatedAt(LocalDateTime.now());
    }

    @XmlElement(name = "street")
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
        setLastUpdatedAt(LocalDateTime.now());
    }

    @XmlElement(name = "city")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        setLastUpdatedAt(LocalDateTime.now());
    }

    @XmlElement(name = "postalCode")
    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        setLastUpdatedAt(LocalDateTime.now());
    }

    @XmlElement(name = "githubUsername")
    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
        setLastUpdatedAt(LocalDateTime.now());
    }

    @XmlElement(name = "tags")
    public List<CloudTag> getTags() {
        return tags;
    }

    public void setTags(List<CloudTag> tags) {
        this.tags = tags;
        setLastUpdatedAt(LocalDateTime.now());
    }

    @XmlElement(name = "deleted")
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
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

    @XmlElement(name = "birthday")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public void updatedBy(CloudPerson updatedPerson) {
        this.firstName = updatedPerson.firstName;
        this.lastName = updatedPerson.lastName;
        this.street = updatedPerson.street;
        this.city = updatedPerson.city;
        this.postalCode = updatedPerson.postalCode;
        this.githubUsername = updatedPerson.githubUsername;
        this.tags = updatedPerson.tags;
        this.isDeleted = updatedPerson.isDeleted;
        this.birthday = updatedPerson.birthday;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public boolean isValid() {
        return this.firstName != null && this.lastName != null && tags != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CloudPerson that = (CloudPerson) o;

        if (id != that.id) return false;
        if (isDeleted != that.isDeleted) return false;
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) return false;
        if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) return false;
        if (street != null ? !street.equals(that.street) : that.street != null) return false;
        if (city != null ? !city.equals(that.city) : that.city != null) return false;
        if (postalCode != null ? !postalCode.equals(that.postalCode) : that.postalCode != null) return false;
        if (tags != null ? !tags.equals(that.tags) : that.tags != null) return false;
        return birthday != null ? birthday.equals(that.birthday) : that.birthday == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (street != null ? street.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (isDeleted ? 1 : 0);
        result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
        return result;
    }
}
