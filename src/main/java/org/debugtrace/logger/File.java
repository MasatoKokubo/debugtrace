// FileLogger.java
// (C) 2015 Masato Kokubo

package org.debugtrace.logger;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import org.debugtrace.DebugTrace;
import org.debugtrace.LineSeparator;

/**
 * A logger that outputs directly to a file.
 *
 * @since 3.4.0
 * @author Masato Kokubo
 */
public class File implements Logger {
    private static final Object lock = new Object();
    private java.io.File file;
    private Charset charset;
    private boolean writeErrorReported;
    private LineSeparator lineSeparator;

    /**
     * Constructs a org.debugtrace.logger.File.
     * @param file the output destination java.io.File
     * @param charset the charset
     * @param lineSeparator the line separator
     * @param append Append output to the existing log file if true, initialize and output otherwise
     */
    public File(java.io.File file, Charset charset, LineSeparator lineSeparator, boolean append) {
        this.file = file;
        this.charset = charset;
        this.lineSeparator = lineSeparator;
        try {
            new FileWriter(file, append).close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(String message) {
        synchronized (lock) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, true), charset)) {
                writer.write(DebugTrace.appendTimestamp(message) + lineSeparator);
            } catch (IOException e) {
                if (!writeErrorReported) {
                    System.err.println(e.toString());
                    writeErrorReported = true;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getName() +
            " (character set: " + charset +
            ", line separator: " + lineSeparator.visible() +
            ", file: " + file.getPath() + ")";
    }
}
