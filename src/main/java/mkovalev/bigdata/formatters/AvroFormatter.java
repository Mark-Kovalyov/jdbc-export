package mkovalev.bigdata.formatters;

import mkovalev.bigdata.JdbcExportException;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Map;

@SuppressWarnings("java:S1135")
public class AvroFormatter implements ExportFormatter{

    static Logger logger = LoggerFactory.getLogger("avro-formatter");

    @SuppressWarnings("java:S2629")
    @Override
    public long export(ResultSet rs, String query, int columnCount, String[] columnNames, String[] columnTypes,
                       String path, Map<String,String> props) throws JdbcExportException {
        long rows = 0L;

        String recordName = props.getOrDefault("recordname", "defaultRecordName");
        String nameSpace = props.getOrDefault("namespace", "defaultNamespace");

        SchemaBuilder.FieldAssembler<Schema> fieldAssembler = SchemaBuilder
                .record(recordName) // TODO: Is it necessary to add namespace?
                .namespace(nameSpace)
                .fields();

        for(int i = 1 ; i <= columnCount ; i++) {
            switch (columnTypes[i]) {
                case "timestamp"                          -> fieldAssembler.optionalString(columnNames[i]);
                case "text", "TEXT", "CHARACTER VARYING"  -> fieldAssembler.optionalString(columnNames[i]);
                case "int4", "INT", "INTEGER", "NUMERIC"  -> fieldAssembler.optionalInt(columnNames[i]); // TODO: int or long?
                case "int8", "BIGINT"                     -> fieldAssembler.optionalLong(columnNames[i]);
                case "float8", "REAL", "DOUBLE PRECISION" -> fieldAssembler.optionalDouble(columnNames[i]);
                case "BLOB" -> fieldAssembler.optionalString(columnNames[i]); // TODO: Hex encoded?
                default -> throw new JdbcExportException("Unable to handle type " + columnTypes[i]);
            }
        }

        Schema schema = fieldAssembler.endRecord();

        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        try(DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter)) {
            if (props.containsKey("compression")) {
                String comp = props.get("compression");
                logger.info("compression = {}", comp);
                CodecFactory codec = CodecFactory.fromString(comp);
                logger.info("codec factory = {}", codec);
                dataFileWriter.setCodec(codec);
            }

            dataFileWriter.create(schema, new FileOutputStream(path));
            while (rs.next()) {
                GenericRecord tableRecord = new GenericData.Record(schema);
                for (int i = 1; i <= columnCount; i++) {
                    if (rs.getObject(i) != null) {
                        Object value = rs.getObject(i);
                        Class<? extends Object> objType = rs.getObject(i).getClass();
                        if (objType == Timestamp.class) {
                            Timestamp ts = rs.getTimestamp(i);
                            tableRecord.put(columnNames[i], ts.toLocalDateTime().toString());
                        } else {
                            tableRecord.put(columnNames[i], value);
                            // TODO: Investigate for put by index is faster
                        }
                    }
                }
                dataFileWriter.append(tableRecord);
                rows++;
            }
        } catch (Exception e) {
            throw new JdbcExportException("Avro export error: " + e.getMessage(), e);
        }

        return rows;

    }
}
