# DebugTrace-java

[Japanese](README_ja.md)

*DebugTrace-java* is a library that outputs trace logs when debugging Java programs.<br>
By embedding `DebugTrace.enter()` and `DebugTrace.leave()` at the start and end of methods,
you can output the execution status of the Java program under development to the log.

|DebugTrace-java version|Java version to support
|:----------------------|:----------------------
|DebugTrace-java 4.x.x  |Java 17 and later
|DebugTrace-java 3.x.x  |Java 8 and later

### 1. Features

* Automatically outputs invoker's class name, method name, source file and line number.
* Automatically indents the log with nesting methods and objects.
* Automatically output logs when changing threads.
* Uses reflection to output the contents of classes that do not implement the `toString` method.
* You can customize the output content in `DebugTrace.properties`.
* There are no dependent libraries at run time. (Required if you use the following logging library)
* You can use the following logging library.
  * Console (stdout and stderr)
  * https://docs.oracle.com/javase/8/docs/api/java/util/logging/Logger.html[JDK Logger]
  * http://logging.apache.org/log4j/1.2/[Log4j]
  * https://logging.apache.org/log4j/2.x/[Log4j2]
  * http://www.slf4j.org/[SLF4J]
  * Direct file output

### 2. How to use

Insert the following for the debuggee and related methods.

* `DebugTrace.enter();`
* `DebugTrace.leave();`
* `DebugTrace.print("value", value);`

#### (1) If the method does not throw an exception and does not return in the middle.

```java
public void foo() {
    DebugTrace.enter(); // TODO: Debug
    ...
    DebugTrace.print("value", value); // TODO: Debug
    ...
    DebugTrace.leave(); // TODO: Debug
}
```

#### (2) If the method does not throw an exception, but there are returns in the middle.

```java
public void foo() {
    try {DebugTrace.enter(); // TODO: Debug
    ...
    DebugTrace.print("value", value); // TODO: Debug
    ...
    if (...)
        return;
    ...
    } finally {DebugTrace.leave();} // TODO: Debug
}
```

#### (3) If the method throws an exception.

```java
public void foo() throws Exception {
    try {DebugTrace.enter(); // TODO: Debug
    ...
    DebugTrace.print("value", value); // TODO: Debug
    ...
    if (...)
        throw new Exception();
    ...
    } catch (Exception e) {DebugTrace.print("e", e); throw e; // TODO: Debug
    } finally {DebugTrace.leave();} // TODO: Debug
}
```

The following is an example of Java source used DebugTrace-java methods and the log of when it has been executed.

[source,java]
.Example1.java
```java
package example;

import java.util.HashMap;
import java.util.Map;

import org.debugtrace.DebugTrace;

public class Example1 {
    private static final Map<Long, Long> fibonacciMap = new HashMap<>();
    static {
        fibonacciMap.put(0L, 0L);
        fibonacciMap.put(1L, 1L);
    }

    public static void main(String[] args) {
        DebugTrace.enter(); // TODO: Debug
        try {
            if (args.length <= 0)
                throw new IllegalArgumentException("args.length = " + args.length);
            long n = Long.parseLong(args[0]);
            long fibonacci = fibonacci(n);
            System.out.println("fibonacci(" + n + ") = " + fibonacci);
        } catch (Exception e) {
            DebugTrace.print("e", e); // TODO: Debug
        }
        DebugTrace.leave(); // TODO: Debug
    }

    public static long fibonacci(long n) {
        DebugTrace.enter(); // TODO: Debug
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
        DebugTrace.leave(); // TODO: Debug
        return fibonacci;
    }
}
```

.debugtrace.log

```log
2025-07-19 02:34:06.969-07:00 DebugTrace 4.1.2 on Amazon.com Inc. OpenJDK Runtime Environment 17.0.15+6-LTS
2025-07-19 02:34:06.979-07:00   property name: DebugTrace.properties
2025-07-19 02:34:06.987-07:00   logger: org.debugtrace.logger.File (character set: UTF-8, line separator: \n, file: Z:\logs\debugtrace.log)
2025-07-19 02:34:06.989-07:00   time zone: America/Los_Angeles
2025-07-19 02:34:06.996-07:00 
2025-07-19 02:34:07.000-07:00 <i>_____________________________ main _____________________________</i>
2025-07-19 02:34:07.002-07:00 
2025-07-19 02:34:07.004-07:00 Enter example.Example2.main (Example2.java:18) <- (:0)
2025-07-19 02:34:07.007-07:00 | Enter example.Example2.fibonacci (Example2.java:33) <- (Example2.java:23)
2025-07-19 02:34:07.009-07:00 | | Enter example.Example2.fibonacci (Example2.java:33) <- (Example2.java:41)
2025-07-19 02:34:07.038-07:00 | | | mapped fibonacci(1) = (long)1 (Example2.java:39)
2025-07-19 02:34:07.041-07:00 | | Leave example.Example2.fibonacci (Example2.java:48) duration: 00:00:00.029
2025-07-19 02:34:07.043-07:00 | | 
2025-07-19 02:34:07.045-07:00 | | Enter example.Example2.fibonacci (Example2.java:33) <- (Example2.java:41)
2025-07-19 02:34:07.047-07:00 | | | Enter example.Example2.fibonacci (Example2.java:33) <- (Example2.java:41)
2025-07-19 02:34:07.049-07:00 | | | | mapped fibonacci(0) = (long)0 (Example2.java:39)
2025-07-19 02:34:07.051-07:00 | | | Leave example.Example2.fibonacci (Example2.java:48) duration: 00:00:00.001
2025-07-19 02:34:07.061-07:00 | | | 
2025-07-19 02:34:07.066-07:00 | | | Enter example.Example2.fibonacci (Example2.java:33) <- (Example2.java:41)
2025-07-19 02:34:07.068-07:00 | | | | mapped fibonacci(1) = (long)1 (Example2.java:39)
2025-07-19 02:34:07.070-07:00 | | | Leave example.Example2.fibonacci (Example2.java:48) duration: 00:00:00.002
2025-07-19 02:34:07.072-07:00 | | | fibonacci(2) = (long)1 (Example2.java:42)
2025-07-19 02:34:07.074-07:00 | | Leave example.Example2.fibonacci (Example2.java:48) duration: 00:00:00.027
2025-07-19 02:34:07.076-07:00 | | fibonacci(3) = (long)2 (Example2.java:42)
2025-07-19 02:34:07.083-07:00 | Leave example.Example2.fibonacci (Example2.java:48) duration: 00:00:00.074
2025-07-19 02:34:07.088-07:00 | 
2025-07-19 02:34:07.097-07:00 | fibonacciMap = (HashMap)[
2025-07-19 02:34:07.104-07:00 |   (Long)0: (Long)0, (Long)1: (Long)1, (Long)2: (Long)1, (Long)3: (Long)2
2025-07-19 02:34:07.106-07:00 | ] (Example2.java:26)
2025-07-19 02:34:07.113-07:00 | 
2025-07-19 02:34:07.115-07:00 Leave example.Example2.main (Example2.java:29) duration: 00:00:00.108
```

### 3. Method List

This library has the following methods. These are all static methods of <a href=http://masatokokubo.github.io/debugtrace/javadoc/org/debugtrace/DebugTrace.html>`org.debugtrace.DebugTrace`</a> class.

<table>
  <caption>Method List</caption>
  <tr>
    <th>Method Name</th><th>Arguments</th><th>Return Value</th><th>Description</th>
  </tr>
  <tr>
    <td><code>enter</code></td>
    <td><i>None</i></td>
    <td><i>None</i></td>
    <td>Outputs method start to log.</td>
  </tr>
  <tr>
    <td><code>leave</code></td>
    <td><i>None</i></td>
    <td><i>None</i></td>
    <td>Outputs method end to log.</td>
  </tr>
  <tr>
    <td><code>print</code></td>
    <td><code>message</code>: a message</td>
    <td>the <code>message</code></td>
    <td>Outputs the message to log.</td>
  </tr>
  <tr>
    <td><code>print</code></td>
    <td><code>messageSupplier</code>: a supplier of message</td>
    <td> tht message getted from the messageSupplier</td>
    <td>Gets a message from the supplier and output it to log.</td>
  </tr>
  <tr>
    <td><code>print</code></td>
    <td>
      <code>name</code>: the value name<br>
      <code>value</code>: the value
    </td>
    <td>the <code>value</code></td>
    <td>
      Outputs to the log in the form of<br>
      <code>"Name = Value"</code><br>
      <code>value</code> type is one of the following.<br>
      <code>boolean</code>, <code>char</code></code>,<br>
      <code>byte</code>, <code>short</code>, <code>int</code>, <code>long</code>,<br>
      <code>float</code>, <code>double</code>, <code>T</code>
    </td>
  </tr>
  <tr>
    <td><code>print</code></td>
    <td>
      <code>name</code>: the value name<br>
      <code>value</code>: the value<br>
      <code>logOptions</code>: <a href="http://masatokokubo.github.io/debugtrace/javadoc/org/debugtrace/LogOptions.html">LogOptions</a><br>
      The following fields can be specified in <code>logOptions</code>.<br>
      <code>minimumOutputSize</code>,<br>
      <code>minimumOutputLength</code>,<br>
      <code>collectionLimit</code>,<br>
      <code>byteArrayLimit</code>,<br>
      <code>stringLimit</code>,<br>
      <code>reflectionNestLimit</code><br>
      Or the following can be specified.<br>
      <code>LogOptions.outputSize</code><br>
      <code>LogOptions.outputLength</code>
    </td>
    <td>the <code>value</code></td>
    <td>Same as above.</td>
  </tr>
  <tr>
    <td><code>print</code></td>
    <td>
      <code>name</code>: the value name<br>
      <code>valueSupplier</code>: the supplier of the value
    </td>
    <td> the value getted from the <code>valueSupplier</code></td>
    <td>
      Gets a value from the <code>valueSupplier</code> and outputs to the log in the form of<br>
      <code><value name> = <value></code><br>
      <code>valueSupplier</code> type is one of the following.<br>
      <code>BooleanSupplier</code>,<br>
      <code>IntSupplier`, `LongSupplier</code><br>
      <code>Supplier&lt;T&gt;</code>
    </td>
  </tr>
  <tr>
    <td><code>print</code></td>
    <td>
      <code>name</code>: the value name<br>
      <code>valueSupplier</code>: the supplier of the value<br>
      <code>logOptions</code>: <a href="http://masatokokubo.github.io/debugtrace/javadoc/org/debugtrace/LogOptions.html">LogOptions</a><br>
      <i>See above for details</i>
    </td>
    <td> the value getted from the <code>valueSupplier</code></td>
    <td>Same as above.</td>
  </tr>
  <tr>
    <td><code>printStack</code></td>
    <td><code>maxCount</code>:  maximum number of stack trace elements to output</td>
    <td><i>None</i></td>
    <td>Outputs a list of StackTraceElements to the log.</td>
  </tr>
</table>

### 4. Properties of *DebugTrace.properties* file

DebugTrace read `DebugTrace.properties` file in the classpath on startup.  
You can specify following properties in the `DebugTrace.properties` file.  

<table>
  <caption>Property List</caption>
  <tr>
    <th>Property Name</th><th>Description</th>
  </tr>
  <tr>
    <td><code>logger</code></td>
    <td>
      Logger used by DebugTrace<br>
      <small><b>Specifiable Values:</b></small>
      <ul>
        <code>Std$Out</code> ➔ Outputs to stdout<br>
        <code>Std$Err</code> ➔ Outputs to stderr<br>
        <code>Jdk</code> ➔ Outputs using the JDK logger<br>
        <code>Log4j</code> ➔ Outputs using the Log4j 1 logger<br>
        <code>Log4j2</code> ➔ Outputs using the Log4j 2 logger<br>
        <code>SLF4J</code> ➔ Outputs using the SLF4J logger<br>
        <code>File: [[character set][/line separator]:] <log file path></code> ➔ Outputs to the file<br>
        <code>File: [[character set][/line separator]:] +<log file path></code> ➔ Appends to the file<br>
      </ul>
      <ul>
        <code>character set</code> ::= <code>UTF-8</code> | <code>Shift_JIS</code> | ...<br>
        <code>line separator</code> ::= <code>lf</code> | <code>cr</code> | <code>crlf</code><br>
      </ul>
      <small><b>Default Value:</b></small>
      <ul>
        <code>Std$Err</code>
      </ul>
      <small><b>Examples:</b></small>
      <ul>
        <code>logger = File: /logs/debugtrace.log</code><br>
        <code>logger = File: UTF-8: /logs/debugtrace.log</code><br>
        <code>logger = File: UTF-8/lf: /logs/debugtrace.log</code><br>
        <code>logger = File: UTF-8/cr: /logs/debugtrace.log</code><br>
        <code>logger = File: UTF-8/crlf: /logs/debugtrace.log</code><br>
        <code>logger = File: /lf: /logs/debugtrace.log</code><br>
        <code>logger = File :Shift_JIS: /logs/debugtrace.log</code><br>
        <code>logger = File: EUC-JP: /logs/debugtrace.log</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>enterFormat</code></td>
    <td>
      The format string of logging when entering methods<br>
      <small><b>Parameters:</b></small>
      <ul>
        <code>%1</code>: The class name<br>
        <code>%2</code>: The method name<br>
        <code>%3</code>: The file name<br>
        <code>%4</code>: The line number<br>
        <code>%6</code>: The file name of the caller<br>
        <code>%7</code>: The line number of the caller
      </ul>
      <small><b>Default Value:</b></small>
      <ul>
        <code>Enter %1$s.%2$s (%3$s:%4$d) <- (%6$s:%7$d)</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>leaveFormat</code></td>
    <td>
      The format string of logging when leaving methods<br>
      <small><b>Parameters:</b></small>
      <ul>
        <code>%1</code>: The class name<br>
        <code>%2</code>: The method name<br>
        <code>%3</code>: The file name<br>
        <code>%4</code>: The line number<br>
        <code>%5</code>: The duration since invoking the corresponding <code>enter</code> method
      </ul>
      <small><b>Default Value:</b></small>
      <ul>
        <code>Leave %1$s.%2$s (%3$s:%4$d) duration: %5$tT.%5$tL</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>threadBoundaryFormat</code></td>
    <td>
      The format string of logging at threads boundary<br>
      <small><b>Parameter:</b></small>
      <ul>
        <code>%1</code>: The thread name<br>
      </ul>
      <small><b>Default Value:</b></small>
      <ul>
        <small><code>_____________________________ %1$s _____________________________</code></small>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>classBoundaryFormat</code></td>
    <td>
      The format string of logging at classes boundary<br>
      <small><b>Parameter:</b></small>
      <ul>
        <code>%1</code>: The class name<br>
      </ul>
      <small><b>Default Value:</b></small>
      <ul>
        <code>___ %1$s ___</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>indentString</code></td>
    <td>
      The indentation string for code<br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>|\\s</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>dataIndentString</code></td>
    <td>
      The indentation string for data<br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>\\s\\s</code><br>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>limitString</code></td>
    <td>
      The string to represent that it has exceeded the limit<br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>...</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>nonOutputString</code></td>
    <td>
      The string to be output instead of not outputting value<br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>***</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>cyclicReferenceString</code></td>
    <td>
      The string to represent that the cyclic reference occurs<br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>*** cyclic reference ***</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>varNameValueSeparator</code></td>
    <td>
      The separator string between the variable name and value<br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>\\s=\\s</code><br>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>keyValueSeparator</code></td>
    <td>
      The separator string between the key and value of Map object<br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>:\\s</code><br>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>printSuffixFormat</code></td>
    <td>
      The format string of <code>print</code> method suffix<br>
      <small><b>Parameters:</b></small>
      <ul>
        <code>%1</code>: The class name<br>
        <code>%2</code>: The method name<br>
        <code>%3</code>: The file name<br>
        <code>%4</code>: The line number<br>
      </ul>
      <small><b>Default Value:</b></small>
      <ul>
        <code>\\s(%3$s:%4$d)</code><br>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>sizeFormat</code></td>
    <td>
      The format string of the size of collection and map<br>
      <small><b>Parameters:</b></small>
      <ul>
        <code>%1</code>: The size<br>
      </ul>
      <small><b>Default Value:</b></small>
      <ul>
        <code>\\s(%3$s:%4$d)</code><br>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>minimumOutputSize</code></td>
    <td>
      The minimum value to output the number of elements of array, collection and map<br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>Integer.MAX_VALUE</code><small><i>(Same as no output)</i></small>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>lengthFormat</code></td>
    <td>
      The format string of the length of string<br>
      <small><b>Parameters:</b></small>
      <ul>
        <code>%1</code>: The string length<br>
      </ul>
      <small><b>Default Value:</b></small>
      <ul>
        <code>length:%1d</code><br>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>minimumOutputLength</code></td>
    <td>
      The minimum value to output the length of string<br>
      <small><b>Default Value:</b>
      <ul>
        <code>Integer.MAX_VALUE</code> <small><i>(Same as no output)</i></small>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>utilDateFormat</code></td>
    <td>
      The format string of <code>java.util.Date</code><br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>sqlDateFormat</code></td>
    <td>
      The format string of <code>java.sql.Date</code><br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>yyyy-MM-ddxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>timeFormat</code></td>
    <td>
      The format string of <code>java.sql.Time</code><br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>HH:mm:ss.SSSxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>timestampFormat</code></td>
    <td>
      The format string of <code>java.sql.Timestamp</code><br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSSSSSSSxxx</code>
      </ul>
  </tr>
  <tr>
    <td><code>localDateFormat</code></td>
    <td>
      The format string of <code>java.time.LocalDate</code><br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>yyyy-MM-dd</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>localTimeFormat</code></td>
    <td>
      The format string of <code>java.time.LocalTime</code><br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>HH:mm:ss.SSSSSSSSS</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>offsetTimeFormat</code></td>
    <td>
      The format string of <code>java.time.OffsetTime</code><br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>HH:mm:ss.SSSSSSSSSxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>localDateTimeFormat</code></td>
    <td>
      The format string of <code>java.time.LocalDateTime</code><br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSSSSSSS</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>offsetDateTimeFormat</code></td>
    <td>
      The format string of <code>java.time.OffsetDateTime</code><br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSSSSSSSxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>zonedDateTimeFormat</code></td>
    <td>
      The format string of <code>java.time.ZonedDateTime</code><br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSSSSSSSxxx VV</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>instantFormat</code></td>
    <td>
      The format string of <code>java.time.Instant</code><br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSSSSSSSX</code>
      </ul>
  </tr>
  <tr>
    <td><code>logDateTimeFormat</code></td>
    <td>
      The format string of the date and time of the log when the logger is <code>Std$Out</code> or <code>Std$Err</code><br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>timeZone</code></td>
    <td>
      Specifying the time zone (<code>ZoneId.of(timeZone)</code>)<br>
      <small><b>Examples:</b></small>
      <ul>
        <code>timeZone = UTC</code><br>
        <code>timeZone = America/New_York</code><br>
        <code>timeZone = Asia/Tokyo</code><br>
      </ul>
      <small><b>Default Value:</b></small>
      <ul>
        <code>ZoneId.systemDefault()</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>maximumDataOutputWidth</code></td>
    <td>
      The maximum output width of data<br>
      <small><b>Default Value:</b></small>
      <ul>
        70
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>collectionLimit</code></td>
    <td>
      The limit value of elements for collection and map to output<br>
      <small><b>Default Value:</b></small>
      <ul>
        128
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>byteArrayLimit</code></td>
    <td>
      The limit value of elements for byte array (<code>byte[]</code>) to output<br>
      <small><b>Default Value:</b></small>
      <ul>
        256
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>stringLimit</code></td>
    <td>
      The limit value of characters for string to output<br>
      <small><b>Default Value:</b></small>
      <ul>
        256
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>reflectionNestLimit</code></td>
    <td>
      The limit value for reflection nesting<br>
      <small><b>Default Value:</b></small>
      <ul>
        4
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>nonOutputProperties</code></td>
    <td>
      Properties not to be output<br>
      <small><b>Format of a value:</b></small>
      <ul>
        <code>&lt;Full class name&gt;#&lt;Property name&gt;</code>
      </ul>
      <small><b>Default Value:</b></small>
      <ul>
        <i>None</i><br>
      </ul>
      <small><b>Example (1 value):</b></small>
      <ul>
        <code>org.lightsleep.helper.EntityInfo#columnInfos</code>
      </ul>
      <small><b>Example (multi values):</b></small>
      <ul>
        <code>org.lightsleep.helper.EntityInfo#columnInfos,\</code><br>
        <code>org.lightsleep.helper.EntityInfo#keyColumnInfos,\</code><br>
        <code>org.lightsleep.helper.ColumnInfo#entityInfo</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>defaultPackage</code></td>
    <td>
      The default package of your java source<br>
      <small><b>Default Value:</b></small>
      <ul>
        <i>None</i><br>
      </ul>
      <small><b>Example:</b></small>
      <ul>
        <code>org.debugtrace.DebugTraceExample</code><br>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>defaultPackageString</code></td>
    <td>
      The string replacing the default package part<br>
      <small><b>Default Value:</b></small>
      <ul>
        <code>...</code>
      <ul>
    </td>
  </tr>
  <tr>
    <td><code>reflectionClasses</code><br>
    <td>
      Classe names that output content by reflection even if <code>toString</code> method is implemented<br>
      <small><b>Default Value:</b></small>
      <ul>
        <i>None</i><br>
      </ul>
      <small><b>Example (1 value):</b></small>
      <ul>
        <code>org.debugtrce.example.Point</code><br>
      </ul>
      <small><b>Example (multi values):</b></small>
      <ul>
        <code>org.debugtrace.example.Point,\</code><br>
        <code>org.debugtrace.example.Rectangle</code><br>
      </ul>
      <small><b>Example (package):</b></small>
      <ul>
        <code>org.debugtrce.example.</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>mapNameMap</code><br>
    <td>
      The map for obtaining map name corresponding to variable name<br>
      <small><b>Format of a value:</b></small>
      <ul>
        <code>&lt;Variable Name&gt;: &lt;Map Name&gt;</code>
      </ul>
      <small><b>Default Value:</b></small>
      <ul>
        <i>None</i><br>
      </ul>
      <small><b>Example:</b></small>
      <ul>
        <code>appleBrand: AppleBrand</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>&lt;Map Name&gt;</code></td>
    <td>
      The map of numbers (as key) and constant names (as value) corresponding to the numbers<br>
      <small><b>Format of a value:</b></small>
      <ul>
        <code>&lt;Number&gt;: &lt;Constant Name&gt;</code>
      </ul>
      <small><b>Predefined constant name maps:</b></small>
      <ul>
        <code>Calendar</code>: <code>Calendar.ERA</code> etc.<br>
        <code>CalendarWeek</code>: <code>Calendar.SUNDAY</code> etc.<br>
        <code>CalendarMonth</code>: <code>Calendar.JANUARY</code> etc.<br>
        <code>CalendarAmPm</code>: <code>Calendar.AM</code> etc.<br>
        <code>SqlTypes</code>: <code>java.sql.Types.BIT</code> etc.
      </ul>
        <small><b>Example:</b></small>
      <ul>
        <code>AppleBrand = \</code><br>
        &#xa0;&#xa0; <code>0: Apple.NO_BRAND,\</code><br>
        &#xa0;&#xa0; <code>1: Apple.AKANE,\</code><br>
        &#xa0;&#xa0; <code>2: Apple.AKIYO,\</code><br>
        &#xa0;&#xa0; <code>3: Apple.AZUSA,\</code><br>
        &#xa0;&#xa0; <code>4: Apple.YUKARI</code>
      </ul>
    </td>
</table>

In the above, `\\s` will be replaced with a space character.  
Specify the date and time format in the format of the argument of the `DateTimeFormatter.ofPattern` method.

#### 4.1. *`nonOutputProperties`*, *`nonOutputString`*

DebugTrace use reflection to output object contents if the `toString` method is not implemented.
If there are other object references, the contents of objects are also output.
However, if there is circular reference, it will automatically detect and suspend output.
You can suppress output by specifying the `nonOutputProperties` property and
can specify multiple values of this property separated by commas.  
The value of the property specified by `nonOutputProperties` are output as the string specified by `nonOutputString` (default: `***`).

Example of nonOutputProperties in DebugTrace.properties:
```properties
nonOutputProperties = \
    org.lightsleep.helper.EntityInfo#columnInfos,\
    org.lightsleep.helper.EntityInfo#keyColumnInfos,\
    org.lightsleep.helper.ColumnInfo#entityInfo
```

#### 4.2. Constant map and *mapNameMap*

A constant map is a map whose keys are numbers and whose values are constant names.
If you specify the map name corresponding to the variable name in the `mapNameMap` property, the constant name corresponding to the numerical value will also be output.

Example of a constant map and `mapNameMap` in DebugTrace.properties
```properties
AppleBrand = \
    0: Apple.NO_BRAND,\
    1: Apple.AKANE,\
    2: Apple.AKIYO,\
    3: Apple.AZUSA,\
    4: Apple.YUKARI

mapNameMap = appleBrand:AppleBrand
```

Example of Java source:
```java
static public class Apple {
    public static final int NO_BRAND = 0;
    public static final int AKANE = 1;
    public static final int AKIYO = 2;
    public static final int AZUSA = 3;
    public static final int YUKARI = 4;
}
    ...

    int appleBrand = Apple.AKANE;
    DebugTrace.print("appleBrand", appleBrand);
    appleBrand = Apple.AKIYO;
    DebugTrace.print(" 2 appleBrand ", appleBrand);
    appleBrand = Apple.AZUSA;
    DebugTrace.print(" 3 example.appleBrand ", appleBrand);
    appleBrand = Apple.YUKARI;
    DebugTrace.print(" 4 example. appleBrand ", appleBrand);
```

Example of the log:
```log
2023-01-29 10:14:29.916+09:00 appleBrand = 1(Apple.AKANE) (ReadmeExample.java:18)
2023-01-29 10:14:29.916+09:00  2 appleBrand  = 2(Apple.AKIYO) (ReadmeExample.java:20)
2023-01-29 10:14:29.916+09:00  3 example.appleBrand  = 3(Apple.AZUSA) (ReadmeExample.java:22)
2023-01-29 10:14:29.916+09:00  4 example. appleBrand  = 4(Apple.YUKARI) (ReadmeExample.java:24)
```

### 5. Examples of using logging libraries

The logger name of DebugTrace is `org.debugtrace.DebugTrace`.   

#### 5.1. Example of *logging.properties* (*JDK*)

```properties
# logging.properties
handlers = java.util.logging.FileHandler
java.util.logging.FileHandler.level = FINEST
java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format = %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %5$s%n
java.util.logging.FileHandler.encoding = UTF-8
java.util.logging.FileHandler.pattern = /var/log/app/debugtrace.log
java.util.logging.FileHandler.append = false
org.debugtrace.DebugTrace.level = FINEST
```
*`-Djava.util.logging.config.file=<path>/logging.properties` is required as Java startup option*

#### 5.2. Example of *log4j.xml* (*Log4j*)

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">

<log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/" debug="false">
  <appender name="traceAppender" class="org.apache.log4j.FileAppender">
    <param name="File" value="/var/log/app/debugtrace.log"/>
    <param name="Append" value="false" />
    <layout class="org.apache.log4j.PatternLayout">
      <param name="ConversionPattern" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p %t %m%n"/>
    </layout>
  </appender>

  <logger name="org.debugtrace.DebugTrace">
    <level value ="trace"/>
    <appender-ref ref="traceAppender"/>
  </logger>
</log4j:configuration>
```

#### 5.3. Example of *log4j2.xml* (*Log4j2*)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
  <Appenders>
    <File name="traceAppender" append="false" fileName="/var/log/app/debugtrace.log">
      <PatternLayout pattern="%date{yyyy-MM-dd HH:mm:ss.SSS} %-5level %thread %message%n"/>
    </File>
  </Appenders>

  <Loggers>
    <Logger name="org.debugtrace.DebugTrace" level="trace" additivity="false">
        <AppenderRef ref="traceAppender"/>
    </Logger>
  </Loggers>
</Configuration>
```

#### 5.4. Example of *logback.xml* (*SLF4J* / *Logback*)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <appender name="traceAppender" class="ch.qos.logback.core.FileAppender">
    <file>/var/log/app/debugtrace.log</file>
    <encoder>
      <pattern>%date{yyyy-MM-dd HH:mm:ss.SSS} %-5level %thread %message%n</pattern>
    </encoder>
  </appender>

  <logger name="org.debugtrace.DebugTrace" level="trace">
    <appender-ref ref="traceAppender"/>
  </logger>
</configuration>
```

### 6. Example of build.gradle

```gradle
repositories {
    ...
    maven { url 'https://jitpack.io' }
    ...
}

dependencies {
    ...
    implementation 'com.github.masatokokubo:debugtrace:4.1.2'
    ...
}
```

### 7. License

[The MIT License (MIT)](LICENSE.txt)

<i style="color:gray">(C) 2015 Masato Kokubo</i>

### 8. Links

[API Specification](http://masatokokubo.github.io/debugtrace/javadoc/index.html)

### 9. Release Notes

[Releases](https://github.com/MasatoKokubo/debugtrace/releases)
