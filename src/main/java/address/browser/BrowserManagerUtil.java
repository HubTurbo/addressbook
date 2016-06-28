package address.browser;

import address.MainApp;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import hubturbo.embeddedbrowser.HyperBrowser;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An utility class to provide static browserManager specific methods.
 */
public class BrowserManagerUtil {

    private static final String FXML_BROWSER_PLACE_HOLDER_SCREEN = "/view/DefaultBrowserPlaceHolderScreen.fxml";

    /**
     * Gets a list of person that are needed to be loaded to the browser in future.
     */
    public static List<URL> getListOfPersonUrlToLoadInFuture(
            List<ReadOnlyViewablePerson> filteredPersons, int indexOfPerson) {
        URL personUrlToLoad = filteredPersons.get(indexOfPerson).profilePageUrl();
        List<URL> listOfRequiredUrl = new ArrayList<>();
        List<URL> listOfUrls = filteredPersons.stream().map(p
                                    -> p.profilePageUrl()).collect(Collectors.toCollection(ArrayList::new));
        int count = 1;
        int i = 1;
        while (count < HyperBrowser.RECOMMENDED_NUMBER_OF_PAGES && i < filteredPersons.size()) {
            URL url = listOfUrls.get((indexOfPerson + i) % filteredPersons.size());
            if (url.equals(personUrlToLoad) || listOfRequiredUrl.contains(url)){
                i++;
                continue;
            }
            listOfRequiredUrl.add(url);
            i++;
            count++;
        }
        return listOfRequiredUrl;
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
