package mkovalev.bigdata.formatters;

import com.jsoniter.output.JsonStream;
import mkovalev.bigdata.JdbcExportException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

public class JsonLineFormatter2 implements ExportFormatter {

    static Logger logger = LoggerFactory.getLogger("json-line-formatter2");

    @Override
    public long export(ResultSet rs, String query, int columnCount, String[] columnNames, String[] columnTypes,
                       String path, Map<String, String> props) throws JdbcExportException {


        long rows = 0L;

        while (rs.next()) {
            // stream.writeObjectStart();
            for (int i = 1; i <= columnCount; i++) {
                if (rs.getObject(i) != null) {
                    // stream.writeObjectField(columnNames[i]);
                    switch (columnTypes[i]) {
                        case "text", "TEXT", "CHARACTER VARYING" -> {
                            String v = rs.getString(i);
                            // stream.writeVal(v);
                        }
                        case "int4", "INTEGER", "NUMERIC" -> {
                            //stream.writeVal(rs.getInt(i));
                        }
                        case "int8", "BIGINT" -> {
                            //stream.writeVal(rs.getLong(i));
                        }
                        case "float8", "REAL", "DOUBLE PRECISION" -> {
                            //stream.writeVal(rs.getDouble(i));
                        }
                        case "timestamp" -> {
                            // TODO: Introduce custom local date time format
                            Timestamp ts = rs.getTimestamp(i);
                            //stream.writeVal(ts.toLocalDateTime().toString());
                        }
                        case "blob" -> {
                            Blob blob = rs.getBlob(i);
                            // TODO: Not tested yet
                            //stream.writeVal(Hex.encodeHexString(blob.getBytes(0, (int) blob.length())));
                        }
                        default -> throw new JdbcExportException("Unable to handle type " + columnTypes[i]);
                    }
                    if (i != columnCount) {
                        //stream.writeMore();
                    }
                }
            }
            // stream.writeObjectEnd();
            // stream.writeRaw("\n");
            rows++;
        }
        // stream.flush();

        return rows;
    }
}
