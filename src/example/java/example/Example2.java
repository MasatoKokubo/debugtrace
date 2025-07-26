// Example2.java
// (C) 2015 Masato Kokubo
package example;

import java.util.HashMap;
import java.util.Map;

import org.debugtrace.DebugTrace;

public class Example2 {
    private static final Map<Long, Long> fibonacciMap = new HashMap<>();
    static {
        fibonacciMap.put(0L, 0L);
        fibonacciMap.put(1L, 1L);
    }

    public static void main(String[] args) {
        try {DebugTrace.enter(); // TODO: Debug
        try {
            if (args.length <= 0)
                throw new IllegalArgumentException("args.length = " + args.length);
            long n = Long.parseLong(args[0]);
            long fibonacci = fibonacci(n);
            System.out.println("fibonacci(" + n + ") = " + fibonacci);
        } finally {
            DebugTrace.print("fibonacciMap", fibonacciMap); // TODO: Debug
        }
        } catch (Exception e) {DebugTrace.print("e", e); throw e; // TODO: Debug
        } finally {DebugTrace.leave();} // TODO: Debug
    }

    public static long fibonacci(long n) {
        try {DebugTrace.enter(); // TODO: Debug
        if (n < 0)
            throw new IllegalArgumentException("n (" + n + ") is negative.");
        long fibonacci = 0;
        if (fibonacciMap.containsKey(n)) {
            fibonacci = fibonacciMap.get(n);
            DebugTrace.print("mapped fibonacci(" + n + ")", fibonacci); // TODO: Debug
        } else {
            fibonacci = fibonacci(n - 2) + fibonacci(n - 1);
            DebugTrace.print("fibonacci(" + n + ")", fibonacci); // TODO: Debug
            if (fibonacci < 0)
               throw new RuntimeException("Overflow occurred in fibonacci(" + n + ") calculation.");
            fibonacciMap.put(n, fibonacci);
        }
        return fibonacci;
        } finally {DebugTrace.leave();} // TODO: Debug
    }
}
