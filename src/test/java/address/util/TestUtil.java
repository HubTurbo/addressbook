package address.util;

import address.model.datatypes.person.Person;
import address.model.datatypes.person.ViewablePerson;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class for test cases.
 */
public class TestUtil {

    public static final Person[] samplePersonData = {
            new Person("Hans", "Muster", -1),
            new Person("Ruth", "Mueller", -2),
            new Person("Heinz", "Kurz", -3),
            new Person("Cornelia", "Meier", -4),
            new Person("Werner", "Meyer", -5),
            new Person("Lydia", "Kunz", -6),
            new Person("Anna", "Best", -7),
            new Person("Stefan", "Meier", -8),
            new Person("Martin", "Mueller", -9)
    };

    public static List<Person> generateSamplePersonData() {
        return Arrays.asList(samplePersonData);
    }

    public static List<ViewablePerson> generateSampleViewablePersonData() {
        return generateSamplePersonData().stream().map(ViewablePerson::fromBacking).collect(Collectors.toList());
    }
}
