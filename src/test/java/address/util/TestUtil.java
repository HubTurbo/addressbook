package address.util;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ViewablePerson;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class for test cases.
 */
public class TestUtil {

    /**
     * Folder used for temp files created during testing. Ignored by Git.
     */
    public static String SANDBOX_FOLDER = FileUtil.getPath("./src/test/data/sandbox/");

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
        //TODO: can be simplified by AddressBook::generateSampleData util method
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
        //TODO: can be simplified by AddressBook::generateSampleData util method
    }

    /**
     * Appends the file name to the sandbox folder path
     * @param fileName
     * @return
     */
    public static String appendToSandboxPath(String fileName) {
        return SANDBOX_FOLDER + fileName;
    }

    public static void createDataFileWithSampleData(String filePath) {
        try {
            File saveFileForTesting = new File(filePath);
            FileUtil.createIfMissing(saveFileForTesting);
            XmlUtil.saveDataToFile(saveFileForTesting, AddressBook.generateSampleData());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
