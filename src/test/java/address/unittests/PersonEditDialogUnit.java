package address.unittests;

import address.MainApp;
import address.controller.PersonEditDialogController;
import address.model.ContactGroup;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.GuiTest.find;

public class PersonEditDialogUnit extends ApplicationTest {
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField streetField;
    private TextField postalCodeField;
    private TextField cityField;
    private TextField birthdayField;
    private ScrollPane groupList;
    private TextField groupSearch;
    private ScrollPane groupResults;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/view/PersonEditDialog.fxml"));
            AnchorPane baseNode = loader.load();

            PersonEditDialogController controller = loader.getController();

            ContactGroup group1 = new ContactGroup("enemies");
            ContactGroup group2 = new ContactGroup("friends");
            ContactGroup group3 = new ContactGroup("relatives");
            List<ContactGroup> groups = new ArrayList<>();
            groups.add(group1);
            List<ContactGroup> groups2 = new ArrayList<>();
            groups2.add(group1);
            groups2.add(group2);
            groups2.add(group3);

            controller.setModel(groups2, groups);

            primaryStage.setScene(new Scene(baseNode));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() {
        firstNameField = find("#firstNameField");
        lastNameField = find("#lastNameField");
        streetField = find("#streetField");
        postalCodeField = find("#postalCodeField");
        cityField = find("#cityField");
        birthdayField = find("#birthdayField");
        groupList = find("#groupList");
        groupSearch = find("#groupSearch");
        groupResults = find("#groupResults");
    }

    @Test
    public void testGroupSearch() {
        clickOn(groupSearch).write("frien");

        assertEquals(1, ((VBox) groupResults.getContent()).getChildren().size());
        assertEquals(1, ((VBox) groupList.getContent()).getChildren().size());
    }

    @Test
    public void testGroupSearch2() {
        clickOn(groupSearch).write("rela");

        assertEquals(1, ((VBox) groupResults.getContent()).getChildren().size());
        assertEquals(1, ((VBox) groupList.getContent()).getChildren().size());
    }

    @Test
    public void testGroupSearch3() {
        clickOn(groupSearch).write("e");

        assertEquals(3, ((VBox) groupResults.getContent()).getChildren().size());
        assertEquals(1, ((VBox) groupList.getContent()).getChildren().size());
    }

    @Test
    public void testGroupSearch4() {
        clickOn(groupSearch).write("frie frie");

        assertEquals(1, ((VBox) groupResults.getContent()).getChildren().size());
        assertEquals(2, ((VBox) groupList.getContent()).getChildren().size());
    }

    @After
    public void close() {
    }
}
