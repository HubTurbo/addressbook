package address.controller;

import address.MainApp;
import commons.FxViewUtil;
import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.fxbrowser.FxBrowserAdapter;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

/**
 * Controller for a help page
 */
public class HelpWindowController {

    @FXML
    private AnchorPane mainPane;

    public HelpWindowController() {

    }

    @FXML
    public void initialize() {
        EmbeddedBrowser browser = new FxBrowserAdapter(new WebView());
        browser.loadUrl(MainApp.class.getResource("/help_html/index.html").toExternalForm());
        FxViewUtil.applyAnchorBoundaryParameters(browser.getBrowserView(), 0.0, 0.0, 0.0, 0.0);
        mainPane.getChildren().add(browser.getBrowserView());
    }
}
