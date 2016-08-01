package address.browser.page;

import hubturbo.embeddedbrowser.EbLoadListener;
import hubturbo.embeddedbrowser.page.Page;
import hubturbo.embeddedbrowser.page.PageInterface;

/**
 * A GitHub profile page
 */
public class GithubProfilePage implements PageInterface {

    private Page page;

    private Boolean wasAutoScrollingSetup = false;

    public GithubProfilePage(Page page) {
        this.page = page;
    }

    @Override
    public void setPageLoadFinishListener(EbLoadListener listener) {
        page.setPageLoadFinishListener(listener);
    }
}
