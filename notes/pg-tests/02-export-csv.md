# PG tests: export CSV

## Given:
```
bigdata=> select count(*) from torrent_names;
  count  
---------
 4703192
(1 row)

bigdata=> \d+ torrent_names 
                                                     Table "public.torrent_names"
      Column      |            Type             | Collation | Nullable | Default | Storage  | Compression | Stats target | Description 
------------------+-----------------------------+-----------+----------+---------+----------+-------------+--------------+-------------
 id               | bigint                      |           |          |         | plain    |             |              | 
 category         | integer                     |           |          |         | plain    |             |              | 
 name             | text                        |           |          |         | extended |             |              | 
 size             | double precision            |           |          |         | plain    |             |              | 
 last_update_time | timestamp without time zone |           |          |         | plain    |             |              | 


bigdata=> SELECT pg_size_pretty(pg_total_relation_size('torrent_names'));
 pg_size_pretty 
----------------
 1360 MB
(1 row)
```

## Run export script
```sh
$ java -jar jdbc-export.jar --url "jdbc:postgresql://localhost:5432/bigdata?user=enot&password=*****" --format csv --outputfile torrent_names.csv -q "select * from torrent_names"
[INFO ] jdbc-export - Start analyze schema
[INFO ] jdbc-export - Column 1: [null, null, null, null, null, null] (int8, JDBC type -5)
[INFO ] jdbc-export - Column 2: [null, int8, null, null, null, null] (int4, JDBC type 4)
[INFO ] jdbc-export - Column 3: [null, int8, int4, null, null, null] (text, JDBC type 12)
[INFO ] jdbc-export - Column 4: [null, int8, int4, text, null, null] (float8, JDBC type 8)
[INFO ] jdbc-export - Column 5: [null, int8, int4, text, float8, null] (timestamp, JDBC type 93)
[INFO ] jdbc-export - Start export
[INFO ] jdbc-export - Finish export
```

```sh
$ head torrent_names.csv
"id";"category";"name";"size";"last_update_time"
"3407014";"205";"Great Planes - aircraft documentary (24 episodes)";"9723810444";"2026-05-24 18:48:00"
"3407015";"101";"[NEW] VA - Mastermix 80\'s (2005) - Dance";"132077951";"2026-05-24 18:48:00"
"3407016";"101";"[NEW] VA - Mastermix In The Mood (2005) - Pop";"162396496";"2026-05-24 18:48:00"
"3407017";"301";"SQL 2000";"620411046";"2026-05-24 18:48:00"
"3407018";"101";"Looptroop-Signs_Of_The_Times-Bootleg-2005-SDR";"99415235";"2026-05-24 18:48:00"
"3407019";"202";"Dolph.Og.Wulffmorgenthaler.Alle.Er.100.Procent.Assholes.2005.DAN";"4745910023";"2026-05-24 18:48:00"
"3407021";"299";"[Snowboard 2006] Jibbing With Jeremy Jones.SVCD";"446812240";"2026-05-24 18:48:00"
"3407022";"201";"Saint.Ralph.Dvdrip.French.6";"728811520";"2026-05-24 18:48:00"
"3407023";"404";"[Xbox] Star Wars Episode III - Revenge of the Sith - Pal - Confu";"2572865536";"2026-05-24 18:48:00"
```

```sh
$ ls -lF torrent_names.csv 
-rw-rw-r-- 1 enot enot 464063147 May 24 19:24 torrent_names.csv
```