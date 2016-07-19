package hubturbo.embeddedbrowser;

import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.page.Page;
import address.util.AppLogger;
import commons.FxViewUtil;
import address.util.LoggerManager;
import commons.UrlUtil;
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
    private static AppLogger logger = LoggerManager.getLogger(HyperBrowser.class);

    public static final int RECOMMENDED_NUMBER_OF_PAGES = 3;

    private final int noOfPages;

    private List<Page> pages;

    /**
     * For recycling of browser instances.
     */
    private Stack<EmbeddedBrowser> inActiveBrowserStack;

    private AnchorPane hyperBrowserView;
    private Optional<Node> initialScreen;

    private URL displayedUrl;

    private BrowserType browserType;

    /**
     * @param browserType The type of browser
     * @param noOfPages The cache configuration setting of the HyperBrowser.
     *                  Recommended Value: HyperBrowser.RECOMMENDED_NUMBER_OF_PAGES
     * @param initialScreen The initial screen of HyperBrowser view.
     */
    public HyperBrowser(BrowserType browserType, int noOfPages, Optional<Node> initialScreen){
        this.browserType = browserType;
        this.noOfPages = noOfPages;
        this.initialScreen = initialScreen;
        initialiseHyperBrowser();
    }

    private void initialiseHyperBrowser(){
        this.hyperBrowserView = new AnchorPane();
        FxViewUtil.applyAnchorBoundaryParameters(hyperBrowserView, 0.0, 0.0, 0.0, 0.0);
        if (initialScreen.isPresent()) {
            hyperBrowserView.getChildren().add(initialScreen.get());
        }

        pages = new ArrayList<>(noOfPages);
        inActiveBrowserStack = new Stack<>();

        for (int i = 0; i < noOfPages; i++){
            EmbeddedBrowser browser = EmbeddedBrowserFactory.createBrowser(browserType);
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
        Optional<Page> page = pages.stream().filter(p -> {
            try {
                return UrlUtil.compareBaseUrls(url, p.getBrowser().getOriginUrl());
            } catch (MalformedURLException e) {
                return false;
            }
        }).findAny();

        if (page.isPresent()){
            if (hyperBrowserView.getChildren().contains(page.get().getBrowser().getBrowserView())){
                hyperBrowserView.getChildren().remove(0);
            }
            reclaimBrowser(page.get());
            pages.remove(page.get());
        }
        assert pages.size() + inActiveBrowserStack.size() == noOfPages;
    }

    private void reclaimBrowser(Page page) {
        page.getBrowser().reset();
        inActiveBrowserStack.push(page.getBrowser());
    }

    /**
     * Loads the HTML content.
     * @param htmlCode The HTML Content
     * @return The page containing the HTML content.
     */
    public Page loadHTML(String htmlCode) {
        Optional<Page> page = pages.stream().filter(p -> {
            try {
                return p.getBrowser().getOriginUrl().equals(displayedUrl);
            } catch (MalformedURLException e) {
                return false;
            }
        }).findAny();

        if (page.isPresent()) {
            page.get().getBrowser().loadHTML(htmlCode);
            replaceBrowserView(page.get().getBrowser().getBrowserView());
            return page.get();
        } else {
            Page sparePage = pages.get(0);
            sparePage.getBrowser().loadHTML(htmlCode);
            replaceBrowserView(sparePage.getBrowser().getBrowserView());
            return sparePage;
        }
    }

    public synchronized List<Page> loadUrl(URL url) throws IllegalArgumentException {
        return this.loadUrls(url, Collections.emptyList());
    }

    /**
     * Loads the URLs to the browser.
     * @param url The URL of the content to load.
     * @param futureUrls The non-nullable list of URLs that may be called to load in the next T time.
     * @return The pages of the url and futureUrls contents. The url page is located at index 0.
     * @throws IllegalArgumentException When the amount of URLs to load is more than no of pages the paging system
     *                                      of the HyperBrowser has.
     */
    public synchronized List<Page> loadUrls(URL url, List<URL> futureUrls) throws IllegalArgumentException {
        if (url == null || futureUrls == null) {
            throw new NullPointerException();
        }

        if (futureUrls.size() + 1 > noOfPages) {
            throw new IllegalArgumentException("The HyperBrowser can not load " + (futureUrls.size() + 1) + "URLs. "
                    + "The HyperBrowser is configured to load a maximum of " + noOfPages +  "URLs.");
        }

        clearPagesNotRequired(getListOfUrlToBeLoaded(url, futureUrls));
        Page page = loadPage(url);
        replaceBrowserView(page.getBrowser().getBrowserView());
        displayedUrl = url;

        List<Page> pages = new ArrayList<>();
        pages.add(page);
        futureUrls.forEach(p -> pages.add(this.loadPage(p)));

        return pages;
    }

    private List<URL> getListOfUrlToBeLoaded(URL url, List<URL> futureUrl) {
        List<URL> listOfUrlToBeLoaded = new ArrayList<>();
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
                return UrlUtil.compareBaseUrls(url, page.getBrowser().getOriginUrl());
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
    private synchronized void clearPagesNotRequired(List<URL> urlsToLoad) {
        logger.debug("Clearing pages which are no longer required.");
        Deque<Page> listOfNotRequiredPage = pages.stream().filter(page -> {
            for (URL url: urlsToLoad) {
                try {
                    if (UrlUtil.compareBaseUrls(url, page.getBrowser().getOriginUrl())) {
                        return false;
                    }
                } catch (MalformedURLException e) {
                    logger.warn("Malformed URL detected in existing pages: {}", e);
                    // TODO proper handling instead of just logging
                }
            }
            return true;
        }).collect(Collectors.toCollection(ArrayDeque::new));

        Optional<Page> currDisplayedPage = listOfNotRequiredPage.stream().filter(page -> {
            try {
                return UrlUtil.compareBaseUrls(page.getBrowser().getOriginUrl(), displayedUrl);
            } catch (MalformedURLException e) {
                return false;
            }
        }).findAny();

        if (currDisplayedPage.isPresent()) {
            //So that, current displayed page can be removed first.
            shiftElementToBottomOfList(listOfNotRequiredPage, currDisplayedPage);
        }

        int popCount = 0;
        while (!listOfNotRequiredPage.isEmpty() && popCount < urlsToLoad.size()) {
            Page page = listOfNotRequiredPage.poll();
            reclaimBrowser(page);
            pages.remove(page);
            popCount++;
        }
        assert pages.size() + inActiveBrowserStack.size() == noOfPages;
    }

    private void shiftElementToBottomOfList(Deque<Page> listOfNotRequiredPage, Optional<Page> currDisplayedPage) {
        listOfNotRequiredPage.addFirst(currDisplayedPage.get());
        listOfNotRequiredPage.removeLastOccurrence(currDisplayedPage.get());
    }

    private void replaceBrowserView(Node browserView) {
        if (hyperBrowserView.getChildren().size() >= 1){
            hyperBrowserView.getChildren().removeAll(hyperBrowserView.getChildren());
        }
        hyperBrowserView.getChildren().add(browserView);
        logger.debug("Updated browser view.");
    }

    public URL getDisplayedUrl() {
        return displayedUrl;
    }
}
