package address.sync.cloud;

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

public class RemoteResponse {
    private static final AppLogger logger = LoggerManager.getLogger(RemoteResponse.class);

    private int responseCode;
    private InputStream body;
    private HashMap<String, String> headers;
    private int nextPageNo;
    private int previousPageNo;
    private int firstPageNo;
    private int lastPageNo;

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

    private HashMap<String, String> getHeaders(CloudRateLimitStatus cloudRateLimitStatus) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("X-RateLimit-Limit", String.valueOf(cloudRateLimitStatus.getQuotaLimit()));
        headers.put("X-RateLimit-Remaining", String.valueOf(cloudRateLimitStatus.getQuotaRemaining()));
        headers.put("X-RateLimit-Reset", String.valueOf(cloudRateLimitStatus.getQuotaReset()));
        return headers;
    }

    public RemoteResponse(int responseCode, Object body, CloudRateLimitStatus cloudRateLimitStatus) {
        deductApiUnlessNotModified(responseCode, cloudRateLimitStatus);
        this.responseCode = responseCode;
        this.headers = getHeaders(cloudRateLimitStatus);
        if (body != null) {
            this.body = convertToInputStream(body);
            addETagToHeader(getHeaders(cloudRateLimitStatus), getETag(convertToInputStream(body)));
        }
    }

    public RemoteResponse(int responseCode, CloudRateLimitStatus cloudRateLimitStatus) {
        this.responseCode = responseCode;
        this.headers = getHeaders(cloudRateLimitStatus);
        this.body = convertToInputStream(getHeaders(cloudRateLimitStatus));
    }

    private void deductApiUnlessNotModified(int responseCode, CloudRateLimitStatus cloudRateLimitStatus) {
        if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) return;
        cloudRateLimitStatus.useQuota(1);
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

    private static ByteArrayInputStream convertToInputStream(Object object) {
        try {
            return new ByteArrayInputStream(JsonUtil.toJsonString(object).getBytes());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
