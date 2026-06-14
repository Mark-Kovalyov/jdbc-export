package mkovalev.bigdata.formatters;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import mkovalev.bigdata.JdbcExportException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

public class JacksonLineFormatter implements ExportFormatter {

    static Logger logger = LoggerFactory.getLogger("jackson-json-line-formatter");

    @Override
    public long export(ResultSet rs, String query, int columnCount, String[] columnNames, String[] columnTypes,
                       String path, Map<String, String> props) throws JdbcExportException {

        JsonFactory jfactory = new JsonFactory();
        long rows = 0L;
        try(PrintWriter pw = new PrintWriter(new FileOutputStream(path))) {
            while (rs.next()) {
                StringWriter sw = new StringWriter();
                JsonGenerator jg = jfactory.createGenerator(sw);
                jg.writeStartObject();
                for (int i = 1; i <= columnCount; i++) {
                    if (rs.getObject(i) != null) {
                        jg.writeFieldName(columnNames[i]);
                        switch (columnTypes[i]) {
                            case "text", "TEXT", "CHARACTER VARYING" -> {
                                String v = rs.getString(i);
                                jg.writeString(v);
                            }
                            case "int4", "INTEGER", "NUMERIC" -> {
                                jg.writeNumber(rs.getInt(i));
                            }
                            case "int8", "BIGINT" -> {
                                jg.writeNumber(rs.getLong(i));
                            }
                            case "float8", "REAL", "DOUBLE PRECISION" -> {
                                jg.writeNumber(rs.getDouble(i));
                            }
                            case "timestamp" -> {
                                Timestamp ts = rs.getTimestamp(i);
                                jg.writeString(ts.toLocalDateTime().toString());
                            }
                            case "blob" -> {
                                Blob blob = rs.getBlob(i);
                                // TODO: Not tested yet
                                jg.writeString(Hex.encodeHexString(blob.getBytes(0, (int) blob.length())));
                                blob.free();
                            }
                            default -> throw new JdbcExportException("Unable to handle type " + columnTypes[i]);
                        }
                    }
                }
                jg.writeEndObject();
                jg.flush();
                pw.println(sw.toString());
                rows++;
            }

        } catch (IOException | SQLException ex) {
            throw new JdbcExportException("Exception during export: " + ex.getMessage(), ex);
        }
        // stream.flush();

        return rows;

    }
}
