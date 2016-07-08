package address.controller;

import address.MainApp;
import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.fxbrowser.FxBrowserAdapter;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

/**
 * Controller for a help page
 */
public class HelpController {

    @FXML
    private AnchorPane mainPane;

    public HelpController() {

    }

    @FXML
    public void initialize() {
        EmbeddedBrowser browser = new FxBrowserAdapter(new WebView());
        browser.loadUrl(MainApp.class.getResource("/help_html/index.html").toExternalForm());
        mainPane.getChildren().add(browser.getBrowserView());
    }
}
