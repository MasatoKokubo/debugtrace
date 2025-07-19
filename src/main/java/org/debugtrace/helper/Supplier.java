// Supplier.java
// (C) 2015 Masato Kokubo

package org.debugtrace.helper;

/**
 * Supplier of a T value.
 *
 * @author Masato Kokubo
 */
@FunctionalInterface
public interface Supplier<T> {
    public T get() throws Exception;
}
