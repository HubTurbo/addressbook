package address.storage;

import address.events.EventManager;
import address.events.FileOpeningExceptionEvent;
import address.events.OpenFileEvent;
import address.events.SaveEvent;
import address.model.Person;
import address.model.PersonListWrapper;
import address.preferences.PreferencesManager;
import com.google.common.eventbus.Subscribe;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;


/**
 * Created by dcsdcr on 4/5/2016.
 */
public class StorageManager {

    public StorageManager(){
        EventManager.getInstance().registerHandler(this);
    }


    /**
     * Loads person data from the specified file. The current person data will
     * be replaced.
     *
     * @param file
     */
    public void loadPersonDataFromFile(File file, ObservableList<Person> personData) throws Exception {
        List<Person> data  = getDataFromFile(file);

        personData.clear();
        personData.addAll(data);

        // Save the file path to the registry.
        PreferencesManager.getInstance().setPersonFilePath(file);
    }

    private List<Person> getDataFromFile(File file) throws JAXBException {
        JAXBContext context = JAXBContext
                .newInstance(PersonListWrapper.class);
        Unmarshaller um = context.createUnmarshaller();

        // Reading XML from the file and unmarshalling.
        return ((PersonListWrapper) um.unmarshal(file)).getPersons();
    }

    @Subscribe
    private void handleOpenFileEvent(OpenFileEvent ofe) {
        try {
            loadPersonDataFromFile(ofe.file, ofe.personData);
        } catch (Exception e) {
            EventManager.getInstance().post(new FileOpeningExceptionEvent(e,ofe.file));
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + ofe.file.getPath());

            alert.showAndWait();
        }
    }

    /**
     * Saves the current person data to the specified file.
     *
     * @param file
     */
    public void savePersonDataToFile(File file, ObservableList<Person> personData) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(PersonListWrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Wrapping our person data.
            PersonListWrapper wrapper = new PersonListWrapper();
            wrapper.setPersons(personData);

            // Marshalling and saving XML to the file.
            m.marshal(wrapper, file);

            // Save the file path to the registry.
            PreferencesManager.getInstance().setPersonFilePath(file);
        } catch (Exception e) { // catches ANY exception
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save data");
            alert.setContentText("Could not save data to file:\n" + file.getPath());

            alert.showAndWait();
        }
    }

    @Subscribe
    private void handleSaveEvent(SaveEvent se){
        savePersonDataToFile(se.file, se.personData);
    }

    public List<Person> getPersonDataFromFile(File file)  {
        try {
            return file == null ? null : getDataFromFile(file);
        } catch (JAXBException e) {
            EventManager.getInstance().post(new FileOpeningExceptionEvent(e, file));
            return Collections.emptyList();
        }
    }
}
