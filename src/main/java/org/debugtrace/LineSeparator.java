// LineSeparator.java
// (C) 2015 Masato Kokubo

package org.debugtrace;

/**
 * Line Separator
 * 
 * @since 3.8.0
 * @author Masato Kokubo
 */
public enum LineSeparator {
    LF("lf", "\n", "\\n"),
    CR("cr", "\r", "\\r"),
    CRLF("crlf", "\r\n", "\\r\\n");

    private final String name;
    private final String string;
    private final String visible;

    private LineSeparator(String name, String string, String visible) {
        this.name = name;
        this.string = string;
        this.visible = visible;
    };

    public static LineSeparator parse(String string) {
        if (string.equals(LF.string) || string.equalsIgnoreCase(LF.name))
            return LineSeparator.LF;

        if (string.equals(CR.string) || string.equalsIgnoreCase(CR.name))
            return LineSeparator.CR;

        if (string.equals(CRLF.string) || string.equalsIgnoreCase(CRLF.name))
            return LineSeparator.CRLF;

        throw new IllegalArgumentException(string + "' is unknown.");
    }

    public String visible() {
        return visible;
    }

    @Override
    public String toString() {
        return string;
    }
}
