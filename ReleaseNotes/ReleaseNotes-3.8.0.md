
* Added `timeZone` property to `DebugTrace.properties`  
　  
_Examples_:  
`timeZone = UTC`  
`timeZone = America/New_York`  
`timeZone = Asia/Tokyo`

 * Added `character set` and `line separator type` specification to `File` logger.  
　  
_Examples_:  
`logger = File: /logs/debugtrace.log`  
`logger = File: UTF-8: /logs/debugtrace.log`  
`logger = File: UTF-8/lf: /logs/debugtrace.log`  
`logger = File: UTF-8/cr: /logs/debugtrace.log`  
`logger = File: UTF-8/crlf: /logs/debugtrace.log`  
`logger = File: /lf: /logs/debugtrace.log`  
`logger = File :Shift_JIS: /logs/debugtrace.log`  
`logger = File: EUC-JP: /logs/debugtrace.log`  

---
*Japanese*

* `DebugTrace.properties`に`timeZone`プロパティを追加  
　  
例:  
`timeZone = UTC`  
`timeZone = America/New_York`  
`timeZone = Asia/Tokyo`

 * `File`ロガーの指定に`文字セット`と`改行コード`の指定を追加  
　  
例:  
`logger = File: /logs/debugtrace.log`  
`logger = File: UTF-8: /logs/debugtrace.log`  
`logger = File: UTF-8/lf: /logs/debugtrace.log`  
`logger = File: UTF-8/cr: /logs/debugtrace.log`  
`logger = File: UTF-8/crlf: /logs/debugtrace.log`  
`logger = File: /lf: /logs/debugtrace.log`  
`logger = File :Shift_JIS: /logs/debugtrace.log`  
`logger = File: EUC-JP: /logs/debugtrace.log`  
