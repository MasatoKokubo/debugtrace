// ListUtils.java
// (C) 2015 Masato Kokubo

package org.debugtrace.helper;

/**
 * Supplier of a double value.
 *
 * @author Masato Kokubo
 */
@FunctionalInterface
public interface DoubleSupplier {
    public double getAsDouble() throws Exception;
}
