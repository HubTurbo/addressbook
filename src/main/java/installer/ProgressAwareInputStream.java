package installer;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Adapted from http://www.java2s.com/Code/Android/File/InputStreamthatnotifieslistenersofitsprogress.htm
 *
 * Input stream that can have a progress listener
 */
public class ProgressAwareInputStream extends InputStream {
    private final InputStream wrappedInputStream;
    private long size;
    private long counter;
    private Consumer<Double> listener;

    public ProgressAwareInputStream(InputStream in, long size) {
        wrappedInputStream = in;
        this.size = size;
    }

    public void setOnProgressListener(Consumer<Double> listener) { this.listener = listener; }

    @Override
    public int read() throws IOException {
        counter += 1;
        check();
        return wrappedInputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int retVal = wrappedInputStream.read(b);
        counter += retVal;
        check();
        return retVal;
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        int retVal = wrappedInputStream.read(b, offset, length);
        counter += retVal;
        check();
        return retVal;
    }

    private void check() {
        double percent = counter * 1.0 / size;

        if (listener != null) {
            listener.accept(percent);
        }
    }

    @Override
    public void close() throws IOException { wrappedInputStream.close(); }
    @Override
    public int available() throws IOException { return wrappedInputStream.available(); }
    @Override
    public void mark(int readLimit) { wrappedInputStream.mark(readLimit); }
    @Override
    public synchronized void reset() throws IOException { wrappedInputStream.reset(); }
    @Override
    public boolean markSupported() { return wrappedInputStream.markSupported(); }
    @Override
    public long skip(long n) throws IOException { return wrappedInputStream.skip(n); }
}
