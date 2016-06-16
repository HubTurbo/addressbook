package address.browser;

import address.browser.embeddedbrowser.EmbeddedBrowser;
import address.browser.javabrowser.WebViewBrowserAdapter;
import address.browser.jxbrowser.JxBrowserAdapter;
import address.browser.page.Page;
import address.util.AppLogger;
import address.util.FxViewUtil;
import address.util.LoggerManager;
import address.util.UrlUtil;
import com.teamdev.jxbrowser.chromium.Browser;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

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

    public static final int FULL_FEATURE_BROWSER = 1;
    public static final int LIMITED_FEATURE_BROWSER = 2;

    private final int noOfPages;

    private List<Page> pages;

    /**
     * For recycling of browser instances.
     */
    private Stack<EmbeddedBrowser> inActiveBrowserStack;

    private AnchorPane hyperBrowserView;
    private Optional<Node> initialScreen;

    private URL displayedUrl;

    private int browserType;

    /**
     * @param browserType The type of browser. e.g. HyperBrowser.FULL_FEATURE_BROWSER
     * @param noOfPages The cache configuration setting of the HyperBrowser.
     *                  Recommended Value: HyperBrowser.RECOMMENDED_NUMBER_OF_PAGES
     * @param initialScreen The initial screen of HyperBrowser view.
     */
    public HyperBrowser(int browserType, int noOfPages, Optional<Node> initialScreen){
        this.browserType = browserType;
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
            EmbeddedBrowser browser;
            if (browserType == FULL_FEATURE_BROWSER){
                browser = new JxBrowserAdapter(new Browser());
            } else if (browserType == LIMITED_FEATURE_BROWSER){
                browser = new WebViewBrowserAdapter(new WebView());
            } else {
                throw new IllegalArgumentException("No such browser type");
            }

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
        assert pages.size() + inActiveBrowserStack.size() == noOfPages;
    }

    public synchronized Page loadUrl(URL url) throws IllegalArgumentException {
        return this.loadUrls(url, Collections.emptyList());
    }

    /**
     * Loads the URLs to the browser.
     * @param url The URL of the content to load.
     * @param futureUrl The non-nullable list of URLs that may be called to load in the next T time.
     * @return The page of the URL content.
     * @throws IllegalArgumentException When the amount of URLs to load is more than no of pages the paging system
     *                                      of the HyperBrowser has.
     */
    public synchronized Page loadUrls(URL url, List<URL> futureUrl) throws IllegalArgumentException {
        if (url == null || futureUrl == null) {
            throw new NullPointerException();
        }

        if (futureUrl.size() + 1 > noOfPages) {
            throw new IllegalArgumentException("The HyperBrowser can not load " + (futureUrl.size() + 1) + "URLs. "
                    + "The HyperBrowser is configured to load a maximum of " + noOfPages + "URL.");
        }

        clearPagesNotRequired(getListOfUrlToBeLoaded(url, futureUrl));
        Page page = loadPage(url);
        replaceBrowserView(page.getBrowser().getBrowserView());
        displayedUrl = url;
        futureUrl.forEach(fUrl -> loadPage(fUrl));

        return page;
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
    private synchronized void clearPagesNotRequired(List<URL> urlsToLoad) {
        logger.debug("Clearing pages which are no longer required.");
        Deque<Page> listOfNotRequiredPage = pages.stream().filter(page
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
        }).collect(Collectors.toCollection(ArrayDeque::new));

        Optional<Page> currDisplayedPage = listOfNotRequiredPage.stream().filter(page -> {
            try {
                return UrlUtil.compareBaseUrls(page.getBrowser().getUrl(), displayedUrl);
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
            inActiveBrowserStack.push(page.getBrowser());
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
