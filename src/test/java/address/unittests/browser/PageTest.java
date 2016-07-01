package address.unittests.browser;

import address.util.JavafxThreadingRule;
import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.BrowserType;
import hubturbo.embeddedbrowser.EbElement;
import hubturbo.embeddedbrowser.EmbeddedBrowserFactory;
import hubturbo.embeddedbrowser.page.Page;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * To test the Page methods.
 */
public class PageTest {

    private static final String REPO_LIST_CLASS_NAME = "repo-list js-repo-list";
    private static final String ORGANIZATION_REPO_ID = "org-repositories";
    private static final String JS_PJAX_CONTAINER_ID = "js-pjax-container";
    private static final String OCTICON_REPO_CLASS_NAME = "octicon octicon-repo";

    @Rule
    public JavafxThreadingRule rule = new JavafxThreadingRule();

    @Test
    public void testGetElementByClass_fullFeatureBrowser_elementFound() throws IOException {
        Page page = getSampleGithubProfilePage();
        assertNotNull(page.getElementByClass(OCTICON_REPO_CLASS_NAME));
    }

    @Test
    public void testGetElementByClass_fullFeatureBrowser_elementNotFound() throws IOException {
        Page page = getSampleGithubProfilePage();
        assertNull(page.getElementByClass("singaporeFlyer"));
    }

    @Test
    public void testGetElementById_fullFeatureBrowser_elementFound() throws IOException {
        Page page = getSampleGithubProfilePage();
        assertNotNull(page.getElementById(JS_PJAX_CONTAINER_ID));
    }

    @Test
    public void testGetElementById_fullFeatureBrowser_elementNotFound() throws IOException {
        Page page = getSampleGithubProfilePage();

        assertNull(page.getElementById("maryland"));
    }

    @Test
    public void testVerifyPresence_fullFeatureBrowser_true() throws IOException {
        Page page = getSampleGithubProfilePage();
        assertTrue(page.verifyPresence(new String[]{JS_PJAX_CONTAINER_ID, OCTICON_REPO_CLASS_NAME}));
    }

    @Test
    public void testVerifyPresence_fullFeatureBrowser_false() throws IOException {
        Page page = getSampleGithubProfilePage();
        Boolean success = false;
        success = page.verifyPresence(new String[]{JS_PJAX_CONTAINER_ID, "lalaland"});
        assertFalse(success);
    }

    private Page getSampleGithubProfilePage() throws IOException {
        EmbeddedBrowser browser = EmbeddedBrowserFactory.createBrowser(BrowserType.FULL_FEATURE_BROWSER);
        InputStream stream = this.getClass().getResourceAsStream("/html_pages/github_profile_page.html");
        String html = IOUtils.toString(stream);
        browser.loadHTML(html);
        Page page = new Page(browser);
        while(page.isPageLoading());

        int noOfTries = 20;
        while(noOfTries > 0) {
            if (page.getBrowser().getDomElement() != null) {
                break;
            }
            noOfTries--;
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return page;
    }
}
