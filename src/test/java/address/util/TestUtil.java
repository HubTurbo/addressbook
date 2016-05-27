package address.util;

import address.model.Person;

import java.util.Arrays;
import java.util.List;

/**
 * A utility class for test cases.
 */
public class TestUtil {

    public static List<Person> generateSampleData() {
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
}
