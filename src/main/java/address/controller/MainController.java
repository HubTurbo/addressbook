package address.controller;

import address.MainApp;
import address.browser.BrowserManager;
import address.events.*;
import address.exceptions.DuplicateTagException;
import address.events.SingleTargetCommandResultEvent;
import address.model.UserPrefs;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.ModelManager;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import address.util.*;
import address.util.collections.UnmodifiableObservableList;
import com.google.common.eventbus.Subscribe;
import commons.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
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
    private static final String FXML_TAG_LIST = "/view/TagList.fxml";
    private static final String FXML_BIRTHDAY_STATISTICS = "/view/BirthdayStatistics.fxml";
    private static final String FXML_TAG_SELECTION_EDIT_DIALOG = "/view/TagSelectionEditDialog.fxml";
    private static final String ICON_APPLICATION = "/images/address_book_32.png";
    private static final String ICON_EDIT = "/images/edit.png";
    private static final String ICON_CALENDAR = "/images/calendar.png";
    private static final String ICON_INFO = "/images/info_icon.png";
    public static final int MIN_HEIGHT = 600;
    public static final int MIN_WIDTH = 450;

    private MainApp mainApp; //TODO: remove this back link to higher level class

    //Links to internal non-UI components
    private ModelManager modelManager;

    //Objects containing parameters for controlling App behavior
    private Config config;
    private UserPrefs prefs;

    //Parts of the main UI
    private BrowserManager browserManager;
    private Stage primaryStage;
    private RootLayout rootLayout;
    private PersonListPanel personListPanel;

    //TODO: replace these with higher level Ui Parts (similar to PersonListPanel)
    private StatusBarHeaderController statusBarHeaderController;
    private StatusBarFooterController statusBarFooterController;

    //TODO: See if these can be pushed down to respective UI parts
    private UnmodifiableObservableList<ReadOnlyViewablePerson> personList;
    private final ObservableList<SingleTargetCommandResultEvent> finishedCommandResults;

    {
        finishedCommandResults = FXCollections.observableArrayList();
    }

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

        try {
            setStageTitle(config.getAppTitle());
            setStageIcon(getImage(ICON_APPLICATION));
            setStageMinSize();
            setStageDefaultSize();

            rootLayout = createRootLayout();
            this.primaryStage.show();

            personListPanel = createPersonListPanel();

            this.browserManager.start();
            showPersonWebPage();

            showFooterStatusBar();
            showHeaderStatusBar();
        } catch (Throwable e) {
            e.printStackTrace();
            showFatalErrorDialogAndShutdown("Fatal error during initializing", e);
        }

    }

    private void setStageTitle(String title) {
        primaryStage.setTitle(title);
    }

    private void setStageIcon(Image appIcon) {
        primaryStage.getIcons().add(appIcon);
    }

    /**
     * Initializes the root layout and tries to load the last opened
     * person file.
     */
    public RootLayout createRootLayout() {
        logger.debug("Initializing root layout.");
        RootLayout rootLayout = new RootLayout(primaryStage, mainApp, this, modelManager);
        rootLayout.setKeyEventHandler(this::handleKeyEvent);
        rootLayout.setAccelerators();
        return rootLayout;
    }

    private void handleKeyEvent(KeyEvent keyEvent) {
        raisePotentialEvent(new KeyBindingEvent(keyEvent));
    }

    /**
     * Shows the person list panel inside the root layout.
     */
    public PersonListPanel createPersonListPanel() {
        logger.debug("Loading person list panel.");
        return new PersonListPanel(rootLayout.getPersonListSlot(), this, modelManager, personList);
    }

    public StatusBarHeaderController getStatusBarHeaderController() {
        return statusBarHeaderController;
    }

    private void showHeaderStatusBar() {
        statusBarHeaderController = new StatusBarHeaderController(this, this.finishedCommandResults);
        AnchorPane sbPlaceHolder = rootLayout.getAnchorPane("#headerStatusbarPlaceholder");

        assert sbPlaceHolder != null : "headerStatusbarPlaceHolder node not found in rootLayout";

        FxViewUtil.applyAnchorBoundaryParameters(statusBarHeaderController.getHeaderStatusBarView(), 0.0, 0.0, 0.0, 0.0);
        sbPlaceHolder.getChildren().add(statusBarHeaderController.getHeaderStatusBarView());
    }

    private void showFooterStatusBar() {
        logger.debug("Loading footer status bar.");
        final String fxmlResourcePath = FXML_STATUS_BAR_FOOTER;
        FXMLLoader loader = loadFxml(fxmlResourcePath);
        GridPane gridPane = (GridPane) loadLoader(loader, "Error Loading footer status bar");
        gridPane.getStyleClass().add("grid-pane");
        statusBarFooterController = loader.getController();
        statusBarFooterController.init(config.getUpdateInterval(), config.getAddressBookName());
        AnchorPane placeHolder = rootLayout.getAnchorPane("#footerStatusbarPlaceholder");
        FxViewUtil.applyAnchorBoundaryParameters(gridPane, 0.0, 0.0, 0.0, 0.0);
        placeHolder.getChildren().add(gridPane);
    }

    //TODO: to be removed
    private Node loadLoader(FXMLLoader loader, String errorMsg ) {
        try {
            return loader.load();
        } catch (IOException e) {
            logger.fatal(errorMsg + ": {}", e);
            showFatalErrorDialogAndShutdown("FXML Load Error", errorMsg,
                    "IOException when trying to load ", loader.getLocation().toExternalForm());
            return null;
        }
    }

    //TODO: to be removed
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
            // Load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = loadFxml(fxmlResourcePath);
        AnchorPane page = (AnchorPane) loadLoader(loader, "Error loading person edit dialog");

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
    }

    public Optional<List<Tag>> getPersonsTagsInput(List<ReadOnlyViewablePerson> persons) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource(FXML_TAG_SELECTION_EDIT_DIALOG));
        AnchorPane pane = (AnchorPane) loadLoader(loader, "Error launching tag selection dialog");


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

    //TODO: to be removed
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
            // Load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = loadFxml(fxmlResourcePath);
        AnchorPane page = (AnchorPane) loadLoader(loader, "Error loading tag edit dialog");

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
    }

    public void showTagList(ObservableList<Tag> tags) {
        logger.debug("Loading tag list.");
        final String fxmlResourcePath = FXML_TAG_LIST;
        // Load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = loadFxml(fxmlResourcePath);
        AnchorPane page = (AnchorPane) loadLoader(loader, "Error loading tag list view");

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
    }

    /**
     * Opens a dialog to show birthday statistics.
     */
    public void showBirthdayStatistics() {
        logger.debug("Loading birthday statistics.");
        final String fxmlResourcePath = FXML_BIRTHDAY_STATISTICS;
        // Load the fxml file and create a new stage for the popup.
        FXMLLoader loader = loadFxml(fxmlResourcePath);
        AnchorPane page = (AnchorPane) loadLoader(loader, "Error loading birthday statistics view");

        Scene scene = new Scene(page);
        Stage dialogStage = loadDialogStage("Birthday Statistics", primaryStage, scene);
        dialogStage.getIcons().add(getImage(ICON_CALENDAR));

        // Set the persons into the controller.
        BirthdayStatisticsController controller = loader.getController();
        controller.setPersonData(modelManager.getAllViewablePersonsReadOnly());

        dialogStage.show();
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
            dialogStage.getIcons().add(getImage(ICON_INFO));
            // Set the persons into the controller.
            ActivityHistoryController controller = loader.getController();
            controller.setConnections(finishedCommandResults);
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

    //TODO: to relocate
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
        AnchorPane pane = rootLayout.getAnchorPane("#personWebpage");
        disableKeyboardShortcutOnNode(pane);
        pane.getChildren().add(browserManager.getHyperBrowserView());
    }

    private void disableKeyboardShortcutOnNode(Node pane) {
        pane.addEventHandler(EventType.ROOT, event -> event.consume());
    }

    @Subscribe
    private void handleResizeAppRequestEvent(ResizeAppRequestEvent event){
        logger.debug("Handling the resize app window request");
        Platform.runLater(this::handleResizeRequest);
    }

    @Subscribe
    private void handleMinimizeAppRequestEvent(MinimizeAppRequestEvent event){
        logger.debug("Handling the minimize app window request");
        Platform.runLater(this::minimizeWindow);
    }

    @Subscribe
    private void handleSingleTargetCommandResultEvent(SingleTargetCommandResultEvent evt) {
        PlatformExecUtil.runAndWait(() -> finishedCommandResults.add(evt));
    }

    protected void setStageDefaultSize() {
        primaryStage.setHeight(prefs.getGuiSettings().getWindowHeight());
        primaryStage.setWidth(prefs.getGuiSettings().getWindowWidth());
        if (prefs.getGuiSettings().getWindowCoordinates() != null) {
            primaryStage.setX(prefs.getGuiSettings().getWindowCoordinates().getX());
            primaryStage.setY(prefs.getGuiSettings().getWindowCoordinates().getY());
        }
    }

    private void setStageMinSize() {
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
    }

    private void minimizeWindow() {
        primaryStage.setIconified(true);
        primaryStage.setMaximized(false);
    }

    private void handleResizeRequest() {
        logger.info("Handling resize request.");
        if (primaryStage.isIconified()) {
            logger.debug("Cannot resize as window is iconified, attempting to show window instead.");
            primaryStage.setIconified(false);
        } else {
            resizeWindow();
        }
    }

    private void resizeWindow() {
        logger.info("Resizing window");
        // specially handle since stage operations on Mac seem to not be working as intended
        if (commons.OsDetector.isOnMac()) {
            // refresh stage so that resizing effects (apart from the first resize after iconify-ing) are applied
            // however, this will cause minor flinching in window visibility
            primaryStage.hide(); // hide has to be called before setMaximized,
                                 // or first resize attempt after iconify-ing will resize twice
            primaryStage.show();

            // on Mac, setMaximized seems to work like "setResize"
            // isMaximized also does not seem to return the correct value
            primaryStage.setMaximized(true);
        } else {
            primaryStage.setMaximized(!primaryStage.isMaximized());
        }

        logger.debug("Stage width: {}", primaryStage.getWidth());
        logger.debug("Stage height: {}", primaryStage.getHeight());
    }

    public void stop() {
        getPrimaryStage().hide();
        releaseResourcesForAppTermination();
    }

    private void showFatalErrorDialogAndShutdown(String title, String headerText, String contentText, String errorLocation) {
        showAlertDialogAndWait(AlertType.ERROR, title, headerText, contentText + errorLocation);
        Platform.exit();
        System.exit(1);
    }

    private void showFatalErrorDialogAndShutdown(String title, Throwable e) {
        //TODO: Do a more detailed error reporting e.g. stack trace
        logger.fatal(title + " " + e.getMessage());
        showAlertDialogAndWait(AlertType.ERROR, title, e.getMessage(), e.toString());
        Platform.exit();
        System.exit(1);
    }
}
