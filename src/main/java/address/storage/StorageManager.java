package address.storage;

import address.events.*;
import address.model.ModelManager;
import address.model.Person;
import address.preferences.PreferencesManager;
import address.util.XmlHelper;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.util.Collections;
import java.util.List;



public class StorageManager {

    private ModelManager modelManager;

    public StorageManager(){
        EventManager.getInstance().registerHandler(this);
    }


    /**
     * Loads person data from the specified file. The current person data will
     * be replaced.
     *
     */
    public void loadPersonDataFromFile(File file) throws Exception {
        List<Person> data  = XmlHelper.getDataFromFile(file);

        modelManager.resetData(data);

        // Save the file path to the registry.
        PreferencesManager.getInstance().setPersonFilePath(file);
    }



    @Subscribe
    private void handleLoadDataRequestEvent(LoadDataRequestEvent ofe) {
        try {
            loadPersonDataFromFile(ofe.file);
        } catch (Exception e) {
            EventManager.getInstance().post(new FileOpeningExceptionEvent(e,ofe.file));
        }
    }

    /**
     * Saves the current person data to the specified file.
     *
     * @param file
     */
    public void savePersonDataToFile(File file, List<Person> personData) {
        try {
            XmlHelper.saveToFile(file, personData);

            // Save the file path to the registry.
            PreferencesManager.getInstance().setPersonFilePath(file);

        } catch (Exception e) {
            EventManager.getInstance().post(new FileSavingExceptionEvent(e,file));
        }
    }


    @Subscribe
    private void handleSaveRequestEvent(SaveRequestEvent se){
        savePersonDataToFile(se.file, se.personData);
    }

    public List<Person> getPersonDataFromFile(File file)  {
        try {
            return file == null ? null : XmlHelper.getDataFromFile(file);
        } catch (Exception e) {
            EventManager.getInstance().post(new FileOpeningExceptionEvent(e, file));
            return Collections.emptyList();
        }
    }

    public void setModel(ModelManager modelManager) {
        this.modelManager = modelManager;
    }
}
