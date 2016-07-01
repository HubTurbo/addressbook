package address.util;

import address.TestApp;
import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ViewablePerson;
import address.model.datatypes.tag.Tag;
import address.storage.StorageAddressBook;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
        createDataFileWithData(generateSampleStorageAddressBook(), filePath);
    }

    public static void createDataFileWithData(StorageAddressBook data, String filePath) {
        try {
            File saveFileForTesting = new File(filePath);
            FileUtil.createIfMissing(saveFileForTesting);
            XmlUtil.saveDataToFile(saveFileForTesting, data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String... s) {
        createDataFileWithSampleData(TestApp.SAVE_LOCATION_FOR_TESTING);
    }

    public static AddressBook generateSampleAddressBook(){
        return new AddressBook(Arrays.asList(samplePersonData), Arrays.asList(sampleTagData));
    }

    public static StorageAddressBook generateSampleStorageAddressBook() {
        return new StorageAddressBook(generateSampleAddressBook());
    }

    /**
     * Tweaks the {@code keyCodeCombination} to resolve the {@code KeyCode.SHORTCUT} to their
     * respective platform-specific keycodes
     */
    public static KeyCode[] scrub(KeyCodeCombination keyCodeCombination) {
        List<KeyCode> keys = new ArrayList<>();
        if (keyCodeCombination.getAlt() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.ALT);
        }
        if (keyCodeCombination.getShift() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.SHIFT);
        }
        if (keyCodeCombination.getMeta() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.META);
        }
        if (keyCodeCombination.getControl() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.CONTROL);
        }
        if (keyCodeCombination.getShortcut() == KeyCombination.ModifierValue.DOWN) {
            keys.add(getPlatformSpecificShortcutKey());

        }
        keys.add(keyCodeCombination.getCode());
        return keys.toArray(new KeyCode[]{});
    }

    public static boolean isHeadlessEnvironment() {
        String headlessProperty = System.getProperty("testfx.headless");
        return headlessProperty != null && headlessProperty.equals("true");
    }

    /**
     * Returns {@code KeyCode.COMMAND or KeyCode.META} if on a Mac depending on headful/headless mode,
     * {@code KeyCode.CONTROL} otherwise.
     */
    private static KeyCode getPlatformSpecificShortcutKey() {
        KeyCode macShortcut = isHeadlessEnvironment() ? KeyCode.COMMAND : KeyCode.META;
        return OsDetector.isOnMac() ? macShortcut : KeyCode.CONTROL;
    }

    /**
     * Replaces any {@code KeyCode.SHORTCUT or KeyCode.META} with {@code KeyCode.META or KeyCode.COMMAND} on Macs
     * depending on headful/headless mode, and {@code KeyCode.CONTROL} on other platforms.
     */
    public static KeyCode[] scrub(KeyCode[] keyCodes) {
        for (int i = 0; i < keyCodes.length; i++) {
            if (keyCodes[i] == KeyCode.META || keyCodes[i] == KeyCode.SHORTCUT) {
                keyCodes[i] = getPlatformSpecificShortcutKey();
            }
        }
        return keyCodes;
    }

    /**
     * Generates a minimal {@link KeyEvent} object that matches the {@code keyCombination}
     */
    public static KeyEvent getKeyEvent(String keyCombination) {
        String[] keys = keyCombination.split(" ");

        String key = keys[keys.length - 1];
        boolean shiftDown = keyCombination.toLowerCase().contains("shift");
        boolean metaDown = keyCombination.toLowerCase().contains("meta")
                || (keyCombination.toLowerCase().contains("shortcut") && OsDetector.isOnMac());
        boolean altDown = keyCombination.toLowerCase().contains("alt");
        boolean ctrlDown = keyCombination.toLowerCase().contains("ctrl")
                || keyCombination.toLowerCase().contains("shortcut") && !OsDetector.isOnMac();
        return new KeyEvent(null, null, null, KeyCode.valueOf(key), shiftDown, ctrlDown, altDown, metaDown);
    }

    public static void setFinalStatic(Field field, Object newValue) throws NoSuchFieldException, IllegalAccessException{
        field.setAccessible(true);
        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
