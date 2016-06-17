package address.sync;

import address.util.AppLogger;
import address.util.JsonUtil;
import address.util.LoggerManager;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;

public class RawCloudResponse {
    private static final AppLogger logger = LoggerManager.getLogger(RawCloudResponse.class);
    int responseCode;
    InputStream body;
    HashMap<String, String> headers;
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

    private void addETagToHeader(HashMap<String, String> header, String eTag) {
        header.put("ETag", eTag);
    }

    public RawCloudResponse(int responseCode, Object body, HashMap<String, String> header) {
        this.responseCode = responseCode;
        if (body != null) {
            this.body = convertToInputStream(body);
            addETagToHeader(header, getETag(convertToInputStream(body)));
        }
        this.headers = header;
    }

    public RawCloudResponse(int responseCode) {
        assert responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR : "RawCloudResponse constructor misused";
        this.responseCode = responseCode;
        this.headers = new HashMap<>();
    }


    public int getResponseCode() {
        return responseCode;
    }

    public InputStream getBody() {
        return body;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * Calculates the hash of the input stream if it has content
     *
     * The input stream will be digested. Caller should clone or
     * duplicate the stream before calling this method.
     *
     * @param bodyStream
     * @return
     */
    public static String getETag(InputStream bodyStream) {
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
            logger.warn("Error generating ETag for response");
            return null;
        }
    }

    public static ByteArrayInputStream convertToInputStream(Object object) {
        try {
            return new ByteArrayInputStream(JsonUtil.toJsonString(object).getBytes());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
