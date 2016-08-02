package address.controller;


import org.controlsfx.control.StatusBar;

public class StatusBarHeaderController extends UiController{

    public static final String HEADER_STATUS_BAR_ID = "headerStatusBar";
    private static final String STATUS_BAR_STYLE_SHEET = "status-bar-with-border";
    private StatusBar headerStatusBar;

    public StatusBarHeaderController(MainController mainController) {
        headerStatusBar = new StatusBar();
        headerStatusBar.setId(HEADER_STATUS_BAR_ID);
        headerStatusBar.getStyleClass().removeAll();
        headerStatusBar.getStyleClass().add(STATUS_BAR_STYLE_SHEET);
        headerStatusBar.setText("");
    }

    public StatusBar getHeaderStatusBarView() {
        return headerStatusBar;
    }
}
