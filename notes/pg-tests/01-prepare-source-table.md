# PG tests: Prepare

## Pre requisities:

- Ubuntu https://ubuntu.com/
- Postgresql 16 https://www.postgresql.org/docs/16/index.html

## Given:

- OS : Linux Ubuntu 6.17.0-29-generic x86_64 x86_64 x86_64 GNU/Linux 
- Postgresql 16.14

## Load test data from external file into Postgresql Database

```sql

```


```sh
spark-shell --driver-memory 4G --executor-memory 3G --executor-cores 2

scala> val df = spark.read.format("json").load("/bigdata/db/tpb-2/tpb.json.bz2")
val df: org.apache.spark.sql.DataFrame = [category: string, description: string ... 4 more fields]

scala> df.printSchema
warning: 1 deprecation (since 2.13.3); for details, enable `:setting -deprecation` or `:replay -deprecation`
root
 |-- category: string (nullable = true)
 |-- description: string (nullable = true)
 |-- id: string (nullable = true)
 |-- infoHash: string (nullable = true)
 |-- name: string (nullable = true)
 |-- size: string (nullable = true)

```

## Load CSV file under Postgresql user (to allow file access)
```sql
$ psql -d bigdata
psql (16.14 (Ubuntu 16.14-0ubuntu0.24.04.1))
Type "help" for help.
  
DROP TABLE IF EXISTS  torrent_names;

CREATE TABLE torrent_names (
  id       bigint,
  category integer,
  name     text,
  size double precision
);
```
## Grant permission to application uses (optionally)
```sql
# grant all on public.torrent_names to enot;
GRANT
```

## Load
```sql
COPY torrent_names
    FROM '/bigdata/db/tpb-2/csv2/data.csv'
WITH (
    FORMAT csv,
    DELIMITER ',',
    NULL '',
    HEADER false,
    QUOTE '"',
    ESCAPE '\'
);
```

## Enrich table with DateTime fields (to cover all possible scenarios)

```sql
ALTER TABLE torrent_names ADD COLUMN last_update_time timestamp;

UPDATE torrent_names SET last_update_time = TIMESTAMP '2026-05-24 18:48:00';
```

## Check access
```sql
psql -U enot -d bigdata -h localhost
```

