package address.controller;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * The View class for the {@link HelpWindow}.
 */
public class HelpWindowView extends BaseView{
    private static final String ICON_HELP = "/images/help_icon.png";
    AnchorPane page;

    @Override
    String getFxmlFileName() {
        return "HelpWindow.fxml";
    }

    public HelpWindowView(Stage primaryStage) {
        super(primaryStage);
        page = (AnchorPane) mainNode;
    }

    public void show() {
        Scene scene = new Scene(page);
        Stage dialogStage = loadDialogStage("Help", null, scene);
        dialogStage.getIcons().add(getImage(ICON_HELP));
        dialogStage.setMaximized(true);
        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();
    }

    private Stage loadDialogStage(String value, Stage primaryStage, Scene scene) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(value);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.setScene(scene);
        return dialogStage;
    }


}
