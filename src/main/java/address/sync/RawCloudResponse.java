package address.sync;

import java.io.InputStream;
import java.util.HashMap;

public class RawCloudResponse {
    int responseCode;
    InputStream body;
    InputStream headers;

    RawCloudResponse(int responseCode, InputStream body, InputStream headers) {
        this.responseCode = responseCode;
        this.body = body;
        this.headers = headers;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public InputStream getBody() {
        return body;
    }

    public InputStream getHeaders() {
        return headers;
    }
}
