package address.sync.model;

import address.util.LocalDateAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDateTime lastUpdatedAt;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate birthday;

    public CloudPerson() {
    }

    public CloudPerson(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.tags = new ArrayList<>();
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

    private void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public void updatedBy(CloudPerson updatedPerson) {
        this.firstName = updatedPerson.firstName;
        this.lastName = updatedPerson.lastName;
        this.street = updatedPerson.street;
        this.city = updatedPerson.city;
        this.postalCode = updatedPerson.postalCode;
        this.tags = updatedPerson.tags;
        this.isDeleted = updatedPerson.isDeleted;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public boolean isValid() {
        return this.firstName != null && this.lastName != null && tags != null;
    }

    public boolean equals(Object o) {
        if (!(o instanceof CloudPerson)) return false;

        if ((firstName == null && ((CloudPerson) o).firstName != null)
                || (firstName != null && !firstName.equals(((CloudPerson) o).firstName))) return false;

        if ((lastName == null && ((CloudPerson) o).lastName != null)
                || (lastName != null && !lastName.equals(((CloudPerson) o).lastName))) return false;

        if ((street == null && ((CloudPerson) o).street != null)
                || (street != null && !street.equals(((CloudPerson) o).street))) return false;

        if ((postalCode == null && ((CloudPerson) o).postalCode != null)
                || (postalCode != null && !postalCode.equals(((CloudPerson) o).postalCode))) return false;

        if ((tags == null && ((CloudPerson) o).tags != null)
                || (tags != null && !tags.equals(((CloudPerson) o).tags))) return false;

        return isDeleted == ((CloudPerson) o).isDeleted;
    }
}
