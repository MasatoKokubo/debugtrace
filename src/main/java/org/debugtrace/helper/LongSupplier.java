// LongSupplier.java
// (C) 2015 Masato Kokubo

package org.debugtrace.helper;

/**
 * Supplier of an long value.
 *
 * @author Masato Kokubo
 */
@FunctionalInterface
public interface LongSupplier {
    public long getAsLong() throws Exception;
}
