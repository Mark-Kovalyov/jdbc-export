# Memory tests and Time test

| Output Formatter                            | Xmx  | Elapsed Time (sec) |
|---------------------------------------------|------|--------------------|
| null                                        | 128M | 4                  |
| CSV                                         | 128M | 25                 |
| XML                                         | 128M | 30                 |
| JSONL(Jsonitier)                            | 2G   | 18                 |
| JSONL(fasterxml.jackson.core.JsonGenerator) | 128M | 24                 |
| AVRO                                        | 128M | 35                 |
| Parquet                                     | 256M | 28                 |
| Protobuf                                    | 128M | 60                 |

```sh
#!/bin/bash

java -Xmx128m \
 -jar bin/jdbc-export.jar \
 --url "jdbc:postgresql://localhost:5432/bigdata?user=enot&password=$PWD" \
 -q "select * from torrent_names" \
 -f null \
 -o /dev/null

java -Xmx2G \
 -jar bin/jdbc-export.jar \
 --url "jdbc:postgresql://localhost:5432/bigdata?user=enot&password=$PWD" \
 -q "select * from torrent_names" \
 -f jsonl \
 -o out.jsonl

java -Xmx128m \
 -jar bin/jdbc-export.jar \
 --url "jdbc:postgresql://localhost:5432/bigdata?user=enot&password=$PWD" \
 -q "select * from torrent_names" \
 -f jsonl-jackson \
 -o out-jackson.jsonl

 wc -l out.jsonl
 wc -l out-jackson.jsonl
```