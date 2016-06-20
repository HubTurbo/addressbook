package address.controller;

import address.MainApp;
import address.events.*;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import address.model.ModelManager;
import address.util.AppLogger;
import address.util.Config;
import address.browser.BrowserManager;

import address.util.LoggerManager;
import address.util.ReorderedList;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * The controller that creates the other controllers
 */
public class MainController {
    private static final AppLogger logger = LoggerManager.getLogger(MainController.class);
    private static final String FXML_STATUS_BAR_FOOTER = "/view/StatusBarFooter.fxml";
    private static final String FXML_TAG_EDIT_DIALOG = "/view/TagEditDialog.fxml";
    private static final String FXML_PERSON_EDIT_DIALOG = "/view/PersonEditDialog.fxml";
    private static final String FXML_PERSON_OVERVIEW = "/view/PersonOverview.fxml";
    private static final String FXML_TAG_LIST = "/view/TagList.fxml";
    private static final String FXML_BIRTHDAY_STATISTICS = "/view/BirthdayStatistics.fxml";
    private static final String FXML_ROOT_LAYOUT = "/view/RootLayout.fxml";
    private static final String ICON_APPLICATION = "/images/address_book_32.png";
    private static final String ICON_EDIT = "/images/edit.png";
    private static final String ICON_CALENDAR = "/images/calendar.png";

    private Stage primaryStage;
    private VBox rootLayout;

    private ModelManager modelManager;
    private BrowserManager browserManager;
    private MainApp mainApp;

    private StatusBarHeaderController statusBarHeaderController;

    private ReorderedList reorderedList;

    public MainController(MainApp mainApp, ModelManager modelManager) {
        EventManager.getInstance().registerHandler(this);
        this.modelManager = modelManager;
        this.mainApp = mainApp;
        this.reorderedList = new ReorderedList(modelManager.getAllViewablePersonsReadOnly());
        this.browserManager = new BrowserManager(reorderedList.getDisplayedList());
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(Config.getConfig().appTitle);

        // Set the application icon.
        this.primaryStage.getIcons().add(getImage(ICON_APPLICATION));

        initRootLayout();
        showPersonOverview();
        showPersonWebPage();
        showFooterStatusBar();
        showHeaderStatusBar();
    }

    /**
     * Initializes the root layout and tries to load the last opened
     * person file.
     */
    public void initRootLayout() {
        logger.debug("Initializing root layout.");
        final String fxmlResourcePath = FXML_ROOT_LAYOUT;
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            rootLayout = loader.load();
            SplitPane pane = (SplitPane) rootLayout.lookup("#splitPane");
            pane.setDividerPositions(0.3f);

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            scene.setOnKeyPressed(event -> {
                EventManager.getInstance().postPotentialEvent(new KeyBindingEvent(event));
            });
            primaryStage.setMinHeight(400);
            primaryStage.setMinWidth(740);
            primaryStage.setHeight(600);
            primaryStage.setWidth(340);
            primaryStage.setScene(scene);

            // Give the rootController access to the main controller and modelManager
            RootLayoutController rootController = loader.getController();
            rootController.setConnections(mainApp, this, modelManager);
            rootController.setAccelerators();

            primaryStage.show();
        } catch (IOException e) {
            logger.warn("Error initializing root layout: {}", e);
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml root layout.",
                                   "IOException when trying to load " + fxmlResourcePath);
        }
    }

    /**
     * Shows the person overview inside the root layout.
     */
    public void showPersonOverview() {
        logger.debug("Loading person overview.");
        final String fxmlResourcePath = FXML_PERSON_OVERVIEW;
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            AnchorPane personOverview = loader.load();
            personOverview.setMinWidth(340);
            personOverview.setPrefWidth(340);
            SplitPane pane = (SplitPane) rootLayout.lookup("#splitPane");
            pane.setResizableWithParent(personOverview, false);
            pane.getItems().add(personOverview);
            // Give the personOverviewController access to the main app and modelManager.
            PersonOverviewController personOverviewController = loader.getController();
            personOverviewController.setConnections(this, modelManager, reorderedList);

        } catch (IOException e) {
            logger.warn("Error loading person overview: {}", e);
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml for person overview.",
                                   "IOException when trying to load " + fxmlResourcePath);
        }
    }

    public StatusBarHeaderController getStatusBarHeaderController() {
        return statusBarHeaderController;
    }

    private void showHeaderStatusBar() {
        statusBarHeaderController = new StatusBarHeaderController();
        rootLayout.getChildren().add(2, statusBarHeaderController.getFooterStatusBarView());
    }

    private void showFooterStatusBar() {
        logger.debug("Loading footer status bar.");
        final String fxmlResourcePath = FXML_STATUS_BAR_FOOTER;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            GridPane gPane = loader.load();
            gPane.getStyleClass().add("grid-pane");
            StatusBarFooterController controller = loader.getController();
            controller.initStatusBar();
            rootLayout.getChildren().add(gPane);

        } catch (IOException e) {
            logger.warn("Error Loading footer status bar: {}", e);
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml for footer status bar.",
                    "IOException when trying to load " + fxmlResourcePath);
        }
    }

    /**
     * Get user input for defining a Person object.
     *
     * @param defaultData default data shown for user input
     * @return a defensively copied optional containing the input data from user, or an empty optional if the
     *          operation is to be cancelled.
     */
    public Optional<ReadOnlyPerson> getPersonDataInput(ReadOnlyPerson defaultData) {
        return showPersonEditDialog(defaultData);
    }

    /**
     * Opens a dialog to edit details for a Person object. If the user
     * clicks OK, the input data is recorded in a new Person object and returned.
     *
     * @param initialData the person object determining the initial data in the input fields
     * @return an optional containing the new data, or an empty optional if there was an error
     *         creating the dialog or the user clicked cancel
     */
    private Optional<ReadOnlyPerson> showPersonEditDialog(ReadOnlyPerson initialData) {
        logger.debug("Loading dialog for person edit.");
        final String fxmlResourcePath = FXML_PERSON_EDIT_DIALOG;
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Person");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    dialogStage.close();
                }
            });
            dialogStage.setScene(scene);
            dialogStage.getIcons().add(getImage(ICON_EDIT));

            // Pass relevant data into the controller.
            PersonEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setInitialPersonData(initialData);
            controller.setTagsModel(modelManager.getTagsAsReadOnlyObservableList(),
                    new ArrayList<>(initialData.getObservableTagList()));

            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                return Optional.of(controller.getFinalInput());
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            logger.warn("Error loading person edit dialog: {}", e);
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml for edit person dialog.",
                                   "IOException when trying to load " + fxmlResourcePath);
            return Optional.empty();
        }
    }

    /**
     * Opens a dialog to edit details for the specified tag. If the user
     * clicks OK, the changes are recorded in a new Tag and returned.
     *
     * @param tag the tag object determining the initial data in the input fields
     * @return an optional containing the new data, or an empty optional if there was an error
     *         creating the dialog or the user clicked cancel
     */
    public Optional<Tag> getTagDataInput(Tag tag) {
        logger.debug("Loading dialog for tag edit.");
        final String fxmlResourcePath = FXML_TAG_EDIT_DIALOG;
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Tag");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.getIcons().add(getImage(ICON_EDIT));

            // Pass relevant data to the controller.
            TagEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setInitialTagData(tag);

            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                return Optional.of(controller.getFinalInput());
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            logger.warn("Error loading tag edit dialog: {}", e);
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml for edit tag dialog.",
                                   "IOException when trying to load " + fxmlResourcePath);
            return Optional.empty();
        }
    }

    public void showTagList(ObservableList<Tag> tags) {
        logger.debug("Loading tag list.");
        final String fxmlResourcePath = FXML_TAG_LIST;
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("List of Tags");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the tag into the controller.
            TagListController tagListController = loader.getController();
            tagListController.setTags(tags, this, modelManager);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();
        } catch (IOException e) {
            logger.warn("Error loading tag list view: {}", e);
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml for tag list.",
                                   "IOException when trying to load " + fxmlResourcePath);
        }
    }

    /**
     * Opens a dialog to show birthday statistics.
     */
    public void showBirthdayStatistics() {
        logger.debug("Loading birthday statistics.");
        final String fxmlResourcePath = FXML_BIRTHDAY_STATISTICS;
        try {
            // Load the fxml file and create a new stage for the popup.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Birthday Statistics");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.getIcons().add(getImage(ICON_CALENDAR));

            // Set the persons into the controller.
            BirthdayStatisticsController controller = loader.getController();
            controller.setPersonData(modelManager.getAllViewablePersonsReadOnly());

            dialogStage.show();
        } catch (IOException e) {
            logger.warn("Error loading birthday statistics view: {}", e);
            showAlertDialogAndWait(AlertType.ERROR, "FXML Load Error", "Cannot load fxml for birthday stats.",
                                   "IOException when trying to load " + fxmlResourcePath);
        }
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @Subscribe
    private void handleFileOpeningExceptionEvent(FileOpeningExceptionEvent foee) {
        showFileOperationAlertAndWait("Could not load data", "Could not load data from file", foee.file,
                                      foee.exception);
    }

    @Subscribe
    private void handleFileSavingExceptionEvent(FileSavingExceptionEvent fsee) {
        showFileOperationAlertAndWait("Could not save data", "Could not save data to file", fsee.file, fsee.exception);
    }

    private void showFileOperationAlertAndWait(String description, String details, File file, Throwable cause) {
        final String content = details + ":\n" + (file == null ? "none" : file.getPath()) + "\n\nDetails:\n======\n"
                                + cause.toString();
        showAlertDialogAndWait(AlertType.ERROR, "File Op Error", description, content.toString());
    }

    private Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

    public void showAlertDialogAndWait(AlertType type, String title, String headerText, String contentText) {
        showAlertDialogAndWait(primaryStage, type, title, headerText, contentText);
    }

    public static void showAlertDialogAndWait(Stage owner, AlertType type, String title, String headerText,
                                              String contentText) {
        final Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    /**
     *  Releases resources to ensure successful application termination.
     */
    public void releaseResourcesForAppTermination(){
        browserManager.freeBrowserResources();
    }

    public void loadGithubProfilePage(ReadOnlyViewablePerson person){
        browserManager.loadProfilePage(person);
    }

    public void showPersonWebPage() {
        SplitPane pane = (SplitPane) rootLayout.lookup("#splitPane");
        pane.getItems().add(browserManager.getHyperBrowserView());
    }

    @Subscribe
    private void handleMaximizeAppRequestEvent(MaximizeAppRequestEvent event){
        logger.debug("Handling the maximize app window request");
        Platform.runLater(() -> maximizeWindow());
    }

    @Subscribe
    private void handleMinimizeAppRequestEvent(MinimizeAppRequestEvent event){
        logger.debug("Handling the minimize app window request");
        Platform.runLater(() -> minimizeWindow());
    }


    private void minimizeWindow() {
        primaryStage.setIconified(true);
        primaryStage.setMaximized(false);
    }

    private void maximizeWindow() {
        primaryStage.setMaximized(true);
        primaryStage.setIconified(false);
    }

}
