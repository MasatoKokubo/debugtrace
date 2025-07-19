// BooleanSupplier.java
// (C) 2015 Masato Kokubo

package org.debugtrace.helper;

/**
 * Supplier of a boolean value.
 *
 * @author Masato Kokubo
 */
@FunctionalInterface
public interface BooleanSupplier {
    public boolean getAsBoolean() throws Exception;
}
