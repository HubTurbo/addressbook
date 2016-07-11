package address.sync.cloud;

import address.sync.cloud.request.Request;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudRequestQueue {
    private CloudFileHandler cloudFileHandler;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public CloudRequestQueue(CloudFileHandler cloudFileHandler) {
        this.cloudFileHandler = cloudFileHandler;
    }

    public void submitRequest(Request request) {
        request.setFileHandler(cloudFileHandler);
        executor.submit(request);
    }
}
