package mkovalev.bigdata.formatters;

import mkovalev.bigdata.JdbcExportException;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Map;

@SuppressWarnings("java:S1135")
public class ParquetFormatter implements ExportFormatter {

    static Logger logger = LoggerFactory.getLogger("parquet-formatter");

    @Override
    public long export(ResultSet rs, String query, int columnCount, String[] columnNames, String[] columnTypes,
                       String path, Map<String, String> props) throws JdbcExportException {

        long rows = 0;
        String hadoopVersion = org.apache.hadoop.util.VersionInfo.getVersion();
        logger.info("Hadoop version    = {}", hadoopVersion);
        logger.info("java.library.path = {}", System.getProperty("java.library.path"));

        Path outputPath = new Path(path);
        String recordName = props.getOrDefault("recordname", "defaultRecordName");
        String nameSpace = props.getOrDefault("namespace", "defaultNamespace");

        logger.info("recordName = {}, namespace = {}", recordName, nameSpace);

        SchemaBuilder.FieldAssembler<Schema> schemaBuilder = SchemaBuilder
                .record(recordName)
                .namespace(nameSpace).fields();

        logger.info("Configure schema");

        for (int i = 1; i <= columnCount; i++) {
            logger.info("Column {} ({})", columnNames[i], columnTypes[i]);
            switch (columnTypes[i]) {
                case "timestamp" -> schemaBuilder.optionalString(columnNames[i]);
                case "text", "TEXT", "CHARACTER VARYING" -> schemaBuilder.optionalString(columnNames[i]);
                case "int4", "INT", "INTEGER", "NUMERIC" -> schemaBuilder.optionalInt(columnNames[i]);
                case "int8", "BIGINT" -> schemaBuilder.optionalLong(columnNames[i]);
                case "float8", "REAL", "DOUBLE PRECISION" -> schemaBuilder.optionalDouble(columnNames[i]);
                case "BLOB" -> schemaBuilder.optionalBytes(columnNames[i]); // TODO: Need to check deeper
                default -> throw new JdbcExportException("Unable to handle type " + columnTypes[i]);
            }
        }

        Schema schema = schemaBuilder.endRecord();

        logger.info("Schema = {}", schema.toString());

        logger.info("Configure compression");

        CompressionCodecName codec = CompressionCodecName.UNCOMPRESSED;

        if (props.containsKey("compression")) {
            String comp = props.get("compression");
            logger.info("received compression = {}", comp);
            switch (comp.toUpperCase()) {
                case "SNAPPY" -> codec = CompressionCodecName.SNAPPY;
                case "GZIP" -> codec = CompressionCodecName.GZIP;
                case "LZO" -> codec = CompressionCodecName.LZO;
                case "BROTLI" -> codec = CompressionCodecName.BROTLI;
                case "ZSTD" -> codec = CompressionCodecName.ZSTD;
                case "UNCOMPRESSED" -> codec = CompressionCodecName.UNCOMPRESSED;
                default -> throw new JdbcExportException("Unsupported compression codec: " + comp);
            }
        }

        logger.info("Prepare parquet writer");

        try (ParquetWriter<GenericRecord> writer =
                     // TODO: deprecated will be removed in 2.0.0; use {@link #builder(OutputFile)} instead.
                     AvroParquetWriter.<GenericRecord>builder(outputPath)
                             .withSchema(schema)
                             .withConf(new Configuration())
                             .withCompressionCodec(codec)
                             .build()) {

            logger.info("Start GenericRecord produce");

            while (rs.next()) {
                GenericRecord genericRecord = new GenericData.Record(schema);
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    if (value != null) {
                        genericRecord.put(columnNames[i], value);
                        // TODO: Investigate for put by index is faster
                    }
                }
                writer.write(genericRecord);
                rows++;
            }


        } catch (Exception ex) {
            throw new JdbcExportException("Error writing Parquet file: " + ex.getMessage(), ex);
        }
        return rows;

    }
}
