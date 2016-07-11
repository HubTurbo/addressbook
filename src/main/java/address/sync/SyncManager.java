package address.sync;


import address.events.*;
import address.main.ComponentManager;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.person.Person;
import address.sync.task.*;
import address.util.AppLogger;
import address.util.Config;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * Syncs data between the local model and remote
 *
 * Once started, attempts to synchronize with the remote periodically
 * Synchronization will be based on the currently-active address book which can be set via setActiveAddressBook
 *
 * Contains event handlers for remote request events. These events should provide a result container
 * for SyncManager to place the result into after finishing the request.
 *
 * All remote requests are run in a separate thread
 */
public class SyncManager extends ComponentManager {
    private static final AppLogger logger = LoggerManager.getLogger(SyncManager.class);

    private final ScheduledExecutorService scheduler;
    private final ExecutorService requestExecutor;
    private final RemoteManager remoteManager;
    private final Config config;

    private Optional<String> activeAddressBook;

    /**
     * Constructor for SyncManager
     *
     * @param config should have updateInterval (milliseconds) and simulateUnreliableNetwork set
     * @param activeAddressBookName name of active addressbook to start with
     */
    public SyncManager(RemoteManager remoteManager, Config config, String activeAddressBookName) {
        this(config, remoteManager, Executors.newCachedThreadPool(),
                Executors.newSingleThreadScheduledExecutor(), activeAddressBookName);
    }

    /**
     * Constructor for SyncManager
     *
     * @param config should have updateInterval (milliseconds) and simulateUnreliableNetwork set
     * @param remoteManager non-null
     * @param executorService non-null
     * @param scheduledExecutorService non-null
     */
    public SyncManager(Config config, RemoteManager remoteManager, ExecutorService executorService,
                       ScheduledExecutorService scheduledExecutorService, String activeAddressBookName) {
        super();
        activeAddressBook = Optional.empty();
        this.config = config;
        this.remoteManager = remoteManager;
        this.requestExecutor = executorService;
        this.scheduler = scheduledExecutorService;
        setActiveAddressBook(activeAddressBookName);
    }

    @Subscribe
    public void handleChangeActiveAddressBookRequestEvent(ChangeActiveAddressBookRequestEvent slce) {
        setActiveAddressBook(slce.getActiveAddressBookName());
    }

    public Optional<String> getActiveAddressBook() {
        return activeAddressBook;
    }

    /**
     * Sets the currently active addressbook for periodic updates
     *
     * @param activeAddressBookName if null, subsequent updates will fail until re-set to a valid address book
     */
    public void setActiveAddressBook(String activeAddressBookName) {
        logger.info("Active addressbook set to {}", activeAddressBookName);
        activeAddressBook = Optional.ofNullable(activeAddressBookName);
    }

    /**
     * Starts synchronizing with the cloud, after every updateInterval milliseconds
     * specified in the config.
     * Synchronization will fail if active address book is not set or is invalid.
     *
     * Raises a SyncStartedEvent at the beginning, and SyncFailedEvent or SyncCompletedEvent at the end of the task
     * Raises a SyncUpdateResourceCompletedEvent after each resource update is finished successfully
     */
    public void start() {
        logger.info("Starting sync manager.");
        Runnable syncTask = new GetUpdatesFromRemoteTask(remoteManager, this::raise, this::getActiveAddressBook);
        logger.debug("Scheduling synchronization task with interval of {} milliseconds", config.getUpdateInterval());
        scheduler.scheduleWithFixedDelay(syncTask, 0, config.getUpdateInterval(), TimeUnit.MILLISECONDS);
    }

    public void stop() {
        logger.info("Stopping sync manager.");
        scheduler.shutdown();
        requestExecutor.shutdown();
    }

    @Subscribe
    public void handleCreatePersonOnRemoteRequestEvent(CreatePersonOnRemoteRequestEvent event) {
        CompletableFuture<ReadOnlyPerson> resultContainer = event.getReturnedPersonContainer();
        RemoteTaskWithResult<ReadOnlyPerson> taskToCall = new CreatePersonOnRemoteTask(remoteManager,
                                                                               event.getAddressBookName(),
                                                                               event.getCreatedPerson());
        callTaskAndHandleResult(taskToCall, resultContainer);
    }

    @Subscribe
    public void handleCreateTagOnRemoteRequestEvent(CreateTagOnRemoteRequestEvent event) {
        CompletableFuture<Tag> resultContainer = event.getReturnedTagContainer();
        RemoteTaskWithResult<Tag> taskToCall = new CreateTagOnRemoteTask(remoteManager, event.getAddressBookName(),
                                                                         event.getCreatedTag());
        callTaskAndHandleResult(taskToCall, resultContainer);
    }

    @Subscribe
    public void handleUpdatePersonOnRemoteRequestEvent(UpdatePersonOnRemoteRequestEvent event) {
        CompletableFuture<ReadOnlyPerson> resultContainer = event.getReturnedPersonContainer();
        RemoteTaskWithResult<ReadOnlyPerson> taskToCall = new UpdatePersonOnRemoteTask(remoteManager,
                                                                               event.getAddressBookName(),
                                                                               event.getPersonId(),
                                                                               event.getUpdatedPerson());
        callTaskAndHandleResult(taskToCall, resultContainer);
    }

    @Subscribe
    public void handleEditTagOnRemoteRequestEvent(EditTagOnRemoteRequestEvent event) {
        CompletableFuture<Tag> resultContainer = event.getReturnedTagContainer();
        RemoteTaskWithResult<Tag> taskToCall = new EditTagOnRemoteTask(remoteManager, event.getAddressBookName(),
                                                                       event.getTagName(), event.getEditedTag());
        callTaskAndHandleResult(taskToCall, resultContainer);
    }

    @Subscribe
    public void handleDeleteTagOnRemoteRequestEvent(DeleteTagOnRemoteRequestEvent event) {
        CompletableFuture<Boolean> resultContainer = event.getResultContainer();
        RemoteTaskWithResult<Boolean> taskToCall = new DeleteTagOnRemoteTask(remoteManager, event.getAddressBookName(),
                                                                             event.getTagName());
        callTaskAndHandleResult(taskToCall, resultContainer);
    }

    @Subscribe
    public void handleDeletePersonOnRemoteRequestEvent(DeletePersonOnRemoteRequestEvent event) {
        CompletableFuture<Boolean> resultContainer = event.getResultContainer();
        RemoteTaskWithResult<Boolean> taskToCall = new DeletePersonOnRemoteTask(remoteManager,
                                                                                event.getAddressBookName(),
                                                                                event.getPersonId());
        callTaskAndHandleResult(taskToCall, resultContainer);
    }

    @Subscribe
    public void handleCreateAddressBookOnRemoteRequestEvent(CreateAddressBookOnRemoteRequestEvent event) {
        CompletableFuture<Boolean> resultContainer = event.getResultContainer();
        RemoteTaskWithResult<Boolean> taskToCall = new CreateAddressBookOnRemoteTask(remoteManager,
                                                                                     event.getAddressBookName());
        callTaskAndHandleResult(taskToCall, resultContainer);
    }

    /**
     * Calls taskToCall and completes the eventResultContainer with the task's result
     *
     * Both the task and the completion of the container run asynchronously using requestExecutor
     *
     * @param taskToCall
     * @param eventResultContainer
     * @param <T>
     */
    private <T> void callTaskAndHandleResult(RemoteTaskWithResult<T> taskToCall,
                                             CompletableFuture<T> eventResultContainer) {
        CompletableFuture<T> taskResultContainer = executeTaskForCompletableFuture(taskToCall, requestExecutor);
        taskResultContainer.whenCompleteAsync(fillResultContainer(eventResultContainer), requestExecutor);
    }

    private <T> BiConsumer<T, Throwable> fillResultContainer(CompletableFuture<T> resultContainer) {
        return (person, ex) -> {
            if (ex != null) {
                resultContainer.completeExceptionally(ex);
                return;
            }
            resultContainer.complete(person);
        };
    }

    /**
     * Executes a callable task and returns a CompletableFuture (instead of a Future)
     *
     * @param callable task to be executed
     * @param executor executor used to execute the callable task
     * @param <T>
     * @return
     */
    private <T> CompletableFuture<T> executeTaskForCompletableFuture(Callable<T> callable, Executor executor) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        logger.debug("Executing callable task: {}", callable);
        executor.execute(() -> {
            try {
                completableFuture.complete(callable.call());
            } catch (Throwable ex) {
                completableFuture.completeExceptionally(ex);
            }
        });
        return completableFuture;
    }
}
