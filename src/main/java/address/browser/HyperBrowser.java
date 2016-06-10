package address.browser;

import address.browser.embeddedbrowser.EmbeddedBrowser;
import address.browser.jxbrowser.JxBrowserAdapter;
import address.browser.page.Page;
import address.util.FxViewUtil;
import address.util.UrlUtil;
import com.teamdev.jxbrowser.chromium.Browser;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A HyperBrowser capable of displaying web pages instantaneously when the page has been cached previously.
 * This is achieved by keeping a cached pool of pages when loadUrls is called with a list of URLs that will be
 * called in the future.
 */
public class HyperBrowser {


    public static final int NUMBER_OF_PRELOADED_PAGE = 3;

    private final int noOfPages;

    private ArrayList<Page> pages;

    /**
     * For recycling of browser instances.
     */
    private Stack<EmbeddedBrowser> inActiveBrowserStack;

    private AnchorPane hyperBrowserView;
    private Optional<Node> initialScreen;

    /**
     * @param noOfPages The cache configuration setting of the HyperBrowser.
     *                  Recommended Value: HyperBrowser.NUMBER_OF_PRELOADED_PAGE
     * @param initialScreen The initial screen of HyperBrowser view.
     */
    public HyperBrowser(int noOfPages, Optional<Node> initialScreen){
        this.noOfPages = noOfPages;
        this.initialScreen = initialScreen;
        initialiseHyperBrowser();
    }

    private void initialiseHyperBrowser(){
        this.hyperBrowserView = new AnchorPane();

        if (initialScreen.isPresent()) {
            hyperBrowserView.getChildren().add(initialScreen.get());
        }

        pages = new ArrayList<>(noOfPages);
        inActiveBrowserStack = new Stack<>();

        for (int i=0; i<noOfPages; i++){
            EmbeddedBrowser browser = new JxBrowserAdapter(new Browser());
            FxViewUtil.applyAnchorBoundaryParameters(browser.getBrowserView(), 0.0, 0.0, 0.0, 0.0);
            inActiveBrowserStack.push(browser);
        }
    }

    public AnchorPane getHyperBrowserView() {
        return hyperBrowserView;
    }

    public void dispose() {
        inActiveBrowserStack.stream().forEach(embeddedBrowser -> embeddedBrowser.dispose());
    }

    /**
     * Clears page from the paging system of HyperBrowser that are not required anymore.
     * @param url The URL of page that is to be cleared, if exists.
     * @return An optional page that is cleared from the pool of pages.
     */
    public synchronized void clearPage(URL url) {
        Optional<Page> page = pages.stream().filter(p
                -> {
            try {
                return UrlUtil.compareBaseUrls(url, p.getBrowser().getUrl());
            } catch (MalformedURLException e) {
                return false;
            }
        }).findAny();

        if (page.isPresent()){
            if (hyperBrowserView.getChildren().contains(page.get().getBrowser().getBrowserView())){
                hyperBrowserView.getChildren().remove(0);
            }
            inActiveBrowserStack.push(page.get().getBrowser());
            pages.remove(page.get());
        }
        assert pages.size() + inActiveBrowserStack.size() == NUMBER_OF_PRELOADED_PAGE;
    }

    /**
     * Gets a list of URL from HyperBrowser displayed page and cached pages.
     * @return A list of URL from HyperBrowser displayed page and cached pages.
     */
    public List<URL> getCachedPagesUrl(){
        return pages.stream().map(page -> {
            try {
                return page.getBrowser().getUrl();
            } catch (MalformedURLException e) {
                return null;
            }
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Loads the URLs to the browser.
     * @param url The URL of the content to load.
     * @param futureUrl The URLs that may be called to load in the next T time.
     * @return The page of the URL content.
     * @throws IllegalArgumentException When the amount of URLs to load is more than no of pages the paging system
     *                                      of the HyperBrowser has.
     * @throws NullPointerException When url is null.
     */
    public synchronized Page loadUrls(URL url, List<URL> futureUrl) throws NullPointerException,
                                                                           IllegalArgumentException {
        if (url == null) {
            throw new NullPointerException();
        }

        if (futureUrl == null) {
            futureUrl = new ArrayList<>(0);
        }

        if (futureUrl.size() + 1 > noOfPages) {
            throw new IllegalArgumentException("The HyperBrowser can not load " + (futureUrl.size() + 1) + "URLs. "
                    + "The HyperBrowser is configured to load a maximum of " + noOfPages + "URL.");
        }

        clearPagesNotRequired(getListOfUrlToBeLoaded(url, futureUrl));
        Page page = loadPage(url);
        replaceBrowserView(page.getBrowser().getBrowserView());

        futureUrl.forEach(fUrl -> loadPage(fUrl));

        return page;
    }

    private List<URL> getListOfUrlToBeLoaded(URL url, List<URL> futureUrl) {
        ArrayList<URL> listOfUrlToBeLoaded = new ArrayList<>();
        listOfUrlToBeLoaded.add(url);
        listOfUrlToBeLoaded.addAll(futureUrl);
        return listOfUrlToBeLoaded;
    }

    /**
     * Loads the URL content to the paging system of the HyperBrowser.
     * @param url The url to load the page.
     * @return The view of URL Content.
     */
    private synchronized Page loadPage(URL url) {
        Optional<Page> foundPage = pages.stream().filter(page -> {
            try {
                return UrlUtil.compareBaseUrls(url, page.getBrowser().getUrl());
            } catch (MalformedURLException e) {
                return false;
            }
        }).findAny();

        if (foundPage.isPresent()) {
            return foundPage.get();
        }

        assert !inActiveBrowserStack.isEmpty();
        EmbeddedBrowser browser = inActiveBrowserStack.pop();

        browser.loadUrl(url.toExternalForm());
        Page newPage = new Page(browser);
        pages.add(newPage);

        return newPage;
    }

    /**
     * Clears pages from paging system of HyperBrowser that are not required anymore.
     * @param urlsToLoad The URLs of the pages which are to be remained in the paging system.
     * @return An array list of pages that are cleared from paging system.
     */
    private synchronized ArrayList<Page> clearPagesNotRequired(List<URL> urlsToLoad) {
        /**
         * TODO: handle the efficiency issue when there is few urlsToLoad than noOfPages of the HyperBrowser,
         * in this case some of the url that are not needed can be kept.
         */
        ArrayList<Page> listOfNotRequiredPage = pages.stream().filter(page
              -> {
            for (URL url: urlsToLoad){
                try {
                    if (UrlUtil.compareBaseUrls(url, page.getBrowser().getUrl())) {
                        return false;
                    }
                } catch (MalformedURLException e) {
                }
            }
            return true;
        }).collect(Collectors.toCollection(ArrayList::new));

        listOfNotRequiredPage.stream().forEach(page -> {
            inActiveBrowserStack.push(page.getBrowser());
            pages.remove(page);
        });
        assert pages.size() + inActiveBrowserStack.size() == NUMBER_OF_PRELOADED_PAGE;
        return listOfNotRequiredPage;
    }

    private void replaceBrowserView(Node browserView) {
        if (hyperBrowserView.getChildren().size() >= 1){
            hyperBrowserView.getChildren().removeAll(hyperBrowserView.getChildren());
        }
        hyperBrowserView.getChildren().add(browserView);
    }
}
