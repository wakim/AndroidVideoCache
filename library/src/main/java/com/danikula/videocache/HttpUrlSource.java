package com.danikula.videocache;

import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.danikula.videocache.ProxyCacheUtils.DEFAULT_BUFFER_SIZE;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

/**
 * {@link Source} that uses http resource as source for {@link ProxyCache}.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class HttpUrlSource implements Source {

    private static final int MAX_REDIRECTS = 5;
    public final String url;
    public static int networkTimeout = 5000;
    private HttpURLConnection connection;
    private InputStream inputStream;
    private volatile int length = Integer.MIN_VALUE;
    private volatile String mime;

    public HttpUrlSource(String url) {
        this(url, ProxyCacheUtils.getSupposablyMime(url));
    }

    public HttpUrlSource(String url, String mime) {
        this.url = Preconditions.checkNotNull(url);
        this.mime = mime;
    }

    public HttpUrlSource(HttpUrlSource source) {
        this.url = source.url;
        this.mime = source.mime;
        this.length = source.length;
    }

    @Override
    public synchronized int length() throws ProxyCacheException {
        if (length == Integer.MIN_VALUE) {
            fetchContentInfo();
        }
        return length;
    }

    @Override
    public void open(int offset) throws ProxyCacheException {
        try {
            connection = openConnection(offset);
            mime = connection.getContentType();
            inputStream = new BufferedInputStream(connection.getInputStream(), DEFAULT_BUFFER_SIZE);
            length = readSourceAvailableBytes(connection, offset, connection.getResponseCode());
        } catch (IOException e) {
            throw new ProxyCacheException("Error opening connection for " + url + " with offset " + offset, e);
        }
    }

    private int readSourceAvailableBytes(HttpURLConnection connection, int offset, int responseCode) throws IOException {
        int contentLength = connection.getContentLength();
        return responseCode == HTTP_OK ? contentLength
                : responseCode == HTTP_PARTIAL ? contentLength + offset : length;
    }

    @Override
    public void close() throws ProxyCacheException {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (NullPointerException | ArrayIndexOutOfBoundsException | IllegalArgumentException | IllegalStateException e) {
                // https://github.com/danikula/AndroidVideoCache/issues/32
                // https://github.com/danikula/AndroidVideoCache/issues/29
                // https://github.com/danikula/AndroidVideoCache/issues/50
                throw new ProxyCacheException("Error disconnecting HttpUrlConnection", e);
            }
        }
    }

    @Override
    public int read(byte[] buffer) throws ProxyCacheException {
        if (inputStream == null) {
            throw new ProxyCacheException("Error reading data from " + url + ": connection is absent!");
        }
        try {
            return inputStream.read(buffer, 0, buffer.length);
        } catch (InterruptedIOException e) {
            throw new InterruptedProxyCacheException("Reading source " + url + " is interrupted", e);
        } catch (IOException e) {
            throw new ProxyCacheException("Error reading data from " + url, e);
        }
    }

    private void fetchContentInfo() throws ProxyCacheException {
        Logger.d("Read content info from " + url);
        try {
            HttpURLConnection urlConnection = openConnection(0, true);

            length = urlConnection.getContentLength();
            mime = urlConnection.getContentType();
            Logger.i("Content info for `" + url + "`: mime: " + mime + ", content-length: " + length);
        } catch (IOException e) {
            Logger.e("Error fetching info from " + url, e);
        }
    }
    private HttpURLConnection openConnection(int offset) throws IOException, ProxyCacheException {
        return openConnection(offset, false);
    }

    private HttpURLConnection openConnection(int offset, boolean headerOnly) throws IOException, ProxyCacheException {
        HttpURLConnection connection;
        boolean redirected;
        int redirectCount = 0;
        String url = this.url;
        do {
            Logger.d("Open connection " + (offset > 0 ? " with offset " + offset : "") + " to " + url);
            connection = (HttpURLConnection) new URL(url).openConnection();

            if (headerOnly) {
                connection.setRequestMethod("HEAD");
            }

            if (offset > 0) {
                connection.setRequestProperty("Range", "bytes=" + offset + "-");
            }

            connection.setConnectTimeout(networkTimeout);
            connection.setReadTimeout(networkTimeout);

            int code = connection.getResponseCode();
            redirected = code == HTTP_MOVED_PERM || code == HTTP_MOVED_TEMP || code == HTTP_SEE_OTHER;
            if (redirected) {
                url = connection.getHeaderField("Location");
                redirectCount++;
                connection.disconnect();
            }
            if (redirectCount > MAX_REDIRECTS) {
                throw new ProxyCacheException("Too many redirects: " + redirectCount);
            }
        } while (redirected);
        return connection;
    }

    public synchronized String getMime() throws ProxyCacheException {
        if (TextUtils.isEmpty(mime)) {
            fetchContentInfo();
        }
        return mime;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "HttpUrlSource{url='" + url + "}";
    }
}
