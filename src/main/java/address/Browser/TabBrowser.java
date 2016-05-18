package address.browser;

import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.ArrayList;

/**
 * A TabPane UI that consists of a browser with multiple tabs.
 */
public class TabBrowser extends TabPane {

    private ArrayList<TabBrowserPage> pages;

    public TabBrowser(int noOfTabs) {
        this.pages = new ArrayList<TabBrowserPage>();
        for (int i = 0; i < noOfTabs; i++) {
            TabBrowserPage page = new TabBrowserPage();
            this.pages.add(page);
            BrowserView browserView = new BrowserView(page);
            Tab tab = new Tab();
            tab.setContent(browserView);
            this.getTabs().add(tab);
        }
        hideTabs();
    }

    private void hideTabs() {
        this.setVisible(false);
        this.setTabMaxHeight(0);
    }

    public TabBrowserPage getPage(int page){
        return pages.get(page);
    }

    public ArrayList<TabBrowserPage> getAllPages(){
        return pages;
    }

    public int getNumberOfPages(){
        return pages.size();
    }

    public int indexOf(IdentifiableBrowser browser){
        return this.pages.indexOf(browser);
    }

    public void dispose(){
        for (int i = 0; i < pages.size(); i++) {
            pages.get(i).dispose();
        }
    }

    public void selectTab(int indexOfTab){
        this.getSelectionModel().select(indexOfTab);
    }

}
