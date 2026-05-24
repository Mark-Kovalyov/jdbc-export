package mayton.bigdata.formatters;

import mayton.bigdata.JdbcExportException;
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
import java.util.Map;

@SuppressWarnings("java:S1135")
public class ParquetFormatter implements ExportFormatter{

    static Logger logger = LoggerFactory.getLogger("parquet-formatter");

    @Override
    public void export(ResultSet rs, String query, int columnCount, String[] columnNames, String[] columnTypes,
                       String path, Map<String, String> props) throws JdbcExportException {

        Path outputPath   = new Path(path);
        String recordName = props.getOrDefault("recordname", "");
        String nameSpace  = props.getOrDefault("namespace", "");

        logger.info("recordName = {}, namespace = {}", recordName, nameSpace);

        SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder
                .record(recordName)
                .namespace(nameSpace).fields();

        for(int i = 1 ; i <= columnCount ; i++) {
            switch (columnTypes[i]) {
                case "TEXT", "CHARACTER VARYING" -> fields.optionalString(columnNames[i]);
                case "INT", "INTEGER", "NUMERIC" -> fields.optionalInt(columnNames[i]);
                case "BIGINT"                    -> fields.optionalLong(columnNames[i]);
                case "REAL", "DOUBLE PRECISION"  -> fields.optionalDouble(columnNames[i]); // TODO: What about float?
                case "BLOB"                      -> fields.optionalBytes(columnNames[i]); // TODO: Need to check deeper
                default -> throw new JdbcExportException("Unable to handle type " + columnTypes[i]);
            }
        }

        Schema schema = fields.endRecord();

        CompressionCodecName codec = CompressionCodecName.UNCOMPRESSED;

        if (props.containsKey("compression")) {
            String comp = props.get("compression");
            logger.info("received compression = {}", comp);
            switch (comp.toUpperCase()) {
                case "SNAPPY"          -> codec = CompressionCodecName.SNAPPY;
                case "GZIP"            -> codec = CompressionCodecName.GZIP;
                case "LZO"             -> codec = CompressionCodecName.LZO;
                case "BROTLI"          -> codec = CompressionCodecName.BROTLI;
                case "ZSTD"            -> codec = CompressionCodecName.ZSTD;
                case "UNCOMPRESSED"    -> codec = CompressionCodecName.UNCOMPRESSED;
                default -> throw new JdbcExportException("Unsupported compression codec: " + comp);
            }
        }

        try(ParquetWriter<GenericRecord> writer =
                // TODO: deprecated will be removed in 2.0.0; use {@link #builder(OutputFile)} instead.
                AvroParquetWriter.<GenericRecord>builder(outputPath)
                        .withSchema(schema)
                        .withConf(new Configuration())
                        .withCompressionCodec(codec)
                        .build()) {

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
            }

            logger.info("There are {} records written to Parquet file", rs.getRow());

        } catch (Exception ex) {
            throw new JdbcExportException("Error writing Parquet file: " + ex.getMessage(), ex);
        }


    }
}
