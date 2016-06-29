package address.sync.task;

import address.sync.RemoteManager;

import java.util.concurrent.Callable;

public abstract class RemoteTaskWithResult<T> implements Callable<T> {
    protected final RemoteManager remoteManager;

    protected RemoteTaskWithResult(RemoteManager remoteManager) {
        assert remoteManager != null : "Remote task cannot be null!";
        this.remoteManager = remoteManager;
    }
}
