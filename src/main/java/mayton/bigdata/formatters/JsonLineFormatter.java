package mayton.bigdata.formatters;

import com.jsoniter.output.JsonStream;
import mayton.bigdata.JdbcExportException;
import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@SuppressWarnings("java:S1135")
public class JsonLineFormatter implements ExportFormatter {
    @Override
    public void export(ResultSet rs, String query, int columnCount, String[] columnNames, String[] columnTypes,
                       String path, Map<String,String> props) throws JdbcExportException {
        try (JsonStream stream = new JsonStream(new FileOutputStream(path), 4096)) { // TODO: Benchmark optimal buffer size
            while (rs.next()) {
                stream.writeObjectStart();
                for (int i = 1; i <= columnCount; i++) {
                    if (rs.getObject(i) != null) {
                        stream.writeObjectField(columnNames[i]);
                        // Sqlite:
                        //INTEGER. The value is a signed integer, stored in 0, 1, 2, 3, 4, 6, or 8 bytes depending on the magnitude of the value.
                        //REAL. The value is a floating point value, stored as an 8-byte IEEE floating point number.
                        //TEXT. The value is a text string, stored using the database encoding (UTF-8, UTF-16BE or UTF-16LE).
                        //BLOB. The value is a blob of data, stored exactly as it was input.
                        // H2:
                        //  CHARACTER VARYING, BIGINT, BIGINT, CHARACTER VARYING, CHARACTER VARYING, CHARACTER VARYING, CHARACTER VARYING, DOUBLE PRECISION, null] (DOUBLE PRECISION, JDBC type 6)
                        switch (columnTypes[i]) {
                            case "TEXT", "CHARACTER VARYING" -> {
                                String v = rs.getString(i);
                                stream.writeVal(v);
                            }
                            case "INT", "INTEGER", "NUMERIC" -> stream.writeVal(rs.getInt(i));
                            case "BIGINT" -> stream.writeVal(rs.getLong(i));
                            case "REAL", "DOUBLE PRECISION" -> stream.writeVal(rs.getDouble(i));
                            case "BLOB" -> {
                                Blob blob = rs.getBlob(i);
                                // TODO: Not tested yet
                                stream.writeVal(Hex.encodeHexString(blob.getBytes(0, (int) blob.length())));
                            }
                            default -> throw new JdbcExportException("Unable to handle type " + columnTypes[i]);
                        }
                        if (i != columnCount) stream.writeMore();
                    }
                }
                stream.writeObjectEnd();
                stream.writeRaw("\n");
            }
            stream.flush();
        } catch (SQLException | IOException ex) {
            throw new JdbcExportException("Exception during export: " + ex.getMessage(), ex);
        }
    }
}
