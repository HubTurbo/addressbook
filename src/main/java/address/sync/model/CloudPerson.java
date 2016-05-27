package address.sync.model;

import java.util.List;

public class CloudPerson {
    int id;
    String firstName;
    String lastName;
    String street;
    String city;
    String postalCode;
    List<CloudGroup> groups;
    boolean isDeleted;

    public CloudPerson(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public List<CloudGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<CloudGroup> groups) {
        this.groups = groups;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public void updatedBy(CloudPerson updatedPerson) {
        this.firstName = updatedPerson.firstName;
        this.lastName = updatedPerson.lastName;
        this.street = updatedPerson.street;
        this.city = updatedPerson.city;
        this.postalCode = updatedPerson.postalCode;
        this.groups = updatedPerson.groups;
        this.isDeleted = updatedPerson.isDeleted;
    }
}
