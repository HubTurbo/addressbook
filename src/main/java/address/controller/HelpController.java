package address.controller;

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
        browser.loadHTML("This is working :)");
        mainPane.getChildren().add(browser.getBrowserView());
    }
}
