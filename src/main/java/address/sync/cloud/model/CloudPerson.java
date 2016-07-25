package address.sync.cloud.model;

import commons.XmlUtil.LocalDateAdapter;
import commons.XmlUtil.LocalDateTimeAdapter;

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

    {
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

    public CloudPerson() {}

    public CloudPerson(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;;
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
        this.id = id;;
    }


    @XmlElement(name = "firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;;
    }

    @XmlElement(name = "lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;;
    }

    @XmlElement(name = "street")
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;;
    }

    @XmlElement(name = "city")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;;
    }

    @XmlElement(name = "postalCode")
    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;;
    }

    @XmlElement(name = "githubUsername")
    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;;
    }

    @XmlElement(name = "tags")
    public List<CloudTag> getTags() {
        return tags;
    }

    public void setTags(List<CloudTag> tags) {
        this.tags = tags;;
    }

    @XmlElement(name = "deleted")
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;;
    }

    @XmlElement(name = "lastUpdatedAt")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    @XmlElement(name = "birthday")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;;
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
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!other.getClass().isAssignableFrom(CloudPerson.class)) return false;
        CloudPerson otherCP = (CloudPerson) other;
        return id == otherCP.id
                && isDeleted == otherCP.isDeleted
                && firstName.equals(otherCP.firstName)
                && lastName.equals(otherCP.lastName)
                && street == null ? otherCP.street == null : street.equals(otherCP.street)
                && city == null ? otherCP.city == null : city.equals(otherCP.city)
                && postalCode == null ? otherCP.postalCode == null : postalCode.equals(otherCP.postalCode)
                && birthday == null ? otherCP.birthday == null : birthday.equals(otherCP.birthday)
                && tags == null ? otherCP.tags == null : tags.equals(otherCP.tags);
    }

    @Override
    public int hashCode() {
        return new StringBuilder().append(id)
                .append(firstName)
                .append(lastName)
                .append(street)
                .append(city)
                .append(postalCode)
                .append(birthday)
                .append(tags)
                .hashCode();
    }
}
