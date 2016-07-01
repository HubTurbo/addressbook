package address.unittests.browser;

import address.browser.page.GithubProfilePage;
import address.util.JavafxThreadingRule;
import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.BrowserType;
import hubturbo.embeddedbrowser.EmbeddedBrowserFactory;
import hubturbo.embeddedbrowser.page.Page;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * To test GithubProfilePage.
 */
public class GithubProfilePageTest {

    @Rule
    public JavafxThreadingRule rule = new JavafxThreadingRule();

    @Test
    public void testGitHubProfilePageMethods_fullFeatureBrowser() throws IOException {
        GithubProfilePage gPage = getGithubProfilePage();
        assertTrue(gPage.isPageLoading());
        while(gPage.isPageLoading());
        assertFalse(gPage.isPageLoading());
        assertTrue(gPage.isValidGithubProfilePage());
    }

    private GithubProfilePage getGithubProfilePage() throws IOException {
        EmbeddedBrowser browser = EmbeddedBrowserFactory.createBrowser(BrowserType.FULL_FEATURE_BROWSER);
        InputStream stream = this.getClass().getResourceAsStream("/html_pages/github_profile_page.html");
        String html = IOUtils.toString(stream);
        stream.close();
        browser.loadHTML(html);
        Page page = new Page(browser);
        return new GithubProfilePage(page);
    }
}
