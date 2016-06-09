package address.browser;

import address.MainApp;
import address.browser.page.Page;
import address.exceptions.IllegalArgumentSizeException;
import address.util.FxViewUtil;
import address.util.UrlUtil;
import com.teamdev.jxbrowser.chromium.Browser;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages a pool of activePages, where each has a non-identical content.
 *
 * To ensure correct functionality and consistency of the pool of pages:
 * 1) To use loadPersonPage(), calling clearPagesNotRequired() or clearPage() first is required.
 *    - Call clearPage(parameter) when only a single person page (non-identical with the pool of pages)
 *      is to be loaded using the loadPersonPage() method.
 *    - Call clearPagesNotRequired(parameter) when multiple person page (non-identical with the pool of pages)
 *      are to be loaded using the loadPersonPage() method.
 */
public class HyperBrowser {

    private static final String FXML_BROWSER_PLACE_HOLDER_SCREEN = "/view/DefaultBrowserPlaceHolderScreen.fxml";

    private ArrayList<Page> activePages;

    /**
     * For recycling of browser instance.
     */
    private Stack<EmbeddedBrowser> inActiveBrowserStack;

    private final int noOfPages;

    private AnchorPane hyperBrowserView;
    private HBox defaultInstructionScreen;

    public HyperBrowser(int noOfPages){
        this.noOfPages = noOfPages;
        initialiseHyperBrowser();
    }

    private void initialiseHyperBrowser(){
        String fxmlResourcePath = FXML_BROWSER_PLACE_HOLDER_SCREEN;
        try {
            this.hyperBrowserView = new AnchorPane();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            defaultInstructionScreen = (HBox) loader.load();
            hyperBrowserView.getChildren().add(defaultInstructionScreen);
        } catch (IOException e) {
            e.printStackTrace();
        }

        activePages = new ArrayList<>(noOfPages);
        inActiveBrowserStack = new Stack<>();

        for (int i=0; i<noOfPages; i++){
            EmbeddedBrowser browser = new JxBrowserAdapter(new Browser());
            FxViewUtil.applyAnchorBoundaryParameters(browser.getBrowserView(), 0.0, 0.0, 0.0, 0.0);
            inActiveBrowserStack.push(browser);
        }
    }

    /**
     * Loads the person's profile page.
     * Precondition: 1) url
     *               2)futureUrl.size() + 1 must not be more than noOfPages otherwise IllegalArgumentSizeException
     *               will be thrown.
     * @param person The person whose profile page is to be loaded.
     * @return The browser assigned to load the person's profile page.
     */
    /**
     * Loads the person's profile page.
     * @param url The URL to load to the HyperBrowser view.
     * @param futureUrl The URLs that may be called to load in the next T time.
     * @throws IllegalArgumentSizeException
     */
    public synchronized void loadPersonPage(URL url, ArrayList<URL> futureUrl) throws IllegalArgumentSizeException
                                                                                            , NullPointerException {

        if (url == null) {
            throw new NullPointerException();
        }

        if (futureUrl.size() + 1 > noOfPages) {
            throw new IllegalArgumentSizeException(futureUrl.size() + 1, noOfPages);
        }

        ArrayList<String> listOfUrlToBeLoaded = new ArrayList<String>();
        listOfUrlToBeLoaded.add(url);
        listOfUrlToBeLoaded.addAll(futureUrl);
        clearPagesNotRequired(listOfUrlToBeLoaded);

        Optional<Page> foundPage = activePages.stream().filter(page
                -> page.getBrowser().getBaseUrl().equals(UrlUtil.getBaseUrl(url))).findAny();
        if (foundPage.isPresent()) {
            replaceBrowserView(foundPage.get().getBrowser().getBrowserView());
            return;
        }

        Page newPage;
        EmbeddedBrowser browser;

        assert !inActiveBrowserStack.isEmpty();
        browser = inActiveBrowserStack.pop();

        browser.loadPage(url);
        newPage = new Page(browser);
        activePages.add(newPage);
        replaceBrowserView(browser.getBrowserView());

        futureUrl.forEach(fUrl -> {
            assert !inActiveBrowserStack.isEmpty();
            final EmbeddedBrowser popBrowser = inActiveBrowserStack.pop();
            popBrowser.loadPage(fUrl);
            activePages.add(new GithubProfilePage(popBrowser));
        });
    }

    /**
     * Clears pages from the pool of pages that are not required anymore.
     * @param urlsToLoad The persons whose pages are to be remained in the pool of pages.
     *                        Preconditions: If loadPersonPage(Person person) method is going to be called after this
     *                                       method, the requiredPersons must contain the person that will be used in
     *                                       loadPersonPage() method.
     * @return An arraylist of pages that are cleared from the pool of pages.
     */
    public synchronized ArrayList<GithubProfilePage> clearPagesNotRequired(ArrayList<String> urlsToLoad) {
        /**
         * TODO: handle the efficiency issue when there is few urlsToLoad than noOfPages of the HyperBrowser,
         * in this case some of the not needed url can be kept.
         */
        ArrayList<GithubProfilePage> listOfNotRequiredPage = activePages.stream().filter(page
              -> {
            String tmp = page.getBrowser().getBaseUrl();
            return !urlsToLoad.contains(tmp);
        })
                                           .collect(Collectors.toCollection(ArrayList::new));
        listOfNotRequiredPage.stream().forEach(page -> {
            inActiveBrowserStack.push(page.getBrowser());
            activePages.remove(page);
        });
        assert activePages.size() + inActiveBrowserStack.size() == BrowserManager.NUMBER_OF_PRELOADED_PAGE;
        return listOfNotRequiredPage;
    }

    /**
     * Clears page from the pool of activePages that are not required anymore.
     * @param url The url whose page is to be cleared, if exists.
     * @return An optional page that is cleared from the pool of pages.
     */
    public synchronized Optional<Page> clearPage(URL url) {
        Optional<Page> page = activePages.stream().filter(p
                -> {
            try {
                return UrlUtil.compareBaseUrls(url, p.getBrowser().getUrl());
            } catch (MalformedURLException e) {
                return false;
            }
        }).findAny();

        if (page.isPresent()){
            inActiveBrowserStack.push(page.get().getBrowser());
            activePages.remove(page.get());
        }
        assert activePages.size() + inActiveBrowserStack.size() == BrowserManager.NUMBER_OF_PRELOADED_PAGE;
        return page;
    }



    public void dispose() {
        inActiveBrowserStack.stream().forEach(embeddedBrowser -> embeddedBrowser.dispose());
    }

    /**
     * Gets the person instance from the list of active pages.
     * @return An array list of person instance.
     */
    public ArrayList<URL> getActivePagesUrl(){
        return activePages.stream().map(page -> page.getBrowser().getUrlString())
                                   .collect(Collectors.toCollection(ArrayList::new));
    }

    private void replaceBrowserView(Node browserView) {
        if (hyperBrowserView.getChildren().size() >= 1){
            hyperBrowserView.getChildren().removeAll(hyperBrowserView.getChildren());
        }
        hyperBrowserView.getChildren().add(browserView);
    }

    public AnchorPane getHyperBrowserView() {
        return hyperBrowserView;
    }
}
