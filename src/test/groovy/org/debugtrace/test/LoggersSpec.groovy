// DebugTraceSpec.groovy
// (C) 2015 Masato Kokubo

package org.debugtrace.test

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.LogManager

import org.debugtrace.DebugTrace
import spock.lang.*

/**
 * @since 3.1.0
 */
@Unroll
class LoggersSpec extends Specification {
    def setupSpec() {
    }

    def cleanupSpec() {
        DebugTrace.initClass('DebugTrace')
    }

    static String systemSeparator = System.getProperty ('line.separator')

    // enterStringSpec
    def "loggersSpec #loggerName"(String loggerName, String charsetName, String lineSeparator) {
        setup:
        def logPath = "logs/${loggerName}.log"
        if (loggerName == 'Jdk') {
            def propertyPath = "src/test/resources/logging_${loggerName}.properties"
            System.setProperty('java.util.logging.config.file', propertyPath)
            LogManager.getLogManager().readConfiguration()
        }
        DebugTrace.initClass("DebugTrace_${loggerName}")

        when:
        DebugTrace.print("This is a ${loggerName} log.");
        def lastLog = DebugTrace.lastLog + lineSeparator

        def logChars = new char[1024];
        def reader = new InputStreamReader(new FileInputStream(logPath), Charset.forName(charsetName))
        def readSize = reader.read(logChars, 0, logChars.length)
        def log = new String(logChars, 0, readSize)
        System.out.print(log)

        then:
        log.contains(lastLog)

        cleanup:
        if (reader != null) reader.close()

        where:
        loggerName       |charsetName|lineSeparator
        'Jdk'            |'UTF-8'    |systemSeparator
        'Log4j'          |'UTF-8'    |systemSeparator
        'Log4j2'         |'UTF-8'    |systemSeparator
        'SLF4J'          |'UTF-8'    |systemSeparator // and Logback
        'File'           |'UTF-8'    |systemSeparator // since 3.4.0
        // since 3.8.0
        'File-UTF-8'     |'UTF-8'    |systemSeparator 
        'File-Shift_JIS' |'Shift_JIS'|systemSeparator 
        'File-EUC-JP'    |'EUC-JP'   |systemSeparator 
        'File-UTF-8-LF'  |'UTF-8'    |'\n' 
        'File-UTF-8-CR'  |'UTF-8'    |'\r' 
        'File-UTF-8-CRLF'|'UTF-8'    |'\r\n'
    }
}
