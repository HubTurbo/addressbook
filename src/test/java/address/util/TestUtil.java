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

    public static List<Person> generateSamplePersonData() {
        final Person[] samplePersonData = {
                new Person("Hans", "Muster"),
                new Person("Ruth", "Mueller"),
                new Person("Heinz", "Kurz"),
                new Person("Cornelia", "Meier"),
                new Person("Werner", "Meyer"),
                new Person("Lydia", "Kunz"),
                new Person("Anna", "Best"),
                new Person("Stefan", "Meier"),
                new Person("Martin", "Mueller")
        };
        return Arrays.asList(samplePersonData);
    }

    public static List<ViewablePerson> generateSampleViewablePersonData() {
        final Person[] samplePersonData = {
                new Person("Hans", "Muster"),
                new Person("Ruth", "Mueller"),
                new Person("Heinz", "Kurz"),
                new Person("Cornelia", "Meier"),
                new Person("Werner", "Meyer"),
                new Person("Lydia", "Kunz"),
                new Person("Anna", "Best"),
                new Person("Stefan", "Meier"),
                new Person("Martin", "Mueller")
        };
        return generateSamplePersonData().stream().map(ViewablePerson::new).collect(Collectors.toList());
    }
}
