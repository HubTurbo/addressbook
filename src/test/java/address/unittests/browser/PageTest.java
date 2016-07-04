package address.unittests.browser;

import address.browser.page.GithubProfilePage;
import address.util.JavafxRuntimeRule;
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
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * To test the Page methods.
 */
public class PageTest {

    private static final String VALID_ID_1 = "js-pjax-container";
    private static final String VALID_CLASS_NAME_1 = "octicon octicon-repo";
    public static final String VALID_CLASS_NAME_2 = "columns profilecols";
    public static final String VALID_ID_2 = "js-flash-container";
    public static final String VALID_ID_3 = "contributions-calendar";

    @Rule
    public JavafxRuntimeRule rule = new JavafxRuntimeRule();

    @Test
    public void testPageTestMethods_fullFeatureBrowser() throws IOException {
        Page page = getSampleGithubProfilePage(BrowserType.FULL_FEATURE_BROWSER);

        assertNotNull(page.getElementByClass(VALID_CLASS_NAME_1));
        assertNull(page.getElementByClass("singaporeFlyer"));

        assertNotNull(page.getElementById(VALID_ID_1));
        assertNull(page.getElementById("maryland"));

        assertTrue(page.verifyPresence(new String[]{VALID_ID_1, VALID_CLASS_NAME_1}));
        assertFalse(page.verifyPresence(new String[]{VALID_ID_1, "lalaland"}));

        assertTrue(page.verifyPresenceByClassNames(new String[]{VALID_CLASS_NAME_1, VALID_CLASS_NAME_2}));
        assertFalse(page.verifyPresenceByClassNames(new String[]{"disney", VALID_CLASS_NAME_2}));

        assertTrue(page.verifyPresenceByClassNames(VALID_CLASS_NAME_2));
        assertFalse(page.verifyPresenceByClassNames("disney"));

        assertTrue(page.verifyPresenceByIds(new String[]{VALID_ID_1, VALID_ID_2, VALID_ID_3}));
        assertFalse(page.verifyPresenceByIds(new String[]{"hubturbo", VALID_ID_2, "teammates"}));

        assertTrue(page.verifyPresenceByIds(VALID_ID_1));
        assertFalse(page.verifyPresenceByIds("hubturbo"));

        GithubProfilePage gPage = new GithubProfilePage(page);
        assertTrue(gPage.isValidGithubProfilePage());

        page.getBrowser().dispose();
    }

    private Page getSampleGithubProfilePage(BrowserType type) throws IOException {
        EmbeddedBrowser browser = EmbeddedBrowserFactory.createBrowser(type);
        InputStream stream = this.getClass().getResourceAsStream("/html_pages/github_profile_page.html");
        String html = IOUtils.toString(stream);
        stream.close();
        browser.loadHTML(html);
        Page page = new Page(browser);
        while(page.isPageLoading());
        return new Page(browser);
    }

}
