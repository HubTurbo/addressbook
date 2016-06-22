package address.util;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ViewablePerson;
import address.model.datatypes.tag.Tag;
import address.storage.StorageAddressBook;

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

    public static final Tag[] sampleTagData = {
            new Tag("relatives"),
            new Tag("friends")
    };
    public static List<Person> generateSamplePersonData() {
        return Arrays.asList(samplePersonData);
    }

    public static List<ViewablePerson> generateSampleViewablePersonData() {
        return generateSamplePersonData().stream().map(ViewablePerson::fromBacking).collect(Collectors.toList());
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
            XmlUtil.saveDataToFile(saveFileForTesting, generateSampleStorageAddressBook());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AddressBook generateSampleAddressBook(){
        return new AddressBook(Arrays.asList(samplePersonData), Arrays.asList(sampleTagData));
    }

    public static StorageAddressBook generateSampleStorageAddressBook() {
        return new StorageAddressBook(generateSampleAddressBook());
    }
}
