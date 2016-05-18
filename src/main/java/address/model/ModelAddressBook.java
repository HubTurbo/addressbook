package address.model;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@XmlRootElement(name = "addressbook")
public class ModelAddressBook extends AddressBook implements IModelData {
    private boolean isPending;
    private List<ModelPerson> persons = new ArrayList<>();
    private List<ModelContactGroup> groups = new ArrayList<>();

    public ModelAddressBook(boolean isPending) {
        this.isPending = isPending;
    }

    /*public ModelAddressBook(AddressBook addressBook, boolean isPending) {
        super(addressBook);
        this.isPending = isPending;
    }*/


    @Override
    @XmlElement(name = "persons")
    public List<Person> getPersons() {
        return super.getPersons().stream()
                .map(Person::new)
                .collect(Collectors.toList());
    }


    @Override
    @XmlElement(name = "groups")
    public List<ContactGroup> getGroups() {
        return groups.stream()
                .map(ContactGroup::new)
                .collect(Collectors.toList());
    }

    @Override
    @XmlElement(name = "pending")
    public boolean isPending() {
        return isPending;
    }

    @Override
    public void setPending(boolean isPending) {
        this.isPending = isPending;
    }

    public void setModelPersons(List<ModelPerson> persons) {
        this.persons = persons;
    }

    public void setModelGroups(List<ModelContactGroup> groups) {
        this.groups = groups;
    }

    @Override
    public boolean containsDuplicates() {
        final Set<Person> personSet = new HashSet<>();
        final Set<ContactGroup> groupSet = new HashSet<>();
        for (Person p : persons) {
            if (!personSet.add(p)) return true;
        }
        for (ContactGroup cg : groups) {
            if (!groupSet.add(cg)) return true;
        }
        return false;
    }
}
