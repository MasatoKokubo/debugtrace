# DebugTrace-java

link:README.asciidoc[[English]]
[English](README.md)

*DebugTrace-java* は、Javaプログラムのデバッグ時にトレースログを出力するライブラリです。<br>
メソッドの開始と終了箇所に `DebugTrace.enter()` および `DebugTrace.leave()` を埋め込む事で、開発中のJavaプログラムの実行状況をログに出力する事ができます。

|DebugTrace-javaのバージョン|サポートするJavaのバージョン
|:------------------------|:-----------------------
|DebugTrace-java 4.x.x    |Java 17以降
|DebugTrace-java 3.x.x    |Java 8以降

### 1. <small>特徴</small>

* 呼び出し元のクラス名、メソッド名、ソースファイルおよび行番号を自動的に出力。
* メソッドやオブジェクトのネストで、ログを自動的にインデント。
* スレッドの切り替え時に自動的にログを出力。
* `toString` メソッドを実装していないクラスのオブジェクトでもリフレクションを使用して内容を出力。
* `DebugTrace.properties` で、出力内容のカスタマイズが可能。
* 実行時に依存するライブラリがない。(下記ロギング・ライブラリを使用する場合は必要)
* 各種ロギング・ライブラリを使用可能。
  * コンソール (stdoutおよびstderr)
  * https://docs.oracle.com/javase/jp/8/docs/api/java/util/logging/Logger.html[JDKロガー]
  * http://logging.apache.org/log4j/1.2/[Log4j]
  * https://logging.apache.org/log4j/2.x/[Log4j2]
  * http://www.slf4j.org/[SLF4J]
  * 直接ファイル出力

### 2. <small>使用方法</small>

デバッグ対象および関連するメソッドに対して以下を挿入します。

* `DebugTrace.enter();`
* `DebugTrace.leave();`
* `DebugTrace.print("value", value);`

#### (1) メソッドが例外をスローせず、途中でリターンしない場合

```java
public void foo() {
    DebugTrace.enter(); // TODO: Debug
    ...
    DebugTrace.print("value", value); // TODO: Debug
    ...
    DebugTrace.leave(); // TODO: Debug
}
```

#### (2) メソッドが例外をスローしないが、途中でリターンがある場合

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

#### (3) メソッドが例外をスローする場合

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

以下は、DebugTrace-javaのメソッドを使用したJavaの例とそれを `args = [3]` で実行した際のログです。

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

[source,log]
.debugtrace.log
```log
2025-07-19 18:34:40.899+09:00 DebugTrace 4.1.2 on Amazon.com Inc. OpenJDK Runtime Environment 17.0.15+6-LTS
2025-07-19 18:34:40.907+09:00   property name: DebugTrace.properties
2025-07-19 18:34:40.916+09:00   logger: org.debugtrace.logger.File (character set: UTF-8, line separator: \n, file: Z:\logs\debugtrace.log)
2025-07-19 18:34:40.918+09:00   time zone: Asia/Tokyo
2025-07-19 18:34:40.924+09:00 
2025-07-19 18:34:40.928+09:00 <i>_____________________________ main _____________________________</i>
2025-07-19 18:34:40.930+09:00 
2025-07-19 18:34:40.933+09:00 Enter example.Example2.main (Example2.java:18) <- (:0)
2025-07-19 18:34:40.935+09:00 | Enter example.Example2.fibonacci (Example2.java:33) <- (Example2.java:23)
2025-07-19 18:34:40.937+09:00 | | Enter example.Example2.fibonacci (Example2.java:33) <- (Example2.java:41)
2025-07-19 18:34:40.961+09:00 | | | mapped fibonacci(1) = (long)1 (Example2.java:39)
2025-07-19 18:34:40.964+09:00 | | Leave example.Example2.fibonacci (Example2.java:48) duration: 00:00:00.024
2025-07-19 18:34:40.966+09:00 | | 
2025-07-19 18:34:40.968+09:00 | | Enter example.Example2.fibonacci (Example2.java:33) <- (Example2.java:41)
2025-07-19 18:34:40.970+09:00 | | | Enter example.Example2.fibonacci (Example2.java:33) <- (Example2.java:41)
2025-07-19 18:34:40.972+09:00 | | | | mapped fibonacci(0) = (long)0 (Example2.java:39)
2025-07-19 18:34:40.977+09:00 | | | Leave example.Example2.fibonacci (Example2.java:48) duration: 00:00:00.002
2025-07-19 18:34:40.979+09:00 | | | 
2025-07-19 18:34:40.981+09:00 | | | Enter example.Example2.fibonacci (Example2.java:33) <- (Example2.java:41)
2025-07-19 18:34:40.983+09:00 | | | | mapped fibonacci(1) = (long)1 (Example2.java:39)
2025-07-19 18:34:40.985+09:00 | | | Leave example.Example2.fibonacci (Example2.java:48) duration: 00:00:00.001
2025-07-19 18:34:40.987+09:00 | | | fibonacci(2) = (long)1 (Example2.java:42)
2025-07-19 18:34:40.989+09:00 | | Leave example.Example2.fibonacci (Example2.java:48) duration: 00:00:00.018
2025-07-19 18:34:40.991+09:00 | | fibonacci(3) = (long)2 (Example2.java:42)
2025-07-19 18:34:40.992+09:00 | Leave example.Example2.fibonacci (Example2.java:48) duration: 00:00:00.055
2025-07-19 18:34:40.998+09:00 | 
2025-07-19 18:34:41.000+09:00 | fibonacciMap = (HashMap)[
2025-07-19 18:34:41.002+09:00 |   (Long)0: (Long)0, (Long)1: (Long)1, (Long)2: (Long)1, (Long)3: (Long)2
2025-07-19 18:34:41.004+09:00 | ] (Example2.java:26)
2025-07-19 18:34:41.006+09:00 | 
2025-07-19 18:34:41.008+09:00 Leave example.Example2.main (Example2.java:29) duration: 00:00:00.072
```

### 3. <small>メソッド一覧</small>

このライブラリには以下のメソッドがあります。すべて
<a href=http://masatokokubo.github.io/debugtrace/javadoc/org/debugtrace/DebugTrace.html>`org.debugtrace.DebugTrace`</a>
クラスの静的メソッドです。

<table>
  <caption>メソッド一覧<caption>
  <tr>
    <th>メソッド名</th><th>引 数</th><th>戻り値</th><th>説 明</th>
  </tr>
  <tr>
    <td><code>enter</code></td>
    <td><i>なし</i></td>
    <td><i>なし</i></td>
    <td>メソッドの開始をログに出力する</td>
  </tr>
  <tr>
    <td><code>leave</code></td>
    <td><i>なし</i></td>
    <td><i>なし</i></td>
    <td>メソッドの終了をログに出力する</td>
  </tr>
  <tr>
    <td><code>print</code></td>
    <td><code>message</code>: メッセージ</td>
    <td><code>message</code><br></td>
    <td>メッセージをログに出力する</td>
  </tr>
  <tr>
    <td><code>print</code></td>
    <td><code>messageSupplier</code>: メッセージのサプライヤー</td>
    <td><code>messageSupplier</code> から取得したメッセージ</td>
    <td>サプライヤーからメッセージを取得してログに出力する</td>
  </tr>
  <tr>
    <td><code>print</code></td>
    <td>
      <code>name</code>: 値の名前<br>
      <code>value</code>: 値
    </td>
    <td><code>value</code><br></td>
    <td>
      <code><値の名前> = <値></code><br>
      の形式でログに出力する<br>
      <code>value</code> のタイプは以下のいずれか<br>
      <code>boolean</code>, <code>char</code>,<br>
      <code>byte</code>, <code>short</code>, <code>int</code>, <code>long</code>,<br>
      <code>float</code>, <code>double</code>, <code>T</code>
    </td>
  </tr>
  <tr>
    <td><code>print</code></td>
    <td>
      <code>name</code>: 値の名前<br>
      <code>value</code>: 値<br>
      <code>logOptions</code>: <a href="http://masatokokubo.github.io/debugtrace/javadoc/org/debugtrace/LogOptions.html">LogOptions</a><br>
      以下のフィールドを指定可能<br>
      <code>minimumOutputSize</code>,<br>
      <code>minimumOutputLength</code>,<br>
      <code>collectionLimit</code>,<br>
      <code>byteArrayLimit</code>,<br>
      <code>stringLimit</code>,<br>
      <code>reflectionNestLimit</code><br>
      または以下を指定可能<br>
      <code>LogOptions.outputSize</code><br>
      <code>LogOptions.outputLength</code>
    </td>
    <td><code>value</code><br></td>
    <td>同上<br></td>
  </tr>
  <tr>
    <td><code>print</code></td>
    <td>
      <code>name</code>: 値の名前<br>
      <code>valueSupplier</code>: 値のサプライヤー
    </td>
    <td><code>valueSupplier</code> から取得した値</td>
    <td>
      <code>valueSupplier</code> から値を取得して<br>
      <code><値の名前> = <値></code><br>
      の形式でログに出力する<br>
      <code>valueSupplier</code> のタイプは以下のいずれか<br>
      <code>BooleanSupplier</code>,<br>
      <code>IntSupplier</code>, <code>LongSupplier</code><br>
      <code>Supplier&lt;T&gt;</code>
    </td>
  </tr>
  <tr>
    <td><code>print</code></td>
    <td>
      <code>name</code>: 値の名前<br>
      <code>valueSupplier</code>: 値のサプライヤー<br>
      <code>logOptions</code>: <a href="http://masatokokubo.github.io/debugtrace/javadoc/org/debugtrace/LogOptions.html">LogOptions</a><br>
      <small><i>詳細は上を参照</i></small>
    </td>
    <td><code>valueSupplier</code> から取得した値</td>
    <td>同上</td>
  </tr>
  <tr>
    <td><code>printStack</code><br></td>
    <td><code>maxCount</code>: 出力するスタックトレース要素の最大数</td>
    <td><i>なし</i></td>
    <td>スタックトレース要素のリストを出力する</td>
  </tr>
</table>

### 4. DebugTrace.properties<small>#ファイルのプロパティ#</small>

DebugTrace は、クラスパスにある `DebugTrace.properties` ファイルを起動時に読み込みます。
`DebugTrace.properties` ファイルには以下のプロパティを指定できます。

<table>
  <caption>プロパティ一覧</caption>
  <tr>
    <td>プロパティ名|説明</td>
  </tr>
  <tr>
    <td><code>logger</code></td>
    <td>
      DebugTrace が使用するロガー<br>
      <small><b>指定可能な値</b></small>
      <ul>
        <code>Std$Out</code> ➔ stdout へ出力<br>
        <code>Std$Err</code> ➔ stderr へ出力
        <code>Jdk</code> ➔ JDKロガー を使用<br>
        <code>Log4j</code> ➔ Log4j を使用<br>
        <code>Log4j2</code> ➔ Log4j2 を使用<br>
        <code>SLF4J</code> ➔ SLF4J を使用<br>
        <code>File: [[文字セット][/行セパレータ]:] <ログファイルパス></code> ➔ ファイルに出力<br>
        <code>File: [[文字セット][/行セパレータ]:] +<ログファイルパス></code> ➔ ファイルに追加出力
      </ul>
      <ul>
        <code>文字セット</code> ::= <code>UTF-8</code> \| <code>Shift_JIS</code> \| ...<br>
        <code>行セパレータ</code> ::= <code>lf</code> \| <code>cr</code> \| <code>crlf</code>
      </ul>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>Std$Err</code>
      </ul>
      <small><b>例:</b></small>
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
      メソッドに入る際に出力するログのフォーマット文字列<br>
      <small><b>パラメータ:</b></small>
      <ul>
        <code>%1</code>: クラス名<br>
        <code>%2</code>: メソッド名<br>
        <code>%3</code>: ファイル名<br>
        <code>%4</code>: 行番号<br>
        <code>%6</code>: 呼び出し元のファイル名<br>
        <code>%7</code>: 呼び出し元の行番号
      </ul>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>Enter %1$s.%2$s (%3$s:%4$d) <- (%6$s:%7$d)</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>leaveFormat</code></td>
    <td>
      メソッドから出る際のログ出力のフォーマット文字列<br>
      <ul>
        <small><b>パラメータ:</b></small>
        <code>%1</code>: クラス名<br>
        <code>%2</code>: メソッド名<br>
        <code>%3</code>: ファイル名<br>
        <code>%4</code>: 行番号<br>
        <code>%5</code>: 対応する <code>enter</code> メソッドを呼び出してからの経過時間
      </ul>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>Leave %1$s.%2$s (%3$s:%4$d) duration: %5$tT.%5$tL</code>
      <ul>
    </td>
  </tr>
  <tr>
    <td><code>threadBoundaryFormat</code></td>
    <td>
      スレッド境界のログ出力の文字列フォーマット<br>
      <small><b>パラメータ:</b></small>
      <ul>
        <code>%1</code>: スレッド名
      </ul>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>_____________________________ %1$s _____________________________</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>classBoundaryFormat</code></td>
    <td>
      クラス境界のログ出力の文字列フォーマット<br>
      <small><b>パラメータ:</b></small>
      <ul>
        <code>%1</code>: クラス名
      </ul>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>___ %1$s ___</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>indentString</code></td>
    <td>
      コードのインデント文字列<br>
      <br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>|\\s</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>dataIndentString</code></td>
    <td>
      データのインデント文字列<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>\\s\\s</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>limitString</code></td>
    <td>
      制限を超えた場合に出力する文字列<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>\...</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>nonOutputString</code></td>
    <td>
      値を出力しない場合に代わりに出力する文字列<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>***</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>cyclicReferenceString</code></td>
    <td>
      循環参照している場合に出力する文字列<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>*** cyclic reference ***</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>varNameValueSeparator</code></td>
    <td>
      変数名と値のセパレータ文字列<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>\\s=\\s</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>keyValueSeparator</code></td>
    <td>
      マップのキーと値のおよびフィールド名と値のセパレータ文字列<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>:\\s</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>printSuffixFormat</code><br>
    <td>
      <code>print</code> メソッドで付加される文字列のフォーマット<br>
      <small><b>パラメータ:</b></small>
      <ul>
        <code>%1</code>: 呼出側のクラス名<br>
        <code>%2</code>: 呼出側のメソッド名<br>
        <code>%3</code>: 呼出側のファイル名<br>
        <code>%4</code>: 呼出側の行番号
      </ul>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>\\s(%3$s:%4$d)</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>sizeFormat</code></td>
    <td>
      コレクションおよびマップの要素数のフォーマット<br>
      <small><b>パラメータ:</b></small>
      <ul>
        <code>%1</code>: 要素数
      </ul>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>size:%1d</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>minimumOutputSize</code></td>
    <td>
      配列、コレクションおよびマップの要素数を出力する最小値<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>Integer.MAX_VALUE</code> <small><i>(出力しない)</i></small>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>lengthFormat</code></td>
    <td>
      文字列長のフォーマット<br>
      <small><b>パラメータ:</b></small>
      <ul>
        <code>%1</code>: 文字列長
      </ul>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>length:%1d</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>minimumOutputLength</code></td>
    <td>
      文字列長を出力する最小値<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>Integer.MAX_VALUE</code> <small>(出力しない)</small>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>utilDateFormat</code></td>
    <td>
      <code>java.util.Date</code> のフォーマット<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>sqlDateFormat</code></td>
    <td>
      <code>java.sql.Date</code> のフォーマット<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>yyyy-MM-ddxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>timeFormat</code></td>
    <td>
      <code>java.sql.Time</code> のフォーマット<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>HH:mm:ss.SSSxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>timestampFormat</code></td>
    <td>
      <code>java.sql.Timestamp</code> のフォーマット<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSSSSSSSxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>localDateFormat</code></td>
    <td>
      <code>java.time.LocalDate</code> のフォーマット<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>yyyy-MM-dd</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>localTimeFormat</code></td>
    <td>
      <code>java.time.LocalTime</code> のフォーマット<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>HH:mm:ss.SSSSSSSSS</code>
      <ul>
    </td>
  </tr>
  <tr>
    <td><code>offsetTimeFormat</code></td>
    <td>
      <code>java.time.OffsetTime</code> のフォーマット<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>HH:mm:ss.SSSSSSSSSxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>localDateTimeFormat</code></td>
    <td>
      <code>java.time.LocalDateTime</code> のフォーマット<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSSSSSSS</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>offsetDateTimeFormat</code></td>
    <td>
      <code>java.time.OffsetDateTime</code> のフォーマット<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSSSSSSSxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>zonedDateTimeFormat</code></td>
    <td>
      <code>java.time.ZonedDateTime</code> のフォーマット<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSSSSSSSxxx VV</code>
      </ul>
  </tr>
  <tr>
    <td><code>instantFormat</code></td>
    <td>
      <code>java.time.Instant</code> のフォーマット<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSSSSSSSX</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>logDateTimeFormat</code></td>
    <td>
      <code>logger</code> が <code>Std$Out</code> および <code>Std$Err</code> の場合のログの日時のフォーマット<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>yyyy-MM-dd HH:mm:ss.SSSxxx</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>timeZone</code></td>
    <td>
      タイムゾーンの指定 (<code>ZoneId.of(timeZone)</code>)<br>
      <small><b>例:</b></small>
      <ul>
        <code>timeZone = UTC</code><br>
        <code>timeZone = America/New_York</code><br>
        <code>timeZone = Asia/Tokyo</code>
      <ul>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>ZoneId.systemDefault()</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>maximumDataOutputWidth</code></td>
    <td>
      データの出力幅の最大値<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        70
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>collectionLimit</code></td>
    <td>
      配列、コレクションおよびマップの要素の出力数の制限値<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        128
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>byteArrayLimit</code></td>
    <td>
      バイト配列(<code>byte[]</code>)要素の出力数の制限値<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        256
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>stringLimit</code></td>
    <td>
      文字列の出力文字数の制限値<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        256
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>reflectionNestLimit</code></td>
    <td>
      リフレクションのネスト数の制限値<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        4
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>nonOutputProperties</code></td>
    <td>
      出力しないプロパティ名のリスト<br>
      <small><b>値のフォーマット:</b></small>
      <ul>
        <code>&lt;フルクラス名&gt;#&lt;プロパティ名&gt;</code>
      </ul>
      <small><b>デフォルト値:</b></small>
      <ul>
        なし
      </ul>
      <small><b>値の例 (1つ):</b></small>
      <ul>
        <small><code>org.lightsleep.helper.EntityInfo#columnInfos</code></small>
      </ul>
      <small><b>値の例 (複数):</b></small>
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
      使用する Javaソースのデフォルトパッケージ<br>
      <small><b>デフォルト値:</b></small>
      <ul>
         なし
      </ul>
      <small><b>値の例:</b></small>
      <ul>
        <code>org.debugtrace.DebugTraceExample</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>defaultPackageString</code></td>
    <td>
      デフォルトパッケージ部を置き換える文字列<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        <code>...</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>reflectionClasses</code></td>
    <td>
      <code>toString</code> メソッドを実装していてもリフレクションで内容を出力するクラス名または <code>パッケージ名 + '.'</code> のリスト<br>
      <small><b>デフォルト値:</b></small>
      <ul>
        なし
      </ul>
      <small><b>値の例 (1つ):</b></small>
      <ul>
        <code>org.debugtrce.example.Point</code>
      </ul>
      <small><b>値の例 (複数):</b></small>
      <ul>
        <code>org.debugtrace.example.Point,\</code><br>
        <code>org.debugtrace.example.Rectangle</code>
      </ul>
      <small><b>値の例 (パッケージ):</b></small>
      <ul>
        <code>org.debugtrce.example.</code>
      </ul>
    </td>
  </tr>
  <tr>
    <td><code>mapNameMap</code></td>
    <td>
      変数名に対応するマップ名を取得するためのマップ<br>
      <small><b>値のフォーマット:</b></small>
      <ul>
        <code>&lt;変数名&gt;: &lt;マップ名&gt;</code>
      </ul>
      <small><b>デフォルト値:</b></small>
      <ul>
        なし
      </ul>
      <small><b>値の例:</b></small>
      <ul>
        <code>appleBrand: AppleBrand</code>
      <ul>
    </td>
  </tr>
  <tr>
    <td><code>&lt;マップ名&gt;</code></td>
    <td>
      数値(key)と数値に対応する定数名(value)のマップ<br>
      <small><b>値のフォーマット:</b></small>
      <ul>
        <code>&lt;数値&gt;: &lt;定数名&gt;</code>
      </ul>
      <small><b>定義済み定数名マップ:</b></small>
      <ul>
        <code>Calendar</code>: <code>Calendar.ERA</code> など<br>
        <code>CalendarWeek</code>: <code>Calendar.SUNDAY</code> など<br>
        <code>CalendarMonth</code>: <code>Calendar.JANUARY</code> など<br>
        <code>CalendarAmPm</code>: <code>Calendar.AM</code> など<br>
        <code>SqlTypes</code>: <code>java.sql.Types.BIT</code> など
      </ul>
      <small><b>設定例:</b></small>
      <ul>
        <code>AppleBrand = \</code><br>
        &#xa0;&#xa0; <code>0: Apple.NO_BRAND,\</code><br>
        &#xa0;&#xa0; <code>1: Apple.AKANE,\</code><br>
        &#xa0;&#xa0; <code>2: Apple.AKIYO,\</code><br>
        &#xa0;&#xa0; <code>3: Apple.AZUSA,\</code><br>
        &#xa0;&#xa0; <code>4: Apple.YUKARI</code>
      </ul>
    </td>
  </tr>
</table>

上記で `\\s` は空白文字に置き換えられます。  
日時のフォーマットは、`DateTimeFormatter.ofPattern` メソッドの引数の形式で指定してください。

#### 4.1. *nonOutputProperties*, *nonOutputString*

DebugTrace は、 `toString` メソッドが実装されていない場合は、リフレクションを使用してオブジェクト内容を出力します。
他のオブジェクトの参照があれば、そのオブジェクトの内容も出力します。
ただし循環参照がある場合は、自動的に検出して出力を中断します。  
`nonOutputroperties` プロパティを指定して出力を抑制する事もできます。
このプロパティの値は、カンマ区切りで複数指定できます。  
`nonOutputProperties` で指定されたプロパティの値は、 `nonOutputString` で指定された文字列(デフォルト: `\***`)で出力されます。

nonOutputPropertiesの例 (DebugTrace.properties)
```
nonOutputProperties = \
    org.lightsleep.helper.EntityInfo#columnInfos,\
    org.lightsleep.helper.EntityInfo#keyColumnInfos,\
    org.lightsleep.helper.ColumnInfo#entityInfo
```

#### 4.2. <small>定数マップ</small>, *mapNameMap*

定数マップは、キーが数値で値が定数名のマップです。
変数名に対応するマップ名を `mapNameMap` プロパティで指定すると、数値に対応する定数名も出力されます。

.定数マップおよび mapNameMap の例 (DebugTrace.properties)
```properties
AppleBrand = \
    0: Apple.NO_BRAND,\
    1: Apple.AKANE,\
    2: Apple.AKIYO,\
    3: Apple.AZUSA,\
    4: Apple.YUKARI

mapNameMap = appleBrand:AppleBrand
```

Javaソースの例
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

Logの例
```log
2023-01-29 10:14:29.916+09:00 appleBrand = 1(Apple.AKANE) (ReadmeExample.java:18)
2023-01-29 10:14:29.916+09:00  2 appleBrand  = 2(Apple.AKIYO) (ReadmeExample.java:20)
2023-01-29 10:14:29.916+09:00  3 example.appleBrand  = 3(Apple.AZUSA) (ReadmeExample.java:22)
2023-01-29 10:14:29.916+09:00  4 example. appleBrand  = 4(Apple.YUKARI) (ReadmeExample.java:24)
```

### 5. <small>ロギング・ライブラリの使用例</small>

ロギング・ライブラリを使用する際のDebugTraceのロガー名は、 `org.debugtrace.DebugTrace` です。

#### 5-1. *logging.properties* (*JDK<small>標準</small>*) <small>の例</small>

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
*Java起動時オプションとして `-Djava.util.logging.config.file=<パス>/logging.properties` が必要*

#### 5-2. *log4j.xml* (*Log4j*)<small>の例</small>

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

#### 5-3. *log4j2.xml* (*Log4j2*) <small>の例</small>

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

#### 5-4. *logback.xml* (*SLF4J*/*Logback*) <small>の例</small>

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

### 6. *build.gradle* <small>の記述例</small>

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

### 7. <small>ライセンス</small>

[MIT ライセンス(MIT)](LICENSE.txt)

<i style="color:gray">(C) 2015 Masato Kokubo</i>

### 8. <small>リンク</small>

[API仕様(英語)](http://masatokokubo.github.io/debugtrace/javadoc/index.html)

### 9. <small>リリースノート</small>

[リリース](https://github.com/MasatoKokubo/debugtrace/releases)
