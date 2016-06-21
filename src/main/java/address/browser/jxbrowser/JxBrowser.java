package address.browser.jxbrowser;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserContext;
import com.teamdev.jxbrowser.chromium.BrowserType;

/**
 * A JxBrowser instance with the ability to know the URL it was tasked to load.
 */
public class JxBrowser extends Browser{

    private String originUrl = "";

    public JxBrowser() {
    }

    public JxBrowser(BrowserContext browserContext) {
        super(browserContext);
    }

    public JxBrowser(BrowserType browserType) {
        super(browserType);
    }

    public JxBrowser(BrowserType browserType, BrowserContext browserContext) {
        super(browserType, browserContext);
    }

    @Override
    public synchronized void loadURL(long l, String s) {
        super.loadURL(l, s);
        originUrl = s;
    }

    @Override
    public synchronized void loadURL(String s) {
        super.loadURL(s);
        originUrl = s;
    }

    public String getOriginUrl() {
        return originUrl;
    }
}
