package address.model;


import address.events.CloudChangeResultReturnedEvent;
import com.google.common.eventbus.Subscribe;

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

    public ModelAddressBook() {
    }

    public ModelAddressBook(boolean isPending) {
        this.isPending = isPending;
    }

    public void setModelPersons(List<ModelPerson> persons) {
        this.persons = persons;
    }

    public void setModelGroups(List<ModelContactGroup> groups) {
        this.groups = groups;
    }

    @XmlElement(name = "persons")
    public List<ModelPerson> getModelPersons() {
        return this.persons;
    }

    @XmlElement(name = "groups")
    public List<ModelContactGroup> getModelGroups() {
        return this.groups;
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
