// IntSupplier.java
// (C) 2015 Masato Kokubo

package org.debugtrace.helper;

/**
 * Supplier of an int value.
 *
 * @author Masato Kokubo
 */
@FunctionalInterface
public interface IntSupplier {
    public int getAsInt() throws Exception;
}
