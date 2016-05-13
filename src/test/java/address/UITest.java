package address;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.loadui.testfx.GuiTest;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;

public class UITest extends GuiTest {
    Stage stage;

    @Override
    protected Parent getRootNode() {
        Parent root = stage.getScene().getRoot();
        stage.getScene().setRoot(new Group());
        return root;
    }

    public UITest() {
        try {
            FXMLLoader.setDefaultClassLoader(MainApp.class.getClassLoader()); // workaround to fxml loading problems
            FxToolkit.registerPrimaryStage();
            FxToolkit.setupApplication(TestApp.class);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        stage = FxToolkit.toolkitContext().getRegisteredStage();
    }
}
