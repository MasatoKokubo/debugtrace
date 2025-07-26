// DebugTrace.java
// (C) 2015 Masato Kokubo

package org.debugtrace;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.IntStream;

import org.debugtrace.helper.BooleanSupplier;
import org.debugtrace.helper.DoubleSupplier;
import org.debugtrace.helper.IntSupplier;
import org.debugtrace.helper.LongSupplier;
import org.debugtrace.helper.MapUtils;
import org.debugtrace.helper.Resource;
import org.debugtrace.helper.SetUtils;
import org.debugtrace.helper.Supplier;
import org.debugtrace.helper.Tuple;
import org.debugtrace.logger.Logger;
import org.debugtrace.logger.Std;

/**
 * Contains the main static methods of DebugTrace.
 * 
 * @since 1.0.0
 * @author Masato Kokubo
 */
public class DebugTrace {
    /**
     * DebugTrace version string
     * 
     * @since 3.0.0
     */
    public static final String VERSION = "3.8.0";

    // A map for wrapper classes of primitive type to primitive type
    private static final Map<Class<?>, Class<?>> primitiveTypeMap = MapUtils.ofEntries(
        MapUtils.entry(boolean  .class, boolean.class),
        MapUtils.entry(char     .class, char   .class),
        MapUtils.entry(byte     .class, byte   .class),
        MapUtils.entry(short    .class, short  .class),
        MapUtils.entry(int      .class, int    .class),
        MapUtils.entry(long     .class, long   .class),
        MapUtils.entry(float    .class, float  .class),
        MapUtils.entry(double   .class, double .class),
        MapUtils.entry(Boolean  .class, boolean.class),
        MapUtils.entry(Character.class, char   .class),
        MapUtils.entry(Byte     .class, byte   .class),
        MapUtils.entry(Short    .class, short  .class),
        MapUtils.entry(Integer  .class, int    .class),
        MapUtils.entry(Long     .class, long   .class),
        MapUtils.entry(Float    .class, float  .class),
        MapUtils.entry(Double   .class, double .class)
    );

    // A set of classes that dose not output the type name
    private static final Set<Class<?>> noOutputTypeSet = SetUtils.of(
        boolean  .class,
        char     .class,
        int      .class,
        String   .class,
        Date     .class,
        Time     .class,
        Timestamp.class
    );

    // A set of component types of array that dose not output the type name
    private static final Set<Class<?>> noOutputComponentTypeSet = SetUtils.of(
        boolean   .class,
        char      .class,
        byte      .class,
        short     .class,
        int       .class,
        long      .class,
        float     .class,
        double    .class,
        Boolean   .class,
        Character .class,
        Byte      .class,
        Short     .class,
        Integer   .class,
        Long      .class,
        Float     .class,
        Double    .class,
        String    .class,
        BigInteger.class,
        BigDecimal.class,
        Date      .class,
        Time      .class,
        Timestamp .class
    );

    // A set of types that do not output the element type
    private static final Set<Class<?>> noOutputElementTypeSet = SetUtils.of(
        int      .class,
        long     .class,
        double   .class,
        Boolean  .class,
        Character.class,
        Integer  .class,
        String   .class,
        Date     .class,
        Time     .class,
        Timestamp.class
    );


    // Prefixes of getter methods
    private static final String[] getterPrefixes = {"", "get", "is"};

    // The string part of package of Groovy runtime class
    private static final String[] skipPackages = {
        "sun.reflect.",
        "java.lang.reflect.",
        "jdk.internal.reflect.", // since 2.5.1 for Java 11
        "org.codehaus.groovy.",
        "groovy.lang.",
        "org.spockframework."
    };

    // Resources
    private static Resource resource;

    protected static String enterFormat              ; // since 3.0.0 enterFormat <- enterString
    protected static String leaveFormat              ; // since 3.0.0 leaveFormat <- leaveString
    protected static String threadBoundaryFormat     ;
    protected static String classBoundaryFormat      ;
    protected static String indentString             ;
    protected static String dataIndentString         ;
    protected static String limitString              ;
    protected static String nonOutputString          ; // since 1.5.0, since 3.0.0 nonOutputString <- nonPrintString
    protected static String cyclicReferenceString    ;
    protected static String varNameValueSeparator    ;
    protected static String keyValueSeparator        ;
    protected static String printSuffixFormat        ;
    protected static String sizeFormat               ; // since 3.0.0
    protected static int    minimumOutputSize        ; // since 3.0.0
    protected static String lengthFormat             ; // since 3.0.0
    protected static int    minimumOutputLength      ; // since 3.0.0
    protected static String utilDateFormat           ;
    protected static String sqlDateFormat            ;
    protected static String timeFormat               ;
    protected static String timestampFormat          ;
    protected static String localDateFormat          ; // since 2.5.0
    protected static String localTimeFormat          ; // since 2.5.0
    protected static String offsetTimeFormat         ; // since 2.5.0
    protected static String localDateTimeFormat      ; // since 2.5.0
    protected static String offsetDateTimeFormat     ; // since 2.5.0
    protected static String zonedDateTimeFormat      ; // since 2.5.0
    protected static String instantFormat            ; // since 2.5.0
    protected static String logDateTimeFormat        ; // since 2.5.0
    protected static String timeZone                 ; // since 3.8.0
    protected static int    maximumDataOutputWidth   ; // since 3.0.0
    protected static int    collectionLimit          ;
    protected static int    byteArrayLimit           ;
    protected static int    stringLimit              ;
    protected static int    reflectionNestLimit      ; // since 3.0.0
    protected static List<String> nonOutputProperties; // since 2.2.0, since 3.0.0 nonOutputProperties <- nonPrintProperties
    protected static String defaultPackage           ; // since 2.3.0
    protected static String defaultPackageString     ; // since 2.3.0
    protected static Set<String> reflectionClassPaths; // since 3.5.0
    protected static Set<Class<?>> reflectionClasses    = new HashSet<>(); // since 2.4.0
    protected static Set<Class<?>> nonReflectionClasses = new HashSet<>();; // since 3.5.0
    protected static Map<String, String> mapNameMap  ; // since 2.4.0

    // @since 2.5.0
    private static DateTimeFormatter utilDateFormatter      ;
    private static DateTimeFormatter sqlDateFormatter       ;
    private static DateTimeFormatter timeFormatter          ;
    private static DateTimeFormatter timestampFormatter     ;
    private static DateTimeFormatter localDateFormatter     ;
    private static DateTimeFormatter localTimeFormatter     ;
    private static DateTimeFormatter offsetTimeFormatter    ;
    private static DateTimeFormatter localDateTimeFormatter ;
    private static DateTimeFormatter offsetDateTimeFormatter;
    private static DateTimeFormatter zonedDateTimeFormatter ;
    private static DateTimeFormatter instantFormatter       ;
    private static DateTimeFormatter logDateTimeFormatter   ;
    private static ZoneId            zoneId; // since 3.8.0

    // Array of indent strings
    private static final String[] indentStrings = new String[32];

    // Array of data indent strings
    private static final String[] dataIndentStrings = new String[32];

    // Logger
    private static Logger logger = null;

    // Map of thread id to the indent state
    private static final Map<Long,  State> stateMap = new HashMap<>();

    // Before thread id
    private static long beforeThreadId;

    // Reflected object list
    private static final List<Object> reflectedObjects = new ArrayList<>();

    private static final Map<String, Map<Integer, String>> convertMapMap = new HashMap<>();
    private static String lastLog = "";

    // FileLogger keyword since 3.4.0
    private static final String FILE_LOGGER_KEYWORD = "File:";


    private static LogOptions defaultLogOptions;

    static {
        initClass();
    }

    /**
     * Initializes this class.
     * 
     * @since 3.0.0
     */
    public static void initClass() {
        initClass("DebugTrace"); // "DebugTrace" is base name of DebugTrace.properties 
    }

    /**
     * Initializes this class.
     * 
     * @param baseName the base name of the resource properties file
     * @since 3.0.0
     */
    public static void initClass(String baseName) {
        resource = new Resource(DebugTrace.class, baseName);

        enterFormat             = resource.getString("enterFormat"           , "Enter %1$s.%2$s (%3$s:%4$d) <- (%6$s:%7$d)"); // since 3.0.0 enterFormat <- enterString
        leaveFormat             = resource.getString("leaveFormat"           , "Leave %1$s.%2$s (%3$s:%4$d) duration: %5$tT.%5$tL"); // since 3.0.0 leaveFormat <- leaveString
        threadBoundaryFormat    = resource.getString("threadBoundaryFormat"  , "______________________________ %1$s ______________________________");
        classBoundaryFormat     = resource.getString("classBoundaryFormat"   , "____ %1$s ____");
        indentString            = resource.getString("indentString"          , "| ");
        dataIndentString        = resource.getString("dataIndentString"      , "  ");
        limitString             = resource.getString("limitString"           , "...");
        nonOutputString         = resource.getString("nonOutputString"       , "***"); // since 1.5.0, since 3.0.0 nonOutputString <- nonPrintString
        cyclicReferenceString   = resource.getString("cyclicReferenceString" , "*** cyclic reference ***");
        varNameValueSeparator   = resource.getString("varNameValueSeparator" , " = ");
        keyValueSeparator       = resource.getString("keyValueSeparator"     , ": ");
        printSuffixFormat       = resource.getString("printSuffixFormat"     , " (%3$s:%4$d)");
        sizeFormat              = resource.getString("sizeFormat"            , "size:%1d"); // since 3.0.0
        minimumOutputSize       = resource.getInt   ("minimumOutputSize"     , Integer.MAX_VALUE); // <- 16 <- 5 since 3.0.0
        lengthFormat            = resource.getString("lengthFormat"          , "length:%1d"); // since 3.0.0
        minimumOutputLength     = resource.getInt   ("minimumOutputLength"   , Integer.MAX_VALUE); // <- 16 <- 5 since 3.0.0
        utilDateFormat          = resource.getString("utilDateFormat"        , "yyyy-MM-dd HH:mm:ss.SSSxxx");
        sqlDateFormat           = resource.getString("sqlDateFormat"         , "yyyy-MM-ddxxx");
        timeFormat              = resource.getString("timeFormat"            , "HH:mm:ss.SSSxxx");
        timestampFormat         = resource.getString("timestampFormat"       , "yyyy-MM-dd HH:mm:ss.SSSSSSSSSxxx");
        localDateFormat         = resource.getString("localDateFormat"       , "yyyy-MM-dd"); // since 2.5.0
        localTimeFormat         = resource.getString("localTimeFormat"       , "HH:mm:ss.SSSSSSSSS"); // since 2.5.0
        offsetTimeFormat        = resource.getString("offsetTimeFormat"      , "HH:mm:ss.SSSSSSSSSxxx"); // since 2.5.0
        localDateTimeFormat     = resource.getString("localDateTimeFormat"   , "yyyy-MM-dd HH:mm:ss.SSSSSSSSS"); // since 2.5.0
        offsetDateTimeFormat    = resource.getString("offsetDateTimeFormat"  , "yyyy-MM-dd HH:mm:ss.SSSSSSSSSxxx"); // since 2.5.0
        zonedDateTimeFormat     = resource.getString("zonedDateTimeFormat"   , "yyyy-MM-dd HH:mm:ss.SSSSSSSSSxxx VV"); // since 2.5.0
        instantFormat           = resource.getString("instantFormat"         , "yyyy-MM-dd HH:mm:ss.SSSSSSSSS"); // since 2.5.0
        logDateTimeFormat       = resource.getString("logDateTimeFormat"     , "yyyy-MM-dd HH:mm:ss.SSSxxx"); // since 2.5.0
        timeZone                = resource.getString("timeZone"              , null); // since 3.8.0
        maximumDataOutputWidth  = resource.getInt   ("maximumDataOutputWidth", 70); // since 3.0.0
        collectionLimit         = resource.getInt   ("collectionLimit"       , 128); // <- 512 since 3.5.0
        byteArrayLimit          = resource.getInt   ("byteArrayLimit"        , 256); // <- 8192 since 3.5.0
        stringLimit             = resource.getInt   ("stringLimit"           , 256); // <- 8192 since 3.5.0
        reflectionNestLimit     = resource.getInt   ("reflectionNestLimit"   , 4); // since 3.0.0
        nonOutputProperties     = resource.getStrings("nonOutputProperties"  ); // since 2.2.0, since 3.0.0 nonOutputProperties <- nonPrintProperties
        defaultPackage          = resource.getString("defaultPackage"        , ""); // since 2.3.0
        defaultPackageString    = resource.getString("defaultPackageString"  , "..."); // since 2.3.0
        reflectionClassPaths    = resource.getStringSet("reflectionClasses"  ); // since 3.5.0, 2.4.0
        mapNameMap              = resource.getStringKeyMap("baseMapNameMap"  ); // since 3.6.0
        mapNameMap.putAll        (resource.getStringKeyMap("mapNameMap"     )); // since 2.4.0
        mapNameMap.keySet().forEach(key -> {
            String mapName = mapNameMap.get(key);
            convertMapMap.put(mapName, resource.getIntegerKeyMap(mapName));
        });

        utilDateFormatter       = createDateTimeFormatter(utilDateFormat      );
        sqlDateFormatter        = createDateTimeFormatter(sqlDateFormat       );
        timeFormatter           = createDateTimeFormatter(timeFormat          );
        timestampFormatter      = createDateTimeFormatter(timestampFormat     );
        localDateFormatter      = createDateTimeFormatter(localDateFormat     );
        localTimeFormatter      = createDateTimeFormatter(localTimeFormat     );
        offsetTimeFormatter     = createDateTimeFormatter(offsetTimeFormat    );
        localDateTimeFormatter  = createDateTimeFormatter(localDateTimeFormat );
        offsetDateTimeFormatter = createDateTimeFormatter(offsetDateTimeFormat);
        zonedDateTimeFormatter  = createDateTimeFormatter(zonedDateTimeFormat );
        instantFormatter        = createDateTimeFormatter(instantFormat       );
        logDateTimeFormatter    = createDateTimeFormatter(logDateTimeFormat   );
        try {
            if (timeZone != null)
                zoneId = ZoneId.of(timeZone);
        }
        catch (Exception e) {
            System.err.println("DebugTrace: " + e.toString());
        }
        if (zoneId == null)
            zoneId = ZoneId.systemDefault();

        indentStrings[0] = "";
        IntStream.iterate(1, index -> index + 1).limit(indentStrings.length - 1)
            .forEach(index -> indentStrings[index] = indentStrings[index - 1] + indentString);

        dataIndentStrings[0] = "";
        IntStream.iterate(1, index -> index + 1).limit(dataIndentStrings.length - 1)
            .forEach(index -> dataIndentStrings[index] = dataIndentStrings[index - 1] + dataIndentString);

        logger = null;
        String loggerName = null;
        try {
            loggerName = resource.getString("logger", null);
            if (loggerName != null) {
                // FileLogger detection
                if (loggerName.startsWith(FILE_LOGGER_KEYWORD)) {
                    // File Logger
                    String path = loggerName.substring(FILE_LOGGER_KEYWORD.length()).trim();
                    Charset charset = Charset.forName("UTF-8");
                    LineSeparator lineSeparator = LineSeparator.parse(System.getProperty("line.separator"));
                    int colonIndex = path.indexOf(':');
                    if (colonIndex >= 0) {
                        // Character set specified
                        String charsetName = path.substring(0, colonIndex).trim();
                        int slashIndex = charsetName.indexOf('/');
                        if (slashIndex >= 0) {
                            // Line separator specified
                            String lineSeparatorStr = charsetName.substring(slashIndex + 1).trim();
                            charsetName = charsetName.substring(0, slashIndex).trim();
                            try {
                                lineSeparator = LineSeparator.parse(lineSeparatorStr);
                            }
                            catch (Exception e) {
                                System.err.println("DebugTrace: " + e.toString());
                            }
                        }
                        try {
                            if (charsetName.length() > 0)
                                charset = Charset.forName(charsetName);
                        }
                        catch (Exception e) {
                            System.err.println("DebugTrace: " + e.toString());
                        }
                        path = path.substring(colonIndex + 1).trim();
                    }
                    boolean append = false;
                    if (path.startsWith("+")) {
                        append = true;
                        path = path.substring(1).trim();
                    }
                    File file = new File(path).getAbsoluteFile();
                    File parentFile = file.getParentFile();
                    if (!parentFile.exists())
                        throw new RuntimeException(parentFile.getPath() + " dose not exist.");
                    if (file.exists() && !file.isFile())
                        throw new RuntimeException(file.getPath() + " is not a file.");
                    logger = new org.debugtrace.logger.File(file, charset, lineSeparator, append);
                } else {
                    // not File Logger
                    if (loggerName.indexOf('.') == -1)
                        loggerName = Logger.class.getPackage().getName() + '.' + loggerName;
                    logger = (Logger)Class.forName(loggerName).getConstructor().newInstance();
                }
            }
        }
        catch (Exception e) {
            System.err.println("DebugTrace: " + e.toString() + "(" + loggerName + ")");
            if (e instanceof InvocationTargetException) {
                Throwable targetEx = ((InvocationTargetException)e).getTargetException();
                if (targetEx != null)
                    System.err.println("DebugTrace: " + targetEx.toString() + " (" + loggerName + ")");
            }
        }

        if (logger == null)
            logger = new Std.Err();

        // Get the Java vendor and runtime version
        String javaVendor = System.getProperty("java.vendor");
        String javaRuntimeName = System.getProperty("java.runtime.name");
        String javaRuntimeVersion = System.getProperty("java.runtime.version");

        logger.log("DebugTrace " + VERSION + " on " +
            javaVendor + " " + javaRuntimeName + " " + javaRuntimeVersion);
        logger.log("  property name: " + baseName + ".properties");
        logger.log("  logger: " + logger.toString());
        logger.log("  time zone: " + zoneId.toString());

        defaultLogOptions = new LogOptions();
    }

    private DebugTrace() {}

    /**
     * Creates a DateTimeFormatter.
     * 
     * @param format a format
     * @return a DateTimeFormatter
     * @since 2.5.0
     */
    private static DateTimeFormatter createDateTimeFormatter(String format) {
        try {
            return DateTimeFormatter.ofPattern(format);
        }
        catch (Exception e) {
            logger.log("\"" + format + "\": " + e.getMessage());
        }
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
    }

    /**
     * Append a timestamp to the head of the string.<br>
     * <i>This method is used internally.</i>
     *
     * @param string a string
     * @return a string appended a timestamp string
     */
    public static String appendTimestamp(String string) {
        return logDateTimeFormatter == null ? string : ZonedDateTime.now(zoneId).format(logDateTimeFormatter) + " " + string;
    }

    /**
     * Returns indent state.
     */
    private static State getCurrentState() {
        State state;
        Long threadId = Thread.currentThread().getId();

        if (stateMap.containsKey(threadId)) {
            state = stateMap.get(threadId);
        } else {
            state = new State(threadId);
            stateMap.put(threadId, state);
        }


        return state;
    }

    /**
     * Returns whether tracing is enabled.
     *
     * @return true if tracing is enabled; false otherwise
     */
    public static boolean isEnabled() {
        return logger.isEnabled();
    }

    /**
     * Returns a string corresponding to the code and data nest level.
     *
     * @param nestLevel the code nest level
     * @param dataNestLevel the data nest level
     * @return a string corresponding to the current indent
     */
    private static String getIndentString(int nestLevel, int dataNestLevel) {
        return indentStrings[
                nestLevel < 0 ? 0 :
                nestLevel >= indentStrings.length ? indentStrings.length - 1
                    : nestLevel]
            + dataIndentStrings[
                dataNestLevel < 0 ? 0 :
                dataNestLevel >= dataIndentStrings.length ? dataIndentStrings.length - 1
                    : dataNestLevel];
    }

    /**
     * Common start processing of output.
     */
    private static void printStart() {
        Thread thread = Thread.currentThread();
        long threadId = thread.getId();
        if (threadId !=  beforeThreadId) {
            // Thread changing
            logger.log(""); // Line break
            logger.log(String.format(threadBoundaryFormat, thread.getName(), threadId));
            logger.log(""); // Line break

            beforeThreadId = threadId;
        }
    }

    /**
     * Call this method at entrance of your methods.
     */
    public static void enter() {
        if (isEnabled()) {
            synchronized(stateMap) {
                State state = getCurrentState();
    
                printStart(); // Common start processing of output
    
                if (state.previousNestLevel() > state.nestLevel())
                    logger.log(getIndentString(state.nestLevel(), 0)); // Line break
    
                lastLog = getIndentString(state.nestLevel(), 0) + createPrintString(enterFormat, null);
                logger.log(lastLog);
    
                state.setPreviousLineCount(1);
    
                state.upNest();
            }
        }
    }

    /**
     * Call this method at exit of your methods.
     */
    public static void leave() {
        if (isEnabled()) {
            synchronized(stateMap) {
                State state = getCurrentState();
    
                printStart(); // Common start processing of output
    
                if (state.previousLineCount() > 1)
                    logger.log(getIndentString(state.nestLevel(), 0)); // Empty Line
    
                long timeSpan = System.nanoTime() - state.downNest();
                Instant instant = Instant.ofEpochSecond(timeSpan / 1000_000_000, timeSpan % 1000_000_000);
                OffsetDateTime dateTime = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);

                lastLog = getIndentString(state.nestLevel(), 0) + createPrintString(leaveFormat, dateTime);
                logger.log(lastLog);
            }
        }
    }

    /**
     * Outputs the message to the log.
     *
     * @param message a message
     * @return the message
     */
    public static String print(String message) {
        if (isEnabled())
            printSub(message);
        return message;
    }

    /**
     * Outputs a message to the log.
     *
     * @param messageSupplier a message supplier
     * @return the message if isEnabled(), otherwise null
     */
    public static String print(Supplier<String> messageSupplier) {
        if (isEnabled()) {
            try {
                String message = messageSupplier.get();
                printSub(messageSupplier.get());
                return message;
            }
            catch (Exception e) { 
                printSub(e.toString());
            }
        }
        return null;
    }

    /**
     * Outputs the message to the log.
     *
     * @param message a message
     */
    private static void printSub(String message) {
        synchronized(stateMap) {
            printStart(); // Common start processing of output

            if (!message.isEmpty()) {
                lastLog = getIndentString(getCurrentState().nestLevel(), 0) +
                    message + createPrintString(printSuffixFormat, null);
            }
            logger.log(lastLog);
        }
    }

    /**
     * Outputs the name and value to the log.
     *
     * @param name the name of the value
     * @param value the value to output (accept null)
     * @param logOptions LogOptions
     * @param isPrimitive true if the value is primitive type, false otherwise
     */
    private static void printSub(String name, Object value, LogOptions logOptions, boolean isPrimitive) {
        synchronized(stateMap) {
            State state = getCurrentState();
 
            printStart(); // Common start processing of output

            reflectedObjects.clear();

            LogBuffer buff = new LogBuffer();

            buff.append(name);
            String mapName = getMapName(name);
            LogBuffer valueBuff = toString(mapName, value, logOptions, isPrimitive, false, false);
            buff.append(varNameValueSeparator, valueBuff);

            buff.noBreakAppend(createPrintString(printSuffixFormat, null));

            List<Tuple._2<Integer, String>> lines = buff.lines();
            if (state.previousLineCount() > 1 || lines.size() > 1)
                logger.log(getIndentString(state.nestLevel(), 0)); // Empty Line

            StringBuilder lastLogBuff = new StringBuilder();
            for (Tuple._2<Integer, String> dataNestLevelLine : lines) {
                int dataNestLevel = dataNestLevelLine.value1();
                String line = dataNestLevelLine.value2();
                String log = getIndentString(state.nestLevel(), dataNestLevel) + line;
                logger.log(log);
                lastLogBuff.append(log).append('\n');
            }
            lastLog = lastLogBuff.toString();

            state.setPreviousLineCount(lines.size());
        }
    }

    /**
     * Creates a print string.
     *
     * @param formatString the format string
     * @param dateTime the time from method start to end
     * @return a print string
     * @since 3.6.0
     */
    private static String createPrintString(String formatString, OffsetDateTime dateTime) {
        List<StackTraceElement> elements = getStackTraceElements(2);
        StackTraceElement element = elements.size() > 0
            ? elements.get(0)
            : new StackTraceElement("", "", "", 0);
        StackTraceElement parentElement = elements.size() > 1
            ? elements.get(1)
            : new StackTraceElement("", "", "", 0);

        String printString = String.format(formatString,
            replaceTypeName(element.getClassName()),
            element.getMethodName(),
            element.getFileName(),
            element.getLineNumber(),
            dateTime,
            parentElement.getFileName(),
            parentElement.getLineNumber());
        return printString;
    }

    /**
     * Returns stack trace elements.
     *
     * @param maxCount maximum number of stack trace elements to return
     * @return stack trace elements
     */
    private static List<StackTraceElement> getStackTraceElements(int maxCount) {
        String myClassName = DebugTrace.class.getName();

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        List<StackTraceElement> result = new ArrayList<>();
        outerLoop:
        for (int index = 2; index < elements.length; ++index) {
            StackTraceElement element = elements[index];
            String className = element.getClassName();
            if (className.indexOf(myClassName) >= 0) continue;
            for (String skipPackage : skipPackages)
               if (className.indexOf(skipPackage) >= 0) continue outerLoop;
            result.add(element);
            if (result.size() >= maxCount)
                break;
        }

        return result;
    }

    /**
     * Outputs the name and the boolean value to the log.
     *
     * @param name the name of the value
     * @param value the boolean value to output
     * @return the value
     */
    public static boolean print(String name, boolean value) {
        return print(name, value, defaultLogOptions);
    }

    /**
     * Outputs the name and the boolean value to the log.
     *
     * @param name the name of the value
     * @param value the boolean value to output
     * @param logOptions LogOptions
     * @return the value
     * @since 3.7.0
     */
    public static boolean print(String name, boolean value, LogOptions logOptions) {
        if (isEnabled())
            printSub(name, value, logOptions, true);
        return value;
    }

    /**
     * Outputs the name and the char value to the log.
     *
     * @param name the name of the value
     * @param value the char value to output
     * @return the value
     */
    public static char print(String name, char value) {
        return print(name, value, defaultLogOptions);
    }

    /**
     * Outputs the name and the char value to the log.
     *
     * @param name the name of the value
     * @param value the char value to output
     * @param logOptions LogOptions
     * @return the value
     * @since 3.7.0
     */
    public static char print(String name, char value, LogOptions logOptions) {
        if (isEnabled())
            printSub(name, value, defaultLogOptions, true);
        return value;
    }

    /**
     * Outputs the name and the byte value to the log.
     *
     * @param name the name of the value
     * @param value the byte value to output
     * @return the value
     */
    public static byte print(String name, byte value) {
        return print(name, value, defaultLogOptions);
    }

    /**
     * Outputs the name and the byte value to the log.
     *
     * @param name the name of the value
     * @param value the byte value to output
     * @param logOptions LogOptions
     * @return the value
     * @since 3.7.0
     */
    public static byte print(String name, byte value, LogOptions logOptions) {
        if (isEnabled())
            printSub(name, value, logOptions, true);
        return value;
    }

    /**
     * Outputs the name and the short value to the log.
     *
     * @param name the name of the value
     * @param value the short value to output
     * @return the value
     */
    public static short print(String name, short value) {
        return print(name, value, defaultLogOptions);
    }


    /**
     * Outputs the name and the short value to the log.
     *
     * @param name the name of the value
     * @param value the short value to output
     * @param logOptions LogOptions
     * @return the value
     * @since 3.7.0
     */
    public static short print(String name, short value, LogOptions logOptions) {
        if (isEnabled())
            printSub(name, value, logOptions, true);
        return value;
    }

    /**
     * Outputs the name and the int value to the log.
     *
     * @param name the name of the value
     * @param value the int value to output
     * @return the value
     */
    public static int print(String name, int value) {
        return print(name, value, defaultLogOptions);
    }

    /**
     * Outputs the name and the int value to the log.
     *
     * @param name the name of the value
     * @param value the int value to output
     * @param logOptions LogOptions
     * @return the value
     * @since 3.7.0
     */
    public static int print(String name, int value, LogOptions logOptions) {
        if (isEnabled())
            printSub(name, value, logOptions, true);
        return value;
    }

    /**
     * Outputs the name and the long value to the log.
     *
     * @param name the name of the value
     * @param value the long value to output
     * @return the value
     */
    public static long print(String name, long value) {
        return print(name, value, defaultLogOptions);
    }

    /**
     * Outputs the name and the long value to the log.
     *
     * @param name the name of the value
     * @param value the long value to output
     * @param logOptions LogOptions
     * @return the value
     * @since 3.7.0
     */
    public static long print(String name, long value, LogOptions logOptions) {
        if (isEnabled())
            printSub(name, value, logOptions, true);
        return value;
    }

    /**
     * Outputs the name and the value to the log.
     *
     * @param name the name of the value
     * @param value the float value to output
     * @return the value
     */
    public static float print(String name, float value) {
        return print(name, value, defaultLogOptions);
    }

    /**
     * Outputs the name and the value to the log.
     *
     * @param name the name of the value
     * @param value the float value to output
     * @param logOptions LogOptions
     * @return the value
     * @since 3.7.0
     */
    public static float print(String name, float value, LogOptions logOptions) {
        if (isEnabled())
            printSub(name, value, logOptions, true);
        return value;
    }

    /**
     * Outputs the name and the value to the log.
     *
     * @param name the name of the value
     * @param value the double value to output
     * @return the value
     */
    public static double print(String name, double value) {
        return print(name, value, defaultLogOptions);
    }


    /**
     * Outputs the name and the value to the log.
     *
     * @param name the name of the value
     * @param value the double value to output
     * @param logOptions LogOptions
     * @return the value
     * @since 3.7.0
     */
    public static double print(String name, double value, LogOptions logOptions) {
        if (isEnabled())
            printSub(name, value, logOptions, true);
        return value;
    }

    /**
     * Outputs the name and the value to the log.
     *
     * @param <T> the type of the value
     * @param name the name of the value
     * @param value the value to output (accept null)
     * @return the value
     */
    public static <T> T print(String name, T value) {
        return print(name, value, defaultLogOptions);
    }


    /**
     * Outputs the name and the value to the log.
     *
     * @param <T> the type of the value
     * @param name the name of the value
     * @param value the value to output (accept null)
     * @param logOptions LogOptions
     * @return the value
     * @since 3.7.0
     */
    public static <T> T print(String name, T value, LogOptions logOptions) {
        if (isEnabled())
            printSub(name, value, logOptions, false);
        return value;
    }

    /**
     * Outputs the name and the boolean value to the log.
     *
     * @param name the name of the value
     * @param valueSupplier the supplier of boolean value to output
     * @return the value if isEnabled(), otherwise false
     */
    public static boolean print(String name, BooleanSupplier valueSupplier) {
        return print(name, valueSupplier, defaultLogOptions);
    }


    /**
     * Outputs the name and the boolean value to the log.
     *
     * @param name the name of the value
     * @param valueSupplier the supplier of boolean value to output
     * @param logOptions LogOptions
     * @return the value if isEnabled(), otherwise false
     * @since 3.7.0
     */
    public static boolean print(String name, BooleanSupplier valueSupplier, LogOptions logOptions) {
        if (isEnabled()) {
            try {
                boolean value = valueSupplier.getAsBoolean();
                printSub(name, value, defaultLogOptions, true);
                return value;
            }
            catch (Exception e) { 
                printSub(name, e.toString(), defaultLogOptions, false);
            }
        }
        return false;
    }

    /**
     * Outputs the name and the  int value to the log.
     *
     * @param name the name of the value
     * @param valueSupplier the supplier of int value to output
     * @return the value if isEnabled(), otherwise 0
     */
    public static int print(String name, IntSupplier valueSupplier) {
        return print(name, valueSupplier, defaultLogOptions);
    }

    /**
     * Outputs the name and the  int value to the log.
     *
     * @param name the name of the value
     * @param valueSupplier the supplier of int value to output
     * @param logOptions LogOptions
     * @return the value if isEnabled(), otherwise 0
     * @since 3.7.0
     */
    public static int print(String name, IntSupplier valueSupplier, LogOptions logOptions) {
        if (isEnabled()) {
            try {
                int value = valueSupplier.getAsInt();
                printSub(name, value, logOptions, true);
                return value;
            }
            catch (Exception e) { 
                printSub(name, e.toString(), logOptions, false);
            }
        }
        return 0;
    }

    /**
     * Outputs the name and the  long value to the log.
     *
     * @param name the name of the value
     * @param valueSupplier the supplier of long value to output
     * @return the value if isEnabled(), otherwise 0L
     */
    public static long print(String name, LongSupplier valueSupplier) {
        return print(name, valueSupplier, defaultLogOptions);
    }

    /**
     * Outputs the name and the  long value to the log.
     *
     * @param name the name of the value
     * @param valueSupplier the supplier of long value to output
     * @param logOptions LogOptions
     * @return the value if isEnabled(), otherwise 0L
     * @since 3.7.0
     */
    public static long print(String name, LongSupplier valueSupplier, LogOptions logOptions) {
        if (isEnabled()) {
            try {
                long value = valueSupplier.getAsLong();
                printSub(name, value, defaultLogOptions, true);
                return value;
            }
            catch (Exception e) { 
                printSub(name, e.toString(), defaultLogOptions, false);
            }
        }
        return 0L;
    }

    /**
     * Outputs the name and the  double value to the log.
     *
     * @param name the name of the value
     * @param valueSupplier the supplier of double value to output
     * @return the value if isEnabled(), otherwise 0.0
     */
    public static double print(String name, DoubleSupplier valueSupplier) {
        return print(name, valueSupplier, defaultLogOptions);
    }

    /**
     * Outputs the name and the  double value to the log.
     *
     * @param name the name of the value
     * @param valueSupplier the supplier of double value to output
     * @param logOptions LogOptions
     * @return the value if isEnabled(), otherwise 0.0
     * @since 3.7.0
     */
    public static double print(String name, DoubleSupplier valueSupplier, LogOptions logOptions) {
        if (isEnabled()) {
            try {
                double value = valueSupplier.getAsDouble();
                printSub(name, value, defaultLogOptions, true);
                return value;
            }
            catch (Exception e) { 
                printSub(name, e.toString(), defaultLogOptions, false);
            }
        }
        return 0.0;
    }

    /**
     * Outputs the name and the value to the log.
     *
     * @param <T> the type of the value
     * @param name the name of the value
     * @param valueSupplier the supplier of value to output
     * @return the value if isEnabled(), otherwise null
     */
    public static <T> T print(String name, Supplier<T> valueSupplier) {
        return print(name, valueSupplier, defaultLogOptions);
    }


    /**
     * Outputs the name and the value to the log.
     *
     * @param <T> the type of the value
     * @param name the name of the value
     * @param valueSupplier the supplier of value to output
     * @param logOptions LogOptions
     * @return the value if isEnabled(), otherwise null
     * @since 3.7.0
     */
    public static <T> T print(String name, Supplier<T> valueSupplier, LogOptions logOptions) {
        if (isEnabled()) {
            try {
                T value = valueSupplier.get();
                printSub(name, value, logOptions, false);
                return value;
            }
            catch (Exception e) { 
                printSub(name, e.toString(), logOptions, false);
            }
        }
        return null;
    }

    /**
     * Outputs a list of StackTraceElements to the log.
     *
     * @param maxCount maximum number of stack trace elements to output
     */
    public static void printStack(int maxCount) {
        if (isEnabled())
            print("stack", getStackTraceElements(maxCount), defaultLogOptions);
    }

    /**
     * Returns a string representation of the value as a LogBuffer.
     *
     * @param mapName the name of the map for get a constant name corresponding to the value (accept null)
     * @param value the value object
     * @param logOptions LogOptions
     * @param isPrimitive true if the value is primitive type, false otherwise
     * @param isComponent true if the value is component of an array, false otherwise
     * @param isElement true if the value is element of a container class, false otherwise
     * @return a LogBuffer
     */
    private static LogBuffer toString(String mapName, Object value, LogOptions logOptions, boolean isPrimitive, boolean isComponent, boolean isElement) {
        logOptions.normalize();
        LogBuffer buff = new LogBuffer();

        if (value == null) {
            buff.append("null");
            return buff;
        }

        Class<?> type = value.getClass();
        if (isPrimitive) {
            type = primitiveTypeMap.get(type);
            if (type == null)
                type = value.getClass();
        }

        String typeName = getTypeName(type, value, logOptions, isComponent, isElement, 0);

        if (type.isArray()) {
            // Array
            if (type == char[].class) {
                // sting
                buff.noBreakAppend(typeName);
                appendString(buff, new String((char[])value), logOptions);
            } else if (type == byte[].class) {
                // byte Array
                LogBuffer valueBuff = toStringBytes((byte[])value, logOptions);
                buff.append(null, valueBuff);
            } else {
                // Other Array
                LogBuffer valueBuff = toStringArray(mapName, value, logOptions);
                buff.append(null, valueBuff);
            }

        } else if (value instanceof Boolean) {
            // Boolean
            buff.noBreakAppend(typeName);
            buff.noBreakAppend(value);

        } else if (value instanceof Character) {
            // Character
            buff.noBreakAppend(typeName);
            buff.noBreakAppend('\'');
            appendChar(buff, ((Character)value).charValue(), false);
            buff.noBreakAppend('\'');

        } else if (value instanceof Number) {
            // Number
            buff.noBreakAppend(typeName);
            if (value instanceof BigDecimal) buff.append(((BigDecimal)value).toPlainString()); // BigDecimal
            else buff.noBreakAppend(value); // Other Number

        } else if (value instanceof CharSequence) {
            // CharSequence
            buff.noBreakAppend(typeName);
            appendString(buff, (CharSequence)value, logOptions);

        } else if (value instanceof java.util.Date) {
            // Date
            buff.noBreakAppend(typeName);
            Timestamp timestamp = value instanceof Timestamp ? (Timestamp)value : new Timestamp(((java.util.Date)value).getTime());
            ZonedDateTime zonedDateTime = timestamp.toLocalDateTime().atZone(zoneId);
            if      (value instanceof Date     ) buff.noBreakAppend(zonedDateTime.format(sqlDateFormatter  )); // java.sql.Date
            else if (value instanceof Time     ) buff.noBreakAppend(zonedDateTime.format(timeFormatter     )); // Time
            else if (value instanceof Timestamp) buff.noBreakAppend(zonedDateTime.format(timestampFormatter)); // Timestamp
            else                                 buff.noBreakAppend(zonedDateTime.format(utilDateFormatter )); // java.util.Date

        } else if (value instanceof Temporal) {
            // Temporal
            buff.noBreakAppend(typeName);
            if      (value instanceof LocalDate     ) buff.noBreakAppend(((LocalDate     )value).format(localDateFormatter     )); // LocalDate
            else if (value instanceof LocalTime     ) buff.noBreakAppend(((LocalTime     )value).format(localTimeFormatter     )); // LocalTime
            else if (value instanceof OffsetTime    ) buff.noBreakAppend(((OffsetTime    )value).format(offsetTimeFormatter    )); // OffsetTime
            else if (value instanceof LocalDateTime ) buff.noBreakAppend(((LocalDateTime )value).format(localDateTimeFormatter )); // LocalDateTime
            else if (value instanceof OffsetDateTime) buff.noBreakAppend(((OffsetDateTime)value).format(offsetDateTimeFormatter)); // OffsetDateTime
            else if (value instanceof ZonedDateTime ) buff.noBreakAppend(((ZonedDateTime )value).format(zonedDateTimeFormatter )); // ZonedDateTime
            else if (value instanceof Instant) buff.noBreakAppend(((Instant)value).atOffset(ZoneOffset.ofHours(0)).format(instantFormatter       )); // Instant
            else buff.noBreakAppend(value);

        } else if (value instanceof OptionalInt) {
            // OptionalInt
            buff.noBreakAppend(typeName);
            if (((OptionalInt)value).isPresent())
                buff.noBreakAppend(((OptionalInt)value).getAsInt());
            else
                buff.noBreakAppend("empty");

        } else if (value instanceof OptionalLong) {
            // OptionalLong
            buff.noBreakAppend(typeName);
            if (((OptionalLong)value).isPresent())
                buff.noBreakAppend(((OptionalLong)value).getAsLong());
            else
                buff.noBreakAppend("empty");

        } else if (value instanceof OptionalDouble) {
            // OptionalDouble
            buff.noBreakAppend(typeName);
            if (((OptionalDouble)value).isPresent())
                buff.noBreakAppend(((OptionalDouble)value).getAsDouble());
            else
                buff.noBreakAppend("empty");

        } else if (value instanceof Optional) {
            // Optional
            buff.noBreakAppend(typeName);
            if (((Optional<?>)value).isPresent()) {
                LogBuffer valueBuff = toString(mapName, ((Optional<?>)value).get(), logOptions, false, false, true);
                buff.append(null, valueBuff);
            }else
                buff.noBreakAppend("empty");

        } else if (value instanceof Collection) {
            // Collection
            LogBuffer valueBuff = toStringCollection(mapName, (Collection<?>)value, logOptions);
            buff.append(null, valueBuff);

        } else if (value instanceof Map) {
            // Map
            LogBuffer valueBuff = toStringMap(mapName, (Map<?,?>)value, logOptions);
            buff.append(null, valueBuff);

        } else if (value instanceof Clob) {
            // Clob
            try {
                long length = ((Clob)value).length();
                if (length > (long)logOptions.stringLimit)
                    length = (long)(logOptions.stringLimit + 1);
                buff.noBreakAppend(typeName);
                appendString(buff, ((Clob)value).getSubString(1L, (int)length), logOptions);
            }
            catch (SQLException e) {
                buff.append(e);
            }

        } else if (value instanceof Blob) {
            // Blob
            try {
                long length = ((Blob)value).length();
                if (length > (long)logOptions.byteArrayLimit)
                    length = (long)(logOptions.byteArrayLimit + 1);
                LogBuffer valueBuff = toStringBytes(((Blob)value).getBytes(1L, (int)length), logOptions);
                buff.append(typeName, valueBuff);
            }
            catch (SQLException e) {
                buff.append(e);
            }

        } else {
            // Other
            String className = type.getName();
            String packageName = type.getPackage() == null ? "" : type.getPackage().getName() + '.';
            boolean isReflection = reflectionClasses.contains(type);
            if (!isReflection) {
                // not reflection class
                if (!nonReflectionClasses.contains(type)) {
                    // unknown
                    isReflection = reflectionClassPaths.stream()
                        .anyMatch(classPath ->
                            classPath.endsWith(".")
                                ? classPath.equals(packageName) // Specifies the package
                                : classPath.equals(className) // Specifies the class
                        );
                    if (!isReflection && !hasToString(type))
                        isReflection = true; // Dose not have a toString method
                    (isReflection ? reflectionClasses : nonReflectionClasses).add(type);
                }
            }

            if (isReflection) {
                // Use Reflection
                if (reflectedObjects.stream().anyMatch(object -> value == object))
                    // Cyclic reference
                    buff.append(cyclicReferenceString).append(value);

                else if (reflectedObjects.size() >= logOptions.reflectionNestLimit)
                    // Over reflection level limitation
                    buff.append(limitString);

                else {
                    // Use Reflection
                    reflectedObjects.add(value);
                    try {
                        LogBuffer valueBuff = toStringReflection(value, logOptions);
                        buff.append(null, valueBuff);
                    }
                    finally {
                        reflectedObjects.remove(reflectedObjects.size() - 1);
                    }
                    return buff;
                }
            } else {
                // Use toString method
                buff.noBreakAppend(typeName);
                buff.noBreakAppend(value);
            }
        }
        String convertedValue =  getConvertedValue(mapName, value);
        if (convertedValue != null)
            buff.noBreakAppend('(').noBreakAppend(convertedValue).noBreakAppend(')');

        return buff;
    }

    /**
     * Returns the type name to be output to the log.<br>
     * If dose not output, returns null.
     *
     * @param type the type of the value
     * @param value the value object
     * @param logOptions LogOptions
     * @param isComponent true if the value is component of an array, false otherwise
     * @param isElement true if the value is element of a container class, false otherwise
     * @param nest current nest count
     * @return the type name to be output to the log
     */
    @SuppressWarnings("rawtypes")
    private static String getTypeName(Class<?>type, Object value, LogOptions logOptions, boolean isComponent, boolean isElement, int nest) {
        String typeName = "";
        long length = -1L;
        int size = -1;

        if (type.isArray()) {
            // Array
            typeName = getTypeName(type.getComponentType(), null, logOptions, false, false, nest + 1);
            if (!typeName.isEmpty()) {
                String bracket = "[";
                if (value != null)
                    bracket += Array.getLength(value);
                bracket += ']';
                int braIndex = typeName.indexOf('[');
                if (braIndex < 0)
                    braIndex = typeName.length();
                typeName = typeName.substring(0, braIndex) + bracket + typeName.substring(braIndex);
            }
        } else {
            // Not Array
            if (   nest > 0
                || (isComponent
                        ? !noOutputComponentTypeSet.contains(type)
                    : isElement
                        ? !noOutputElementTypeSet.contains(type)
                        : !noOutputTypeSet.contains(type))
                ) {
                // Output the type name
                typeName = type.getCanonicalName();
                if (typeName == null)
                    typeName = type.getName();
                if (   typeName.startsWith("java.lang.")
                    || typeName.startsWith("java.math.")
                    || typeName.startsWith("java.sql.")
                    || typeName.startsWith("java.time.")
                    || typeName.startsWith("java.util.") && !typeName.equals("java.util.Date"))
                    typeName = type.getSimpleName();
                else
                    typeName = replaceTypeName(typeName);
            }

            if (value != null) {
                try {
                    if      (value instanceof CharSequence) length = ((CharSequence)value).length();
                    else if (value instanceof Blob        ) length = ((Blob        )value).length();
                    else if (value instanceof Clob        ) length = ((Clob        )value).length();
                    else if (value instanceof Collection  ) size   = ((Collection  )value).size();
                    else if (value instanceof Map         ) size   = ((Map         )value).size();
                }
                catch (SQLException e) {}
            }
    }

        if (length >= logOptions.minimumOutputLength) {
            if (!typeName.isEmpty())
                typeName += " ";
            typeName += String.format(lengthFormat, length);

        } else if (size >= logOptions.minimumOutputSize) {
            if (!typeName.isEmpty())
                typeName += " ";
            typeName += String.format(sizeFormat, size);
        }

        if (!typeName.isEmpty() && nest == 0)
            typeName = "(" + typeName + ")";

        return typeName;
    }

    /**
     * Replace a class name.
     *
     * @param className a class name
     * @return the replaced ckass name
     * @since 2.3.0
     */
    private static String replaceTypeName(String typeName) {
        if (!defaultPackage.isEmpty() && typeName.startsWith(defaultPackage))
            typeName = defaultPackageString + typeName.substring(defaultPackage.length());
        return typeName;
    }

    /**
     * Returns the map for converting.
     *
     * @param mapName the name of the map for get a constant name corresponding to the value (accept null)
     * @param value the value object
     * @return a converted value (may be null)
     * @since 2.4.0
     */
    private static String getConvertedValue(String mapName, Object value) {
        if (mapName == null || value == null)
            return null;

        Integer key = null;
        if (value instanceof Byte)
            key = (int)(byte)value;
        else if (value instanceof Short)
            key = (int)(short)value;
        else if (value instanceof Integer)
            key = (Integer)value;
        else if (value instanceof Long) {
            if ((long)value >= Integer.MIN_VALUE && (long)value <= Integer.MAX_VALUE)
                key = (int)(long)value;
        }
        if (key == null)
            return null;

        Map<Integer, String> convertMap = convertMapMap.get(mapName);
        if (convertMap == null) {
            convertMap = resource.getIntegerKeyMap(mapName);
            convertMapMap.put(mapName, convertMap);
        }

        return convertMap.get(key);
    }

    /**
     * Appends a character representation for logging to the string buffer.
     *
     * @param buff a LogBuffer
     * @param ch a character
     * @param inString true if the character is included in the string, false otherwise
     */
    private static void appendChar(LogBuffer buff, char ch, boolean inString) {
        switch (ch) {
        case '\b': buff.noBreakAppend("\\b" ); break; // 08 BS
        case '\t': buff.noBreakAppend("\\t" ); break; // 09 HT
        case '\n': buff.noBreakAppend("\\n" ); break; // 0A LF
        case '\f': buff.noBreakAppend("\\f" ); break; // 0C FF
        case '\r': buff.noBreakAppend("\\r" ); break; // 0D CR
        case '"' : buff.noBreakAppend(inString ? "\\\"" : "\""); break; // "
        case '\'': buff.noBreakAppend(inString ? "'" : "\\'" ); break; // '
        case '\\': buff.noBreakAppend("\\\\"); break; // \
        default:
            if (ch < ' ' || ch == '\u007F')
                buff.noBreakAppend("\\u").noBreakAppend(String.format("%04X", (short)ch));
            else
                buff.noBreakAppend(ch);
            break;
        }
    }

    /**
     * Appends a CharSequence representation for logging to the string buffer.
     *
     * @param buff a LogBuffer
     * @param charSequence a CharSequence
     * @param logOptions LogOptions
     * @param charSequence a CharSequence object
     */
    private static void appendString(LogBuffer buff, CharSequence charSequence, LogOptions logOptions) {
        buff.noBreakAppend('"');
        for (int index = 0; index < charSequence.length(); ++index) {
            if (index >= logOptions.stringLimit) {
                buff.noBreakAppend(limitString);
                break;
            }
            appendChar(buff, charSequence.charAt(index), true);
        }
        buff.noBreakAppend('"');
    }

    /**
     * Returns a string representation of the bytes as a LogBuffer.
     *
     * @param bytes a byte array
     * @param logOptions LogOptions
     * @return a LogBuffer
     */
    private static LogBuffer toStringBytes(byte[] bytes, LogOptions logOptions) {
        LogBuffer buff = new LogBuffer();
        StringBuilder charBuff = new StringBuilder();

        boolean isMultiLines = bytes.length > 16 && logOptions.byteArrayLimit > 16;

        buff.append(getTypeName(bytes.getClass(), bytes, logOptions, false, false, 0));
        buff.noBreakAppend('[');

        if (isMultiLines) {
            buff.lineFeed();
            buff.upNest();
        }

        int offset = 0;
        for (int index = 0; index < bytes.length; ++index) {
            if (isMultiLines && offset == 0)
                // outputs hexadecimal address
                buff.append(String.format("%04X ", index));

            if (offset > 0)
                buff.append(" ");

            if (index >= logOptions.byteArrayLimit) {
                buff.noBreakAppend(limitString);
                break;
            }

            // outputs hexadecimal byte
            int value = bytes[index];
            if (value < 0) value += 256;
            char ch = (char)(value / 16 + '0');
            if (ch > '9') ch += 'A' - '9' - 1;
            buff.noBreakAppend(ch);
            ch = (char)(value % 16 + '0');
            if (ch > '9') ch += 'A' - '9' - 1;
            buff.noBreakAppend(ch);

            // outputs as character
            charBuff.append((char)(value >= 0x20 && value <= 0x7E ? value : '.'));

            ++offset;

            if (isMultiLines && offset == 16) {
                buff.noBreakAppend("  ").noBreakAppend(charBuff);
                buff.lineFeed();
                offset = 0;
                charBuff.setLength(0);
            }
        }

        if (isMultiLines) {
            if (buff.length() > 0) {
                for (; offset < 16; ++offset)
                    buff.noBreakAppend("   "); // padding
                buff.noBreakAppend("  ").noBreakAppend(charBuff);
                buff.lineFeed();
            }
            buff.downNest();
        } else {
            if (offset > 0)
                buff.noBreakAppend("  ").noBreakAppend(charBuff);
        }
        buff.noBreakAppend(']');

        return buff;
    }

    /**
     * Returns a string representation of the array as a LogBuffer.
     *
     * @param mapName the name of the map for get a constant name corresponding to the value (accept null)
     * @param array an array
     * @param logOptions LogOptions
     * @return a LogBuffer
     */
    private static LogBuffer toStringArray(String mapName, Object array, LogOptions logOptions) {
        LogBuffer buff = new LogBuffer();

        buff.append(getTypeName(array.getClass(), array, logOptions, false, false, 0));
        buff.noBreakAppend('[');

        LogBuffer bodyBuff = toStringArrayBody(mapName, array, logOptions);

        boolean isMultiLines = bodyBuff.isMultiLines() || buff.length() + bodyBuff.length() > maximumDataOutputWidth;

        if (isMultiLines) {
            buff.lineFeed();
            buff.upNest();
        }

        buff.append(null, bodyBuff);

        if (isMultiLines) {
            buff.lineFeed();
            buff.downNest();
        }

        buff.noBreakAppend(']');

        return buff;
    }

    private static LogBuffer toStringArrayBody(String mapName, Object array, LogOptions logOptions) {
        LogBuffer buff = new LogBuffer();

        Class<?> componentType = array.getClass().getComponentType();

        int length = Array.getLength(array);

        boolean wasMultiLines = false;
        for (int index = 0; index < length; ++index) {
            if (index > 0)
                buff.noBreakAppend(", "); // Append a delimiter

            if (index >= logOptions.collectionLimit) {
                buff.append(limitString);
                break;
            }

            Object component = Array.get(array, index);

            LogBuffer elementBuff = toString(mapName, component, logOptions, componentType.isPrimitive(), true, false);
            if (index > 0 && (wasMultiLines || elementBuff.isMultiLines()))
                buff.lineFeed();
            buff.append(null, elementBuff);

            wasMultiLines = elementBuff.isMultiLines();
        }

        return buff;
    }

    /**
     * Returns a string representation of the collection as a LogBuffer.
     *
     * @param mapName the name of the map for get a constant name corresponding to the value (accept null)
     * @param collection a Collection
     * @param logOptions LogOptions
     * @return a LogBuffer
     */
    private static <E> LogBuffer toStringCollection(String mapName, Collection<E> collection, LogOptions logOptions) {
        LogBuffer buff = new LogBuffer();

        buff.append(getTypeName(collection.getClass(), collection, logOptions, false, false, 0));
        buff.noBreakAppend('[');

        LogBuffer bodyBuff = toStringCollectionBody(mapName, collection, logOptions);

        boolean isMultiLines = bodyBuff.isMultiLines() || buff.length() + bodyBuff.length() > maximumDataOutputWidth;

        if (isMultiLines) {
            buff.lineFeed();
            buff.upNest();
        }

        buff.append(null, bodyBuff);

        if (isMultiLines) {
            buff.lineFeed();
            buff.downNest();
        }

        buff.noBreakAppend(']');

        return buff;
    }

    private static <E> LogBuffer toStringCollectionBody(String mapName, Collection<E> collection, LogOptions logOptions) {
        LogBuffer buff = new LogBuffer();
    
        Iterator<E> iterator = collection.iterator();

        boolean wasMultiLines = false;
        for (int index = 0; iterator.hasNext(); ++index) {
            if (index > 0)
                buff.noBreakAppend(", ");

            if (index >= logOptions.collectionLimit) {
                buff.append(limitString);
                break;
            }

            E element = iterator.next();

            LogBuffer elementBuff = toString(mapName, element, logOptions, false, false, true);
            if (index > 0 && (wasMultiLines || elementBuff.isMultiLines()))
                buff.lineFeed();
            buff.append(null, elementBuff);

            wasMultiLines = elementBuff.isMultiLines();
        }

        return buff;
    }

    /**
     * Returns a string representation of the map as a LogBuffer.
     *
     * @param mapName the name of the map for get a constant name corresponding to the value (accept null)
     * @param map a Map
     * @param logOptions LogOptions
     * @return a LogBuffer
     */
    private static <K, V> LogBuffer toStringMap(String mapName, Map<K, V> map, LogOptions logOptions) {
        LogBuffer buff = new LogBuffer();

        buff.append(getTypeName(map.getClass(), map, logOptions, false, false, 0));
        buff.noBreakAppend('[');

        LogBuffer bodyBuff = toStringMapBody(mapName, map, logOptions);

        boolean isMultiLines = bodyBuff.isMultiLines() || buff.length() + bodyBuff.length() > maximumDataOutputWidth;

        if (isMultiLines) {
            buff.lineFeed();
            buff.upNest();
        }

        buff.append(null, bodyBuff);

        if (isMultiLines) {
            buff.lineFeed();
            buff.downNest();
        }

        buff.noBreakAppend(']');

        return buff;
    }

    private static <K, V> LogBuffer toStringMapBody(String mapName, Map<K, V> map, LogOptions logOptions) {
        LogBuffer buff = new LogBuffer();
    
        Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();

        boolean wasMultiLines = false;
        for (int index = 0; iterator.hasNext(); ++index) {
            if (index > 0)
                buff.noBreakAppend(", ");

            if (index >= logOptions.collectionLimit) {
                buff.append(limitString);
                break;
            }

            Map.Entry<K, V> keyValue = iterator.next();

            LogBuffer entryBuff = new LogBuffer();
            LogBuffer keyBuff = toString(mapName, keyValue.getKey(), logOptions, false, false, true);
            LogBuffer valueBuff = toString(mapName, keyValue.getValue(), logOptions, false, false, true);
            entryBuff.append(null, keyBuff).append(keyValueSeparator, valueBuff);

            if (index > 0 && (wasMultiLines || entryBuff.isMultiLines()))
                buff.lineFeed();
            buff.append(null, entryBuff);

            wasMultiLines = entryBuff.isMultiLines();
        }

        return buff;
    }

    /**
     * Returns true, if this class or super classes without Object class has toString method.
     *
     * @param object an object
     * @return true if this class or super classes without Object class has toString method; false otherwise
     */
    private static boolean hasToString(Class<?> clazz) {
        boolean result = false;

        while (clazz != null && clazz != Object.class) {
            try {
                clazz.getDeclaredMethod("toString");
                result = true;
                break;
            }
            catch (Exception e) {
            }
            clazz = clazz.getSuperclass();
        }

        return result;
    }

    /**
     * Returns a string representation of the object as a LogBuffer uses reflection.
     *
     * @param object an object
     * @param logOptions LogOptions
     * @return a LogBuffer
     */
    private static LogBuffer toStringReflection(Object object, LogOptions logOptions) {
        LogBuffer buff = new LogBuffer();

        Class<?> type = object.getClass();
        buff.append(getTypeName(type, object, logOptions, false, false, 0));
        boolean isExtended = type.getSuperclass() != null && type.getSuperclass() != Object.class;

        LogBuffer bodyBuff = toStringReflectionBody(object, logOptions, type, isExtended);

        boolean isMultiLines = bodyBuff.isMultiLines() || buff.length() + bodyBuff.length() > maximumDataOutputWidth;

        buff.noBreakAppend('{');
        if (isMultiLines) {
            buff.lineFeed();
            buff.upNest();
        }

        buff.append(null, bodyBuff);

        if (isMultiLines) {
            if (buff.length() > 0)
                buff.lineFeed();
            buff.downNest();
        }
        buff.noBreakAppend('}');

        return buff;
    }

    private static LogBuffer toStringReflectionBody(Object object, LogOptions logOptions, Class<?> type, boolean isExtended) {
        LogBuffer buff = new LogBuffer();

        Class<?> baseType = type.getSuperclass();
        if (baseType != null && baseType != Object.class) {
            // Call for the base type
            LogBuffer baseBuff =  toStringReflectionBody(object, logOptions, baseType, isExtended);
            buff.append(null, baseBuff);
        }

        String typeNamePrefix = type.getName() + "#";

        if (isExtended) {
            if (buff.length() > 0)
                buff.lineFeed();
            buff.append(String.format(classBoundaryFormat, replaceTypeName(type.getName())));
            buff.lineFeed();
        }

        // fields
        boolean first = true;
        boolean wasMultiLines = false;
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers)) continue; // static

            String fieldName = field.getName();

            Object value = null;
            Method method = null; // getter method

            if (!Modifier.isPublic(modifiers)) {
                // non public field
                // get getter method
                for (String getterPrefix : getterPrefixes) {
                    String methodName = getterPrefix.length() == 0
                        ? fieldName
                        : getterPrefix + fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH) + fieldName.substring(1);
                    try {
                        method = type.getDeclaredMethod(methodName);
                        if (method.getReturnType() == field.getType())
                            break;
                        else
                            // return type of the getter method is not the field type
                            method = null;
                    }
                    catch (Exception e) {
                    }
                }
            }

            try {
                if (method != null) {
                    // has a getter method
                    if (!Modifier.isPublic(method.getModifiers()))
                        // non public method
                        method.setAccessible(true);
                    value = method.invoke(object);
                } else {
                    // does not have a getter method
                    if (!Modifier.isPublic(modifiers))
                        // non public field
                        field.setAccessible(true);
                    value = field.get(object);
                }
            }
            catch (Exception e) {
                value = "<" + e + ">";
            }

            if (!first)
                buff.noBreakAppend(", ");

            LogBuffer fieldBuff = new LogBuffer();
            fieldBuff.append(fieldName);

            if (value != null && nonOutputProperties.contains(typeNamePrefix + fieldName) || fieldName.equals("metaClass"))
                // the property is non-printing and the value is not null or Groovy's metaClass
                fieldBuff.noBreakAppend(keyValueSeparator).noBreakAppend(nonOutputString);
            else {
                String mapName = getMapName(fieldName);
                LogBuffer valueBuff = toString(mapName, value, logOptions, field.getType().isPrimitive(), false, false);
                fieldBuff.append(keyValueSeparator, valueBuff);
            }

            if (!first && (wasMultiLines || fieldBuff.isMultiLines()))
                buff.lineFeed();
            buff.append(null, fieldBuff);

            first = false;
            wasMultiLines = fieldBuff.isMultiLines();
        }

        return buff;
    }

    /**
     * Returns the map name.
     * @param name the key of the mapNameMap
     * @return the map name.
     * @since 3.6.0
     */
    private static String getMapName(String name) {
        if (name == null) return null;
        String mapNameKey = name.toLowerCase();
        mapNameKey = mapNameKey.substring(mapNameKey.lastIndexOf('.') + 1).trim();
        mapNameKey = mapNameKey.substring(mapNameKey.lastIndexOf(' ') + 1);
        String mapName = mapNameMap.get(mapNameKey);
        return mapName;
    }

    /**
     * Returns the last log string output.
     *
     * @return the last log string output.
     * @since 2.4.0
     */
    public static String getLastLog() {
        return lastLog;
    }
}
