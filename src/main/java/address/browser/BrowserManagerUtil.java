package address.browser;

import address.MainApp;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An utility class to provide static browserManager specific methods.
 */
public class BrowserManagerUtil {

    private static final String FXML_BROWSER_PLACE_HOLDER_SCREEN = "/view/DefaultBrowserPlaceHolderScreen.fxml";

    /**
     * Gets a list of person that are needed to be loaded to the browser in future.
     */
    public static ArrayList<ReadOnlyViewablePerson> getListOfPersonToLoadInFuture(List<ReadOnlyViewablePerson> filteredPersons, int indexOfPerson) {
        ArrayList<ReadOnlyViewablePerson> listOfRequiredPerson = new ArrayList<>();

        for (int i = 1; i < HyperBrowser.RECOMMENDED_NUMBER_OF_PAGES && i < filteredPersons.size(); i++){
            listOfRequiredPerson.add(filteredPersons.get((indexOfPerson + i) % filteredPersons.size()));
        }
        return listOfRequiredPerson;
    }

    public static Optional<Node> getBrowserInitialScreen(){
        String fxmlResourcePath = FXML_BROWSER_PLACE_HOLDER_SCREEN;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            return Optional.ofNullable(loader.load());
        } catch (IOException e){
            return Optional.empty();
        }
    }
}
