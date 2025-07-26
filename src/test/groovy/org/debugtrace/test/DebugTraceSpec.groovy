// DebugTraceSpec.groovy
// (C) 2015 Masato Kokubo

package org.debugtrace.test

import java.sql.Time
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.debugtrace.DebugTrace
import org.debugtrace.LogOptions
import spock.lang.*

@Unroll
class DebugTraceSpec extends Specification {
    enum Fruits {APPLE, ORANGE, MELON, GRAPE, PINEAPPLE}

    static commonSuffix = ' (' + DebugTraceSpec.class.simpleName + '.groovy:'

    @Shared options = new LogOptions()
    @Shared timeZone = TimeZone.default

    def setupSpec() {
        options.minimumOutputSize = 5
        options.minimumOutputLength = 5
        options.collectionLimit = 5
        options.byteArrayLimit = 512
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+9"))
    }

    def cleanupSpec() {
        TimeZone.default = timeZone
    }

    // enterStringSpec
    def enterLeaveStringSpec() {
        when: DebugTrace.enter()
        then: DebugTrace.lastLog.startsWith('Enter')

        when: DebugTrace.leave()
        then: DebugTrace.lastLog.startsWith('Leave')
    }

    // nonPrintPropertiesSpec
    def nonPrintPropertiesSpec() {
        setup:
        DebugTrace.enter()

        when:
        def p = new Point(2, 3);

        then:
        DebugTrace.print('p', p) == p
        DebugTrace.lastLog.indexOf('y: ***') >= 0

        cleanup:
        DebugTrace.leave()
    }

    // defaultPackageSpec
    def defaultPackageSpec() {
        setup:
        DebugTrace.enter()

        when:
        def p = new Point(2, 3);

        then:
        DebugTrace.print('p', p) == p
        DebugTrace.lastLog.indexOf('....Point') >= 0

        cleanup:
        DebugTrace.leave()
    }

    // reflectionClassesSpec
    def reflectionClassesSpec() {
        setup:
        DebugTrace.enter()

        when:
        def p = new Point(2, 3);

        then:
        DebugTrace.print('p', p) == p
        DebugTrace.lastLog.indexOf('Point){') >= 0

        cleanup:
        DebugTrace.leave()
    }

    // printSpec
    def printSpec() {
        setup:
        DebugTrace.enter()

        expect:
        DebugTrace.print('v', false) == false
        DebugTrace.lastLog.indexOf('v = false') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', true) == true
        DebugTrace.lastLog.indexOf('v = true') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', (byte)127) == (byte)127
        DebugTrace.lastLog.indexOf('v = (byte)127') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', (short)32767) == (short)32767
        DebugTrace.lastLog.indexOf('v = (short)32767') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', 999_999_999) == 999_999_999
        DebugTrace.lastLog.indexOf('v = 999999999') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', 999_999_999_999L) == 999_999_999_999L
        DebugTrace.lastLog.indexOf('v = (long)999999999999') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', 999F) == 999F
        DebugTrace.lastLog.indexOf('v = (float)999.0') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', 999D) == 999D
        DebugTrace.lastLog.indexOf('v = (double)999.0') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', (char)'A') == (char)'A'
        DebugTrace.lastLog.indexOf("v = 'A'") >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', (char)'\b') == (char)'\b'
        DebugTrace.lastLog.indexOf("v = '\\b'") >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', (char)'\t') == (char)'\t'
        DebugTrace.lastLog.indexOf("v = '\\t'") >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', (char)'\n') == (char)'\n'
        DebugTrace.lastLog.indexOf("v = '\\n'") >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', (char)'\r') == (char)'\r'
        DebugTrace.lastLog.indexOf("v = '\\r'") >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', (char)'\f') == (char)'\f'
        DebugTrace.lastLog.indexOf("v = '\\f'") >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', (char)'\'') == (char)'\''
        DebugTrace.lastLog.indexOf("v = '\\''") >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', (char)'\u0001') == (char)'\u0001'
        DebugTrace.lastLog.indexOf("v = '\\u0001'") >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', (char)'\u001F') == (char)'\u001F'
        DebugTrace.lastLog.indexOf("v = '\\u001F'") >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', (char)'\u007F') == (char)'\u007F'
        DebugTrace.lastLog.indexOf("v = '\\u007F'") >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        // 3.0.7
        DebugTrace.print('v', (char)'"') == (char)'"'
        DebugTrace.lastLog.indexOf("v = '\"'") >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', "ABCD") == "ABCD"
        DebugTrace.lastLog.indexOf('v = "ABCD"') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', "ABCDE", options) == "ABCDE"
        DebugTrace.lastLog.indexOf('v = (length:5)"ABCDE"') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        // 3.0.7
        DebugTrace.print('v', "'ABCDE'", options) == "'ABCDE'"
        DebugTrace.lastLog.indexOf('v = (length:7)"\'ABCDE\'"') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        // 3.0.7
        DebugTrace.print('v', "ABCDE\b\t\n\r\f'\"\\", options) == "ABCDE\b\t\n\r\f'\"\\"
        DebugTrace.lastLog.indexOf('v = (length:13)"ABCDE\\b\\t\\n\\r\\f\'\\"\\\\"') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', "\u0001\u001F\u007F") == "\u0001\u001F\u007F"
        DebugTrace.lastLog.indexOf('v = "\\u0001\\u001F\\u007F"') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        // 3.0.6
        when:
        def object = new Object()
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (Object){}') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        cleanup:
        DebugTrace.leave()
    }

    // simpleTypeNameSpec
    def simpleTypeNameSpec() {
        setup:
        DebugTrace.enter()

        when:
        Object object = null
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = null') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = Boolean.TRUE
        then:
        DebugTrace.print('v', (Object)object) == object
        DebugTrace.lastLog.indexOf('v = (Boolean)true') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = (Character)'A'
        then:
        DebugTrace.print('v', (Object)object) == object
        DebugTrace.lastLog.indexOf('v = (Character)\'A\'') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = (Byte)1
        then:
        DebugTrace.print('v', (Object)object) == object
        DebugTrace.lastLog.indexOf('v = (Byte)1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = (Short)1
        then:
        DebugTrace.print('v', (Object)object) == object
        DebugTrace.lastLog.indexOf('v = (Short)1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = (Integer)1
        then:
        DebugTrace.print('v', (Object)object) == object
        DebugTrace.lastLog.indexOf('v = (Integer)1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = 1L
        then:
        DebugTrace.print('v', (Object)object) == object
        DebugTrace.lastLog.indexOf('v = (Long)1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = 1.1F
        then:
        DebugTrace.print('v', (Object)object) == object
        DebugTrace.lastLog.indexOf('v = (Float)1.1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = 1.1D
        then:
        DebugTrace.print('v', (Object)object) == object
        DebugTrace.lastLog.indexOf('v = (Double)1.1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = BigInteger.ONE
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (BigInteger)1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = BigDecimal.ONE
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (BigDecimal)1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = new java.util.Date(1000L)
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (java.util.Date)1970-01-01 09:00:01') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = new Date(1000L)
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = 1970-01-01') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = new Time(1000L)
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = 09:00:01') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = new Timestamp(1234L)
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = 1970-01-01 09:00:01.234') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = new Name('F', 'L')
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (....Name){') >= 0
        DebugTrace.lastLog.indexOf('first: "F",') >= 0
        DebugTrace.lastLog.indexOf('last: "L"') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = Fruits.APPLE
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (....DebugTraceSpec.Fruits)APPLE') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        cleanup:
        DebugTrace.leave()
    }

    // optionalTypeNameSpec
    def optionalTypeNameSpec() {
        setup:
        DebugTrace.enter()

        expect:
        DebugTrace.print('v', OptionalInt.empty()) == OptionalInt.empty()
        DebugTrace.lastLog.indexOf('v = (OptionalInt)empty') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', OptionalInt.of(1)) == OptionalInt.of(1)
        DebugTrace.lastLog.indexOf('v = (OptionalInt)1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', OptionalLong.empty()) == OptionalLong.empty()
        DebugTrace.lastLog.indexOf('v = (OptionalLong)empty') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', OptionalLong.of(1L)) == OptionalLong.of(1L)
        DebugTrace.lastLog.indexOf('v = (OptionalLong)1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', OptionalDouble.empty()) == OptionalDouble.empty()
        DebugTrace.lastLog.indexOf('v = (OptionalDouble)empty') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', OptionalDouble.of(1.1D)) == OptionalDouble.of(1.1D)
        DebugTrace.lastLog.indexOf('v = (OptionalDouble)1.1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', Optional.<Integer>empty()) == Optional.<Integer>empty()
        DebugTrace.lastLog.indexOf('v = (Optional)empty') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', Optional.of(1)) == Optional.of(1)
        DebugTrace.lastLog.indexOf('v = (Optional)1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        DebugTrace.print('v', Optional.of(1L)) == Optional.of(1L)
        DebugTrace.lastLog.indexOf('v = (Optional)(Long)1') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        cleanup:
        DebugTrace.leave()
    }

    // arrayTypeNameSpec
    def arrayTypeNameSpec() {
        setup:
        DebugTrace.enter()
        Object object = null
        
        when:
        object = [false, true] as boolean[] 
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (boolean[2])[false, true]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = ['A', 'B'] as char[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (char[2])"AB"') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [-1, 0, 1, 2] as byte[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (byte[4])[FF 00 01 02  ....]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [-1, 0, 1, 2] as short[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (short[4])[-1, 0, 1, 2]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [-1, 0, 1, 2] as int[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (int[4])[-1, 0, 1, 2]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [-1, 0, 1, 2] as long[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (long[4])[-1, 0, 1, 2]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object =[-1.1, 0, 1.1, 2.2] as float[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (float[4])[-1.1, 0.0, 1.1, 2.2]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [-1.1, 0, 1.1, 2.2] as double[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (double[4])[-1.1, 0.0, 1.1, 2.2]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [false, true] as Boolean[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (Boolean[2])[false, true]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = ['A', 'B'] as Character[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (Character[2])[\'A\', \'B\']') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [-1, 0, 1, 2] as Byte[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (Byte[4])[-1, 0, 1, 2]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [-1, 0, 1, 2] as Short[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (Short[4])[-1, 0, 1, 2]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [-1, 0, 1, 2] as Integer[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (Integer[4])[-1, 0, 1, 2]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [-1, 0, 1, 2] as Long[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (Long[4])[-1, 0, 1, 2]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [-1.1, 0, 1.1, 2.2] as Float[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (Float[4])[-1.1, 0.0, 1.1, 2.2]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [-1.1, 0, 1.1, 2.2] as Double[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (Double[4])[-1.1, 0.0, 1.1, 2.2]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [-1.1, 0, 1.1, 2.2] as BigDecimal[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (BigDecimal[4])[-1.1, 0, 1.1, 2.2]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = ["A", "B"] as String[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (String[2])["A", "B"]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [Fruits.APPLE, Fruits.ORANGE] as Fruits[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (....DebugTraceSpec.Fruits[2])[(....DebugTraceSpec.Fruits)APPLE, (....DebugTraceSpec.Fruits)ORANGE]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [1, 2, 3, 4, 5] as int[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (int[5])[1, 2, 3, 4, 5]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when:
        object = [1, 2, 3, 4, 5] as Integer[]
        then:
        DebugTrace.print('v', object) == object
        DebugTrace.lastLog.indexOf('v = (Integer[5])[1, 2, 3, 4, 5]') >= 0
        DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        cleanup:
        DebugTrace.leave()
    }

    // listTypeNameSpec
    def listTypeNameSpec() {
        setup:
        DebugTrace.enter()

        when: DebugTrace.print('v', [false, true])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[false, true]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [(char)'A', (char)'B'])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[\'A\', \'B\']') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [(byte)1, (byte)2])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(Byte)1, (Byte)2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [(short)1, (short)2])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(Short)1, (Short)2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [1, 2])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[1, 2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [1L, 2L])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(Long)1, (Long)2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [1.1F, 2.2F])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(Float)1.1, (Float)2.2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [1.1D, 2.2D])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(Double)1.1, (Double)2.2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [new BigDecimal("1.1"), new BigDecimal("2.2")])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(BigDecimal)1.1, (BigDecimal)2.2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', ["A", "B"])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)["A", "B"]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [new java.util.Date(1234), new java.util.Date(2345)])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(java.util.Date)1970-01-01 09:00:01.234+09:00, (java.util.Date)1970-01-01 09:00:02.345+09:00]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [new Date(0), new Date(24*60*60*1000L)])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[1970-01-01+09:00, 1970-01-02+09:00]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [new Time(1234), new Time(2345)])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[09:00:01.234+09:00, 09:00:02.345+09:00]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [
                {def t = new Timestamp(1000); t.setNanos(234567890); return t}(),
                {def t = new Timestamp(2000); t.setNanos(345678901); return t}()
            ])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[1970-01-01 09:00:01.234567890+09:00, 1970-01-01 09:00:02.345678901+09:00]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [LocalDate.of(2018, 7, 29), LocalDate.of(2018, 7, 30)])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(LocalDate)2018-07-29, (LocalDate)2018-07-30]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [LocalTime.of(1, 23, 45, 678901234), LocalTime.of(12, 34, 56, 789012345)])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(LocalTime)01:23:45.678901234, (LocalTime)12:34:56.789012345]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [LocalDateTime.of(2018, 7, 29, 1, 23, 45, 678901234), LocalDateTime.of(2018, 7, 30, 12, 34, 56, 789012345)])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(LocalDateTime)2018-07-29 01:23:45.678901234, (LocalDateTime)2018-07-30 12:34:56.789012345]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [
                OffsetDateTime.of(2018, 7, 29,  1, 23, 45, 678901234, ZoneOffset.ofHours(-9)),
                OffsetDateTime.of(2018, 7, 30, 12, 34, 56, 789012345, ZoneOffset.ofHours(9))
            ])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(OffsetDateTime)2018-07-29 01:23:45.678901234-09:00, (OffsetDateTime)2018-07-30 12:34:56.789012345+09:00]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [
                ZonedDateTime.of(2018, 7, 29,  1, 23, 45, 678901234, ZoneId.of("GMT-9")),
                ZonedDateTime.of(2018, 7, 30, 12, 34, 56, 789012345, ZoneId.of("Asia/Tokyo"))
            ])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(ZonedDateTime)2018-07-29 01:23:45.678901234-09:00 GMT-09:00, (ZonedDateTime)2018-07-30 12:34:56.789012345+09:00 Asia/Tokyo]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [Instant.ofEpochSecond(123, 456789012), Instant.ofEpochSecond(234, 567890123)])
//    then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(Instant)1970-01-01 00:02:03.456789012Z, (Instant)1970-01-01 00:03:54.567890123Z') >= 0
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(Instant)1970-01-01 00:02:03.456789012, (Instant)1970-01-01 00:03:54.567890123') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [Fruits.APPLE, Fruits.ORANGE])
        then: DebugTrace.lastLog.indexOf('v = (ArrayList)[(....DebugTraceSpec.Fruits)APPLE, (....DebugTraceSpec.Fruits)ORANGE]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [1, 2, 3, 4, 5], options)
        then:
            DebugTrace.lastLog.indexOf('v = (ArrayList size:5)[1, 2, 3, 4, 5]') >= 0
            DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        cleanup:
        DebugTrace.leave()
    }

    // mapTypeNameSpec
    def mapTypeNameSpec() {
        setup:
        DebugTrace.enter()

        when: DebugTrace.print('v', [(false): true, (true): false])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[false: true, true: false]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [((char)'A'): (char)'B'])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[\'A\': \'B\']') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [((byte)1): (byte)2])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[(Byte)1: (Byte)2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [((short)1): (short)2])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[(Short)1: (Short)2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [1: 2])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[1: 2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [1L: 2L])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[(Long)1: (Long)2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [1.1F: 2.2F])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[(Float)1.1: (Float)2.2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [1.1D: 2.2D])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[(Double)1.1: (Double)2.2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', ["A": "B"])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)["A": "B"]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [(new BigDecimal("1.1")): new BigDecimal("2.2")])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[(BigDecimal)1.1: (BigDecimal)2.2]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [(new java.util.Date(1234)): new java.util.Date(2345)])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[(java.util.Date)1970-01-01 09:00:01.234+09:00: (java.util.Date)1970-01-01 09:00:02.345+09:00]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [(new Date(0)): new Date(24*60*60*1000L)])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[1970-01-01+09:00: 1970-01-02+09:00]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [(new Time(1234)): new Time(2345)])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[09:00:01.234+09:00: 09:00:02.345+09:00]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [
                ({def t = new Timestamp(1000); t.setNanos(234567890); return t}()):
                {def t = new Timestamp(2000); t.setNanos(345678901); return t}()
            ])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[1970-01-01 09:00:01.234567890+09:00: 1970-01-01 09:00:02.345678901+09:00]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [(Fruits.APPLE): Fruits.ORANGE])
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap)[(....DebugTraceSpec.Fruits)APPLE: (....DebugTraceSpec.Fruits)ORANGE]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        when: DebugTrace.print('v', [1: 'A', 2: 'AB', 3: 'ABC', 4: 'ABCD', 5: 'ABCDE'], options)
        then: DebugTrace.lastLog.indexOf('v = (LinkedHashMap size:5)[1: "A", 2: "AB", 3: "ABC", 4: "ABCD", 5: (length:5)"ABCDE"]') >= 0
              DebugTrace.lastLog.indexOf(commonSuffix) >= 0

        cleanup:
        DebugTrace.leave()
    }

    // bytesSpec
    def bytesSpec() {
        setup:
        DebugTrace.enter()

        when: DebugTrace.print('v', [] as byte[])
        then: DebugTrace.lastLog.indexOf('v = (byte[0])[]') >= 0
 
        when: DebugTrace.print('v', [0] as byte[])
        then: DebugTrace.lastLog.indexOf('v = (byte[1])[00  .]') >= 0
 
        when: DebugTrace.print('v', [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14] as byte[])
        then: DebugTrace.lastLog.indexOf('v = (byte[15])[00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E  ...............]') >= 0
 
        when: DebugTrace.print('v', [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15] as byte[])
        then: DebugTrace.lastLog.indexOf('v = (byte[16])[00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F  ................]') >= 0

        when: DebugTrace.print('v', [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16] as byte[])
        then: DebugTrace.lastLog.indexOf(
                '| v = (byte[17])[\n' +
                '|   0000 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F  ................\n' +
                '|   0010 10                                               .\n' +
                '| ]') >= 0


        when: DebugTrace.print('v', [
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 
                0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 
                0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F, 
                0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F, 
                0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F, 
                0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F, 
                0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F, 
                0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F, 
                0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F, 
                0xA0, 0xA1, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF, 
                0xB0, 0xB1, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF, 
                0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF, 
                0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF, 
                0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF, 
                0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF, 
                0x00
            ] as byte[], options)
        then: DebugTrace.lastLog
            .indexOf(
                '| v = (byte[257])[\n' +
                '|   0000 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F  ................\n' +
                '|   0010 10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F  ................\n' +
                '|   0020 20 21 22 23 24 25 26 27 28 29 2A 2B 2C 2D 2E 2F   !"#$%&\'()*+,-./\n' +
                '|   0030 30 31 32 33 34 35 36 37 38 39 3A 3B 3C 3D 3E 3F  0123456789:;<=>?\n' +
                '|   0040 40 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F  @ABCDEFGHIJKLMNO\n' +
                '|   0050 50 51 52 53 54 55 56 57 58 59 5A 5B 5C 5D 5E 5F  PQRSTUVWXYZ[\\]^_\n' +
                '|   0060 60 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F  `abcdefghijklmno\n' +
                '|   0070 70 71 72 73 74 75 76 77 78 79 7A 7B 7C 7D 7E 7F  pqrstuvwxyz{|}~.\n' +
                '|   0080 80 81 82 83 84 85 86 87 88 89 8A 8B 8C 8D 8E 8F  ................\n' +
                '|   0090 90 91 92 93 94 95 96 97 98 99 9A 9B 9C 9D 9E 9F  ................\n' +
                '|   00A0 A0 A1 A2 A3 A4 A5 A6 A7 A8 A9 AA AB AC AD AE AF  ................\n' +
                '|   00B0 B0 B1 B2 B3 B4 B5 B6 B7 B8 B9 BA BB BC BD BE BF  ................\n' +
                '|   00C0 C0 C1 C2 C3 C4 C5 C6 C7 C8 C9 CA CB CC CD CE CF  ................\n' +
                '|   00D0 D0 D1 D2 D3 D4 D5 D6 D7 D8 D9 DA DB DC DD DE DF  ................\n' +
                '|   00E0 E0 E1 E2 E3 E4 E5 E6 E7 E8 E9 EA EB EC ED EE EF  ................\n' +
                '|   00F0 F0 F1 F2 F3 F4 F5 F6 F7 F8 F9 FA FB FC FD FE FF  ................\n' +
                '|   0100 00                                               .\n' +
                '| ]') >= 0

         cleanup:
         DebugTrace.leave()
    }

    // printStackSpec
    def printStackSpec() {
        setup:
        DebugTrace.enter()
 
        when: func1()
        then:
        DebugTrace.lastLog.contains('func3')
        DebugTrace.lastLog.contains('func2')
        DebugTrace.lastLog.contains('func1')

        cleanup:
        DebugTrace.leave()
    }

    private void func1() {
        func2()
    }

    private void func2() {
        func3()
    }

    private void func3() {
        DebugTrace.printStack(10)
    }

    private class ThrowExceptiuon {
        @Override
        public String toString() {
            throw new RuntimeException('Point');
        }
    }

    // 3.5.2
    def "toString method throws Exception"() {
        setup:
        DebugTrace.enter()
 
        when:
        def foo = new ThrowExceptiuon()
        DebugTrace.print('foo', foo)

        then:
        DebugTrace.lastLog.contains('java.lang.RuntimeException: Point')

        cleanup:
        DebugTrace.leave()
    }
}
