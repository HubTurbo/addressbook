package address.controller;

import address.MainApp;
import address.browser.BrowserManager;
import address.events.*;
import address.exceptions.DuplicateTagException;
import address.model.UserPrefs;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.ModelManager;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import address.util.*;
import address.util.collections.UnmodifiableObservableList;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The controller that creates the other controllers
 */
public class MainController extends UiController{
    private static final AppLogger logger = LoggerManager.getLogger(MainController.class);
    private static final String FXML_ACTIVITY_HISTORY = "/view/ActivityHistory.fxml";
    private static final String FXML_STATUS_BAR_FOOTER = "/view/StatusBarFooter.fxml";
    private static final String FXML_TAG_EDIT_DIALOG = "/view/TagEditDialog.fxml";
    private static final String FXML_PERSON_EDIT_DIALOG = "/view/PersonEditDialog.fxml";
    private static final String FXML_PERSON_OVERVIEW = "/view/PersonOverview.fxml";
    private static final String FXML_TAG_LIST = "/view/TagList.fxml";
    private static final String FXML_BIRTHDAY_STATISTICS = "/view/BirthdayStatistics.fxml";
    private static final String FXML_ROOT_LAYOUT = "/view/RootLayout.fxml";
    private static final String FXML_TAG_SELECTION_EDIT_DIALOG = "/view/TagSelectionEditDialog.fxml";
    private static final String ICON_APPLICATION = "/images/address_book_32.png";
    private static final String ICON_EDIT = "/images/edit.png";
    private static final String ICON_CALENDAR = "/images/calendar.png";
    public static final int MIN_HEIGHT = 400;
    public static final int MIN_WIDTH = 740;

    private Stage primaryStage;
    private VBox rootLayout;

    private ModelManager modelManager;
    private BrowserManager browserManager;
    private MainApp mainApp;
    private Config config;
    private UserPrefs prefs;

    private StatusBarHeaderController statusBarHeaderController;

    private UnmodifiableObservableList<ReadOnlyViewablePerson> personList;

    /**
     * Constructor for mainController
     *
     * @param mainApp
     * @param modelManager
     * @param config should have appTitle and updateInterval set
     */
    public MainController(MainApp mainApp, ModelManager modelManager, Config config, UserPrefs prefs) {
        super();
        this.mainApp = mainApp;
        this.modelManager = modelManager;
        this.config = config;
        this.prefs = prefs;
        this.personList = modelManager.getAllViewablePersonsReadOnly();
        this.browserManager = new BrowserManager(personList, config.getBrowserNoOfPages(), config.getBrowserType());
        this.browserManager.initBrowser();
    }

    public void start(Stage primaryStage) {
        logger.info("Starting main controller.");
        this.primaryStage = primaryStage;
        this.browserManager.start();
        primaryStage.setTitle(config.getAppTitle());

        // Set the application icon.
        this.primaryStage.getIcons().add(getImage(ICON_APPLICATION));

        initRootLayout();
        showPersonOverview();
        showPersonWebPage();
        showFooterStatusBar();
        showHeaderStatusBar();
        //showTipOfTheDay(); TODO: enable this later
    }

    private void showTipOfTheDay() {
        TipOfTheDayController tipOfTheDayController = new TipOfTheDayController(primaryStage);
        tipOfTheDayController.start();
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
            FXMLLoader loader = loadFxml(fxmlResourcePath);
            rootLayout = loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            scene.setOnKeyPressed(event -> raisePotentialEvent(new KeyBindingEvent(event)));
            setMinSize();
            setDefaultSize();
            primaryStage.setScene(scene);

            // Give the rootController access to the main controller and modelManager
            RootLayoutController rootController = loader.getController();
            rootController.setConnections(mainApp, this, modelManager);
            rootController.setAccelerators();

            primaryStage.show();
        } catch (IOException e) {
            logger.fatal("Error initializing root layout: {}", e);
            showFatalErrorDialogAndShutdown("FXML Load Error", "Cannot load fxml root layout.",
                                            "IOException when trying to load ", fxmlResourcePath);
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
            FXMLLoader loader = loadFxml(fxmlResourcePath);
            VBox personOverview = loader.load();
            AnchorPane pane = (AnchorPane) rootLayout.lookup("#personOverview");
            SplitPane.setResizableWithParent(pane, false);
            // Give the personOverviewController access to the main app and modelManager.
            PersonOverviewController personOverviewController = loader.getController();
            personOverviewController.setConnections(this, modelManager, personList);

            pane.getChildren().add(personOverview);
        } catch (IOException e) {
            logger.fatal("Error loading person overview: {}", e);
            showFatalErrorDialogAndShutdown("FXML Load Error", "Cannot load fxml for person overview.",
                                            "IOException when trying to load " , fxmlResourcePath);
        }
    }

    public StatusBarHeaderController getStatusBarHeaderController() {
        return statusBarHeaderController;
    }

    private void showHeaderStatusBar() {
        statusBarHeaderController = new StatusBarHeaderController(this);
        AnchorPane sbPlaceHolder = (AnchorPane) rootLayout.lookup("#headerStatusbarPlaceholder");

        assert sbPlaceHolder != null : "headerStatusbarPlaceHolder node not found in rootLayout";

        FxViewUtil.applyAnchorBoundaryParameters(statusBarHeaderController.getHeaderStatusBarView(), 0.0, 0.0, 0.0, 0.0);
        sbPlaceHolder.getChildren().add(statusBarHeaderController.getHeaderStatusBarView());
    }

    private void showFooterStatusBar() {
        logger.debug("Loading footer status bar.");
        final String fxmlResourcePath = FXML_STATUS_BAR_FOOTER;
        try {
            FXMLLoader loader = loadFxml(fxmlResourcePath);
            GridPane gridPane = loader.load();
            gridPane.getStyleClass().add("grid-pane");
            StatusBarFooterController controller = loader.getController();
            controller.init(config.getUpdateInterval(), config.getAddressBookName());
            AnchorPane placeHolder = (AnchorPane) rootLayout.lookup("#footerStatusbarPlaceholder");
            FxViewUtil.applyAnchorBoundaryParameters(gridPane, 0.0, 0.0, 0.0, 0.0);
            placeHolder.getChildren().add(gridPane);
        } catch (IOException e) {
            logger.fatal("Error Loading footer status bar: {}", e);
            showFatalErrorDialogAndShutdown("FXML Load Error", "Cannot load fxml for footer status bar.",
                                            "IOException when trying to load ", fxmlResourcePath);
        }
    }

    private FXMLLoader loadFxml(String fxmlResourcePath) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
        return loader;
    }

    /**
     * Opens a dialog to edit details for a Person object. If the user
     * clicks OK, the input data is recorded in a new Person object and returned.
     *
     * @param initialData the person object determining the initial data in the input fields
     * @param dialogTitle the title of the dialog shown
     * @return an optional containing the new data, or an empty optional if there was an error
     *         creating the dialog or the user clicked cancel
     */
    public Optional<ReadOnlyPerson> getPersonDataInput(ReadOnlyPerson initialData, String dialogTitle) {
        logger.debug("Loading dialog for person edit.");
        final String fxmlResourcePath = FXML_PERSON_EDIT_DIALOG;
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = loadFxml(fxmlResourcePath);
            AnchorPane page = loader.load();

            Scene scene = new Scene(page);
            Stage dialogStage = loadDialogStage(dialogTitle, primaryStage, scene);
            dialogStage.getIcons().add(getImage(ICON_EDIT));

            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    dialogStage.close();
                }
            });

            // Pass relevant data into the controller.
            PersonEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setInitialPersonData(initialData);
            controller.setTags(modelManager.getTagsAsReadOnlyObservableList(),
                    new ArrayList<>(initialData.getObservableTagList()));

            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                logger.debug("Person collected: " + controller.getEditedPerson().toString());
                return Optional.of(controller.getEditedPerson());
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            logger.fatal("Error loading person edit dialog: {}", e);
            showFatalErrorDialogAndShutdown("FXML Load Error", "Cannot load fxml for edit person dialog.",
                                            "IOException when trying to load ", fxmlResourcePath);
            return Optional.empty();
        }
    }

    public Optional<List<Tag>> getPersonsTagsInput(List<ReadOnlyViewablePerson> persons) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource(FXML_TAG_SELECTION_EDIT_DIALOG));
        AnchorPane pane = null;
        try {
            pane = loader.load();

        } catch (IOException e) {
            logger.warn("Error launching tag selection dialog: {}", e);
            assert false : "Error loading fxml : " + FXML_TAG_SELECTION_EDIT_DIALOG;
        }

        // Create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        Scene scene = new Scene(pane, Color.TRANSPARENT);
        dialogStage.setScene(scene);

        TagSelectionEditDialogController controller = loader.getController();
        controller.setTags(modelManager.getTagsAsReadOnlyObservableList(),
                ReadOnlyPerson.getCommonTags(persons));
        controller.setDialogStage(dialogStage);

        dialogStage.showAndWait();

        if (controller.isOkClicked()) {
            return Optional.of(controller.getFinalAssignedTags());
        }
        return Optional.empty();
    }

    private Stage loadDialogStage(String value, Stage primaryStage, Scene scene) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(value);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.setScene(scene);
        return dialogStage;
    }

    /**
     * Attempts to delete tag data from the model
     *
     * @param tag
     * @return true if successful
     */
    public boolean deleteTagData(Tag tag) {
        return modelManager.deleteTag(tag);
    }

    /**
     * Attempts to add new tag data to the model
     *
     * Tag data is obtained from prompting the user repeatedly until a valid tag is given or until the user cancels
     * @return
     */
    public boolean addTagData() {
        Optional<Tag> newTag = Optional.of(new Tag());
        do {
            newTag = getTagDataInput(newTag.get(), "New Tag");
        } while (newTag.isPresent() && !isAddSuccessful(newTag.get()));

        return newTag.isPresent();
    }

    /**
     * Attempts to edit the given tag and update the resulting tag in the model
     *
     * Tag data is obtained from prompting the user repeatedly until a valid tag is given or until the user cancels
     * @param tag
     * @return
     */
    public boolean editTagData(Tag tag) {
        Optional<Tag> editedTag = Optional.of(tag);
        do {
            editedTag = getTagDataInput(editedTag.get(), "Edit Tag");
        } while (editedTag.isPresent() && !isUpdateSuccessful(tag, editedTag.get()));

        return editedTag.isPresent();
    }

    /**
     * Attempts to add the given new tag to the model, and returns the result
     *
     * @param newTag
     * @return true if add is successful
     */
    private boolean isAddSuccessful(Tag newTag) {
        try {
            modelManager.addTagToBackingModel(newTag);
            return true;
        } catch (DuplicateTagException e) {
            showAlertDialogAndWait(AlertType.WARNING, "Warning", "Cannot have duplicate tag", e.toString());
            return false;
        }
    }

    /**
     * Attempts to update the given tag in the model, and returns the result
     *
     * @param newTag
     * @return true if update is successful
     */
    private boolean isUpdateSuccessful(Tag originalTag, Tag newTag) {
        try {
            modelManager.updateTag(originalTag, newTag);
            return true;
        } catch (DuplicateTagException e) {
            showAlertDialogAndWait(AlertType.WARNING, "Warning", "Cannot have duplicate tag", e.toString());
            return false;
        }
    }

    /**
     * Opens a dialog to edit details for the specified tag. If the user
     * clicks OK, the changes are recorded in a new Tag and returned.
     *
     * @param tag the tag object determining the initial data in the input fields
     * @param dialogTitle the title of the dialog to be shown
     * @return an optional containing the new data, or an empty optional if there was an error
     *         creating the dialog or the user clicked cancel
     */
    public Optional<Tag> getTagDataInput(Tag tag, String dialogTitle) {
        logger.debug("Loading dialog for tag edit.");
        final String fxmlResourcePath = FXML_TAG_EDIT_DIALOG;
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = loadFxml(fxmlResourcePath);
            AnchorPane page = loader.load();

            Scene scene = new Scene(page);
            Stage dialogStage = loadDialogStage(dialogTitle, primaryStage, scene);
            dialogStage.getIcons().add(getImage(ICON_EDIT));

            // Pass relevant data to the controller.
            TagEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setInitialTagData(tag);

            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                return Optional.of(controller.getEditedTag());
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            logger.fatal("Error loading tag edit dialog: {}", e);
            showFatalErrorDialogAndShutdown("FXML Load Error", "Cannot load fxml for edit tag dialog.",
                                            "IOException when trying to load ", fxmlResourcePath);
            return Optional.empty();
        }
    }

    public void showTagList(ObservableList<Tag> tags) {
        logger.debug("Loading tag list.");
        final String fxmlResourcePath = FXML_TAG_LIST;
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = loadFxml(fxmlResourcePath);
            AnchorPane page = loader.load();

            Scene scene = new Scene(page);
            Stage dialogStage = loadDialogStage("List of Tags", primaryStage, scene);

            // Set the tag into the controller.
            TagListController tagListController = loader.getController();
            tagListController.setMainController(this);
            tagListController.setModelManager(modelManager);
            tagListController.setTags(tags);
            tagListController.setStage(dialogStage);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();
        } catch (IOException e) {
            logger.fatal("Error loading tag list view: {}", e);
            showFatalErrorDialogAndShutdown("FXML Load Error", "Cannot load fxml for tag list.",
                                            "IOException when trying to load ", fxmlResourcePath);
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
            FXMLLoader loader = loadFxml(fxmlResourcePath);
            AnchorPane page = loader.load();

            Scene scene = new Scene(page);
            Stage dialogStage = loadDialogStage("Birthday Statistics", primaryStage, scene);
            dialogStage.getIcons().add(getImage(ICON_CALENDAR));

            // Set the persons into the controller.
            BirthdayStatisticsController controller = loader.getController();
            controller.setPersonData(modelManager.getAllViewablePersonsReadOnly());

            dialogStage.show();
        } catch (IOException e) {
            logger.fatal("Error loading birthday statistics view: {}", e);
            showFatalErrorDialogAndShutdown("FXML Load Error", "Cannot load fxml for birthday stats.",
                                            "IOException when trying to load ", fxmlResourcePath);
        }
    }

    public void showActivityHistoryDialog() {
        logger.debug("Loading Activity History.");
        final String fxmlResourcePath = FXML_ACTIVITY_HISTORY;
        try {
            // Load the fxml file and create a new stage for the popup.
            FXMLLoader loader = loadFxml(fxmlResourcePath);
            AnchorPane page = loader.load();

            Scene scene = new Scene(page);
            Stage dialogStage = loadDialogStage("Activity History", primaryStage, scene);

            // Set the persons into the controller.
            ActivityHistoryController controller = loader.getController();
            controller.setConnections(modelManager.getFinishedCommands());
            controller.init();
            dialogStage.show();
        } catch (IOException e) {
            logger.fatal("Error loading activity history view: {}", e);
            showFatalErrorDialogAndShutdown("FXML Load Error", "Cannot load fxml for activity history.",
                    "IOException when trying to load ", fxmlResourcePath);
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
        showAlertDialogAndWait(AlertType.ERROR, "File Op Error", description, content);
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
        AnchorPane pane = (AnchorPane) rootLayout.lookup("#personWebpage");
        pane.getChildren().add(browserManager.getHyperBrowserView());
    }

    @Subscribe
    private void handleResizeAppRequestEvent(ResizeAppRequestEvent event){
        logger.debug("Handling the resize app window request");
        Platform.runLater(this::resizeWindow);
    }

    @Subscribe
    private void handleMinimizeAppRequestEvent(MinimizeAppRequestEvent event){
        logger.debug("Handling the minimize app window request");
        Platform.runLater(this::minimizeWindow);
    }

    /**
     * Toggles between maximized and default size.
     * If not currently at the maximized size, goes to maximised size.
     * If currently maximized, goes to default size.
     */
    private void resizeWindow(){
        if(primaryStage.isMaximized()){
            setDefaultSize();
        } else {
            maximizeWindow();
        }
    }

    protected void setDefaultSize() {

        primaryStage.setHeight(prefs.getGuiSettings().getWindowHeight());
        primaryStage.setWidth(prefs.getGuiSettings().getWindowWidth());
        if (prefs.getGuiSettings().getWindowCoordinates() != null) {
            primaryStage.setX(prefs.getGuiSettings().getWindowCoordinates().getX());
            primaryStage.setY(prefs.getGuiSettings().getWindowCoordinates().getY());
        }
        primaryStage.setMaximized(false);
        primaryStage.setIconified(false);
    }

    private void setMinSize() {
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
    }

    private void minimizeWindow() {
        primaryStage.setIconified(true);
        primaryStage.setMaximized(false);
    }

    private void maximizeWindow() {
        primaryStage.setMaximized(true);
        primaryStage.setIconified(false);
    }

    public void stop() {
        getPrimaryStage().hide();
        releaseResourcesForAppTermination();
    }

    private void showFatalErrorDialogAndShutdown(String title, String headerText, String contentText, String errorLocation) {
        showAlertDialogAndWait(AlertType.ERROR, title, headerText,
                contentText + errorLocation);
        Platform.exit();
        System.exit(1);
    }
}
