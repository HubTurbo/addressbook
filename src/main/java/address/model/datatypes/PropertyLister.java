package address.model.datatypes;

import javafx.beans.property.Property;

import java.util.List;

public interface PropertyLister {

    /**
     * @return deterministically ordered list of all javaFX Property fields
     */
    List<Property> getPropertiesInOrder();
}
