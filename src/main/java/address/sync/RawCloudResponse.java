package address.sync;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;

public class RawCloudResponse {
    int responseCode;
    InputStream body;
    InputStream headers;
    int nextPageNo = -1;
    int previousPageNo = -1;
    int firstPageNo = -1;
    int lastPageNo = -1;

    public int getNextPageNo() {
        return nextPageNo;
    }

    public void setNextPageNo(int nextPageNo) {
        this.nextPageNo = nextPageNo;
    }

    public int getPreviousPageNo() {
        return previousPageNo;
    }

    public void setPreviousPageNo(int previousPageNo) {
        this.previousPageNo = previousPageNo;
    }

    public int getFirstPageNo() {
        return firstPageNo;
    }

    public void setFirstPageNo(int firstPageNo) {
        this.firstPageNo = firstPageNo;
    }

    public int getLastPageNo() {
        return lastPageNo;
    }

    public void setLastPageNo(int lastPageNo) {
        this.lastPageNo = lastPageNo;
    }

    RawCloudResponse(int responseCode, InputStream body, HashMap<String, String> headers) {
        this.responseCode = responseCode;
        this.body = body;
        this.headers = CloudSimulator.convertToInputStream(headers);
    }

    RawCloudResponse(int responseCode) {
        assert responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR : "RawCloudResponse constructor misused";
        this.responseCode = responseCode;
        this.headers = CloudSimulator.convertToInputStream(getEmptyHeader());
    }

    private HashMap<String, String> getEmptyHeader() {
        HashMap<String, String> headers = new HashMap<>();
        return headers;
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

    static String getETag(InputStream bodyStream, boolean isEmptyBody) {
        if (isEmptyBody) return null;
        try {
            // Adapted from http://www.javacreed.com/how-to-compute-hash-code-of-streams/
            DigestInputStream digestInputStream = new DigestInputStream(new BufferedInputStream(bodyStream),
                                                                        MessageDigest.getInstance("SHA-1"));

            while (digestInputStream.read() != -1) {}

            final byte[] digest = digestInputStream.getMessageDigest().digest();

            Formatter formatter = new Formatter();
            for (final byte b : digest) {
                formatter.format("%02x", b);
            }

            return formatter.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            System.out.println("Error generating ETag for response");
            return null;
        }
    }
}
