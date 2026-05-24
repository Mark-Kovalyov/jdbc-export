# PG tests: export CSV

## Run export script
```sh
$ java -jar jdbc-export.jar --url "jdbc:postgresql://localhost:5432/bigdata?user=enot&password=***" --format jsonl --outputfile torrent_names.jsonl -q "select * from torrent_names"
```

```sh
t$ head torrent_names.jsonl 
{"id":3407014,"category":205,"name":"Great Planes - aircraft documentary (24 episodes)","size":9.723810444E9,"last_update_time":"2026-05-24T18:48"}
{"id":3407015,"category":101,"name":"[NEW] VA - Mastermix 80\\'s (2005) - Dance","size":1.32077951E8,"last_update_time":"2026-05-24T18:48"}
{"id":3407016,"category":101,"name":"[NEW] VA - Mastermix In The Mood (2005) - Pop","size":1.62396496E8,"last_update_time":"2026-05-24T18:48"}
{"id":3407017,"category":301,"name":"SQL 2000","size":6.20411046E8,"last_update_time":"2026-05-24T18:48"}
{"id":3407018,"category":101,"name":"Looptroop-Signs_Of_The_Times-Bootleg-2005-SDR","size":9.9415235E7,"last_update_time":"2026-05-24T18:48"}
{"id":3407019,"category":202,"name":"Dolph.Og.Wulffmorgenthaler.Alle.Er.100.Procent.Assholes.2005.DAN","size":4.745910023E9,"last_update_time":"2026-05-24T18:48"}
{"id":3407021,"category":299,"name":"[Snowboard 2006] Jibbing With Jeremy Jones.SVCD","size":4.4681224E8,"last_update_time":"2026-05-24T18:48"}
{"id":3407022,"category":201,"name":"Saint.Ralph.Dvdrip.French.6","size":7.2881152E8,"last_update_time":"2026-05-24T18:48"}
{"id":3407023,"category":404,"name":"[Xbox] Star Wars Episode III - Revenge of the Sith - Pal - Confu","size":2.572865536E9,"last_update_time":"2026-05-24T18:48"}
{"id":3407025,"category":201,"name":"Rockers","size":4.649083002E9,"last_update_time":"2026-05-24T18:48"}
```