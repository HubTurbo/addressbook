package installer;

import address.updater.model.LibraryDescriptor;
import address.updater.model.UpdateData;
import address.util.FileUtil;
import address.util.JsonUtil;
import address.util.Version;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class UpdateDataGenerator {
    public static void main(String[] args) {
        UpdateData updateData = new UpdateData();
        updateData.setVersion(Version.getCurrentVersion());

        List<String> arguments = Arrays.asList(args);

        updateData.setMainAppFilename(arguments.get(0));

        ArrayList<String> librariesName = new ArrayList<>(arguments.subList(1, arguments.size()));

        ArrayList<LibraryDescriptor> librariesDescriptor = librariesName.stream()
                .map(lib -> new LibraryDescriptor(lib, LibraryDescriptor.Os.ALL))
                .collect(Collectors.toCollection(ArrayList::new));

        updateData.setLibraries(librariesDescriptor);

        try {
            FileUtil.writeToFile(new File("UpdateData.json"), JsonUtil.toJsonString(updateData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
