package address.sync.resource;

import java.util.List;

public class CloudPerson extends CloudResource {
    int id;
    String firstName;
    String lastName;
    String street;
    String city;
    String postalCode;
    List<CloudGroup> groupList;

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

    public List<CloudGroup> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<CloudGroup> groupList) {
        this.groupList = groupList;
    }

}
