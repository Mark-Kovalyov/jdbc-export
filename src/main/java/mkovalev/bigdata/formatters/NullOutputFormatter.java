package mkovalev.bigdata.formatters;

import mkovalev.bigdata.JdbcExportException;

import java.sql.ResultSet;
import java.util.Map;

public class NullOutputFormatter implements ExportFormatter {

    @Override
    public long export(ResultSet rs, String query, int columnCount, String[] columnNames, String[] columnTypes, String path, Map<String, String> props) throws JdbcExportException {
        long count = 0;
        try {
            while (rs.next()) {
                count++;
            }
        } catch (Exception e) {
            throw new JdbcExportException("Exception during export: " + e.getMessage(), e);
        }
        return count;
    }
}
