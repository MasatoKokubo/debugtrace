// DebugTrace.java
// (C) 2015 Masato Kokubo

package org.debugtrace;

/**
 * Holds options to pass to print methods.
 *
 * @since 3.7.0
 * @author Masato Kokubo
 */
public class LogOptions {
    /** The minimum value to output the number of elements of array, collection and map */
    public int minimumOutputSize;

    /** The minimum value to output the length of string */
    public int minimumOutputLength;

    /** The limit value of elements for collection and map to output */
    public int collectionLimit;

    /** The limit value of elements for byte array (byte[]) to output */
    public int byteArrayLimit;

    /** The limit value of characters for string to output */
    public int stringLimit;

    /** The limit value for reflection nesting */
    public int reflectionNestLimit;

    /** Output the number of elements of array, collection and map */
    public static final LogOptions outputSize = new LogOptions();
    static {
        outputSize.minimumOutputSize = 0;
    }

    /** Output the length of string */
    public static final LogOptions outputLength = new LogOptions();
    static {
        outputLength.minimumOutputLength = 0;
    }

    /**
     * Constructs a LogOptions.
     */
    public LogOptions() {
        minimumOutputSize   = DebugTrace.minimumOutputSize;
        minimumOutputLength = DebugTrace.minimumOutputLength;
        collectionLimit     = DebugTrace.collectionLimit;
        byteArrayLimit      = DebugTrace.byteArrayLimit;
        stringLimit         = DebugTrace.stringLimit;
        reflectionNestLimit = DebugTrace.reflectionNestLimit;
    }

    /**
     * Normalizes each field of this.
     */
    protected void normalize() {
        if (minimumOutputSize < 0)
            minimumOutputSize = 0;
        if (minimumOutputLength < 0)
            minimumOutputLength = 0;
        if (collectionLimit < 0)
            collectionLimit = 0;
        if (byteArrayLimit < 0)
            byteArrayLimit = 0;
        if (stringLimit < 0)
            stringLimit = 0;
        if (reflectionNestLimit < 1)
            reflectionNestLimit = 1;
    }
}
