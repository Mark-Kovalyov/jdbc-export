package mkovalev.bigdata.formatters;

import mkovalev.bigdata.JdbcExportException;

import java.sql.ResultSet;
import java.util.Map;

public interface ExportFormatter {

    void export(ResultSet rs, String query, int columnCount, String[] columnNames, String[] columnTypes, String path,
                Map<String,String> props) throws JdbcExportException;

}
