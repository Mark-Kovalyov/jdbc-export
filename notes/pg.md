# Postgresql notes

## Postgresql numeric types

| Name            |Storage Size |Description |Range
|-----------------|-------------|------------|------
| smallint        |2 bytes |small-range integer |-32768 to +32767
| integer         |4 bytes |typical choice for integer |-2147483648 to +2147483647
| bigint          |8 bytes |large-range integer |-9223372036854775808 to +9223372036854775807
| decimal         |variable |user-specified precision, exact |up to 131072 digits before the decimal point; up to 16383 digits after the decimal point
| numeric         |variable |user-specified precision, exact |up to 131072 digits before the decimal point; up to 16383 digits after the decimal point
| real            |4 bytes |variable-precision, inexact |6 decimal digits precision
| double precision |8 bytes |variable-precision, inexact |15 decimal digits precision
| smallserial     |2 bytes |small autoincrementing integer |1 to 32767
| serial          |4 bytes |autoincrementing integer |1 to 2147483647
| bigserial       |8 bytes |large autoincrementing integer |1 to 9223372036854775807

## Memory issue (JDBC-driver?)

```azure
$ java -Xmx1G -jar bin/jdbc-export.jar --url "jdbc:postgresql://localhost:5432/bigdata?user=enot&password=****" --format xml --outputfile torrent_names.xml -q "select * from torrent_names"
[INFO ] jdbc-export - Props:
[INFO ] jdbc-export - Start analyze schema
[INFO ] jdbc-export - Column 1: (int8, JDBC type -5)
[INFO ] jdbc-export - Column 2: (int4, JDBC type 4)
[INFO ] jdbc-export - Column 3: (text, JDBC type 12)
[INFO ] jdbc-export - Column 4: (float8, JDBC type 8)
[INFO ] jdbc-export - Column 5: (timestamp, JDBC type 93)
[INFO ] jdbc-export - Start export
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at java.base/java.util.Arrays.copyOf(Arrays.java:3481)
	at java.base/java.util.ArrayList.grow(ArrayList.java:237)
	at java.base/java.util.ArrayList.grow(ArrayList.java:244)
	at java.base/java.util.ArrayList.add(ArrayList.java:454)
	at java.base/java.util.ArrayList.add(ArrayList.java:467)
	at org.postgresql.core.v3.QueryExecutorImpl.processResults(QueryExecutorImpl.java:2403)
	at org.postgresql.core.v3.QueryExecutorImpl.execute(QueryExecutorImpl.java:372)
	at org.postgresql.jdbc.PgStatement.executeInternal(PgStatement.java:518)
	at org.postgresql.jdbc.PgStatement.execute(PgStatement.java:435)
	at org.postgresql.jdbc.PgPreparedStatement.executeWithFlags(PgPreparedStatement.java:196)
	at org.postgresql.jdbc.PgPreparedStatement.executeQuery(PgPreparedStatement.java:139)
	at mkovalev.bigdata.JdbcExport.main(JdbcExport.java:109)
    
(base) enot@ryzen:/bigdata/git/jdbc-export$ java -Xmx2G -jar bin/jdbc-export.jar --url "jdbc:postgresql://localhost:5432/bigdata?user=enot&password=****" --format xml --outputfile torrent_names.xml -q "select * from torrent_names"
[INFO ] jdbc-export - Props:
[INFO ] jdbc-export - Start analyze schema
[INFO ] jdbc-export - Column 1: (int8, JDBC type -5)
[INFO ] jdbc-export - Column 2: (int4, JDBC type 4)
[INFO ] jdbc-export - Column 3: (text, JDBC type 12)
[INFO ] jdbc-export - Column 4: (float8, JDBC type 8)
[INFO ] jdbc-export - Column 5: (timestamp, JDBC type 93)
[INFO ] jdbc-export - Start export
[INFO ] xml-formatter - factory class created class com.ctc.wstx.stax.WstxOutputFactory
[INFO ] jdbc-export - Finish export
(base) enot@ryzen:/bigdata/git/jdbc-export$ 
```

```java
...
conn.setAutoCommit(false); // This is performance advice to reduce memory overhead
....
PreparedStatement pst = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
pst.setFetchSize(50);
ResultSet rs2 = pst.executeQuery();
```

```
$ java -Xmx128M -jar bin/jdbc-export.jar --url "jdbc:postgresql://localhost:5432/bigdata?user=enot&password=****" --format xml --outputfile torrent_names.xml -q "select * from torrent_names"
[INFO ] jdbc-export - Props:
[INFO ] jdbc-export - Start analyze schema
[INFO ] jdbc-export - Column 1: (int8, JDBC type -5)
[INFO ] jdbc-export - Column 2: (int4, JDBC type 4)
[INFO ] jdbc-export - Column 3: (text, JDBC type 12)
[INFO ] jdbc-export - Column 4: (float8, JDBC type 8)
[INFO ] jdbc-export - Column 5: (timestamp, JDBC type 93)
[INFO ] jdbc-export - Start export
[INFO ] xml-formatter - factory class created class com.ctc.wstx.stax.WstxOutputFactory
[INFO ] jdbc-export - Finish export
```

## Performance tests with different fetch sizes:

| Fetch size | Speed (rows/s)
|-----------|---------------
| 5000      |286 
| 500       |272 
| 50        |174

                