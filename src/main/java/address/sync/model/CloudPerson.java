package address.sync.model;

import java.time.LocalDateTime;
import java.util.List;

public class CloudPerson {
    private int id;
    private String firstName;
    private String lastName;
    private String street;
    private String city;
    private String postalCode;
    private List<CloudTag> tags;
    private boolean isDeleted;
    private LocalDateTime lastUpdatedAt;

    public CloudPerson() {
        setLastUpdatedAt(LocalDateTime.now());
    }

    public CloudPerson(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public List<CloudTag> getTags() {
        return tags;
    }

    public void setTags(List<CloudTag> tags) {
        this.tags = tags;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void updatedBy(CloudPerson updatedPerson) {
        this.firstName = updatedPerson.firstName;
        this.lastName = updatedPerson.lastName;
        this.street = updatedPerson.street;
        this.city = updatedPerson.city;
        this.postalCode = updatedPerson.postalCode;
        this.tags = updatedPerson.tags;
        this.isDeleted = updatedPerson.isDeleted;
        this.lastUpdatedAt = updatedPerson.lastUpdatedAt;
    }

    private void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
