package mkovalev.bigdata.formatters.orc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUtils {

    private SQLUtils() {}

    
    public static ResultSet getColumns( DatabaseMetaData metadata,  String tableName) throws SQLException {
        return metadata.getColumns(null, null, tableName, null);
    }
}
