package hubturbo.embeddedbrowser;

/**
 * To contain browser configuration setting
 * TODO: Makes init() method of browserManager non-static. So that we could pass in the BrowserType also.
 */
public class BrowserConfig {

    private final int noOfPages;

    /**
     * @param noOfPages The number of pages the browser will use for its paging system.
     */
    public BrowserConfig(int noOfPages) {
        //this.browserType = browserType;
        this.noOfPages = noOfPages;
    }

    public int getNoOfPages() {
        return noOfPages;
    }
}
