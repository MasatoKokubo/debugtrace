// LogBuffer.java
// (C) 2015 Masato Kokubo

package org.debugtrace;

import java.util.ArrayList;
import java.util.List;

import org.debugtrace.helper.Tuple;

/**
 * Buffers logs.
 *
 * @since 3.0.0
 * @author Masato Kokubo
 */
class LogBuffer {
    private int nestLevel = 0;
    private int appendNestLevel = 0;

    // tuples of data indentation level and log string
    private final List<Tuple._2<Integer, String>> lines = new ArrayList<>();

    // buffer for a line of logs
    private final StringBuilder lastLine = new StringBuilder();

    /**
     * Breaks the current line.
     */
    public void lineFeed() {
        while (lastLine.length() > 0 && lastLine.charAt(lastLine.length() - 1) == ' ')
            lastLine.setLength(lastLine.length() - 1);
        lines.add(Tuple.of(nestLevel + appendNestLevel, lastLine.toString()));
        appendNestLevel = 0;
        lastLine.setLength(0);
    }

    /**
     * Ups the nest level.
     */
    public void upNest() {
        ++nestLevel;
    }

    /**
     * Downs the nest level.
     */
    public void downNest() {
        --nestLevel;
    }

    /**
     * Appends a string representation of the value.
     *
     * @param value the value to append
     * @param nestLevel the nest level of the value
     * @param noBreak if true, does not break even if the maximum width is exceeded
     * @return this object
     */
    public LogBuffer append(Object value, int nestLevel, boolean noBreak) {
        String str = null;
        try {
            str = value.toString();
        }
        catch (Exception e) {
            str = e.toString();
        }
        if (!noBreak && length() > 0 && length() + str.length() > DebugTrace.maximumDataOutputWidth)
            lineFeed();
        appendNestLevel = nestLevel;
        lastLine.append(str);
        return this;
    }

    /**
     * Appends a string representation of the value.
     *
     * @param value the value to append
     * @param nestLevel >the nest level of the value
     * @return this object
     */
    public LogBuffer append(Object value, int nestLevel) {
        return append(value, nestLevel, false);
    }

    /**
     * Appends a string representation of the value.
     *
     * @param value the value to append
     * @return this object
     */
    public LogBuffer append(Object value) {
        return append(value, 0, false);
    }

    /**
     * Appends a string representation of the value.
     * Does not break even if the maximum width is exceeded.
     *
     * @param value the value to append
     * @return this object
     */
    public LogBuffer noBreakAppend(Object value) {
        return append(value, 0, true);
    }

    /**
     * Appends lines of another LogBuffer.
     *
     * @param separator the separator string to append if not null
     * @param buff another LogBuffer
     * @return this object
     */
    public LogBuffer append(String separator, LogBuffer buff) {
        if (separator != null)
            append(separator, 0, true);
        int index = 0;
        for (Tuple._2<Integer, String> line : buff.lines()) {
            if (index > 0)
                lineFeed();
            append(line.value2(), line.value1(), index == 0 && separator != null);
            ++index;
        }
        return this;
    }

    /**
     * Returns log length of the last line.
     *
     * @return log length of the last line
     */
    public int length() {
        return lastLine.length();
    }

    /**
     * Returns true if multiple line.
     *
     * @return true if multiple line, false otherwise
     */
    public boolean isMultiLines() {
        return lines.size() > 1 || lines.size() == 1 && length() > 0;
    }

    /**
     * Returns tuples of data indentation level and log string.
     *
     * @return tuples of data indentation level and log string
     */
    public List<Tuple._2<Integer, String>> lines() {
        List<Tuple._2<Integer, String>> lines = new ArrayList<>(this.lines);
        if (lastLine.length() > 0)
            lines.add(Tuple.of(nestLevel, lastLine.toString()));
        return lines;
    }
}
