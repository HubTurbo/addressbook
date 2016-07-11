package address.sync.cloud.request;

import address.sync.cloud.CloudFileHandler;

/**
 * Base class for requests
 */
public abstract class Request implements Runnable {
    protected CloudFileHandler cloudFileHandler;

    public void setFileHandler(CloudFileHandler cloudFileHandler) {
        this.cloudFileHandler = cloudFileHandler;
    }
}
