package mayton.bigdata;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ParquetTest {

    static String temp = null;

    static boolean isLinux = false;

    @BeforeAll
    static void init() {
        String osName = System.getProperty("os.name");
        isLinux = osName.equalsIgnoreCase("linux");
        temp = isLinux ? "/tmp" : "c:/tmp";
    }

    @Test
    void testWrite() throws IOException {
        if (isLinux) {
            Schema schema = SchemaBuilder.record("User")
                    .namespace("demo")
                    .fields()
                    .optionalInt("id")
                    .optionalString("name")
                    .optionalString("email")
                    .optionalInt("age")
                    .endRecord();

            String uuid = java.util.UUID.randomUUID().toString();

            Path outputPath = new Path(String.format(temp + "/jdbc-export/parquet/users-%s.parquet", uuid));

            // Create ParquetWriter
            try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                    .<GenericRecord>builder(outputPath)
                    .withSchema(schema)
                    .withConf(new Configuration())
                    .withCompressionCodec(CompressionCodecName.SNAPPY)
                    .build()) {

                // Create and write records
                for (int i = 1; i <= 5; i++) {
                    GenericRecord record = new GenericData.Record(schema);
                    record.put("id", i);
                    record.put("name", "User" + i);
                    record.put("email", "user" + i + "@example.com");
                    record.put("age", 20 + i);
                    writer.write(record);
                }

                System.out.println("Parquet file written successfully!");
            }
        } else {
            System.err.println("Warning! Linux OS is not detected. Test skipped");
        }
    }


}
