package mayton.bigdata.formatters;

import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.QuoteStrategies;
import mayton.bigdata.JdbcExportException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Map;

public class CsvFormatter implements ExportFormatter {
    @Override
    public void export(ResultSet rs, String query, int columnCount, String[] columnNames, String[] columnTypes,
                       String path, Map<String,String> props) throws JdbcExportException {
        try (CsvWriter csv = CsvWriter.builder()
                .quoteCharacter('"')
                .fieldSeparator(';')
                .quoteStrategy(QuoteStrategies.ALWAYS)
                .build(new FileOutputStream(path))) {
            String[] row = new String[columnCount];
            // TODO: Consider to replace with System.arraycopy(columnNames, 1, row, 0, columnCount);
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = columnNames[i];
            }
            csv.writeRecord(row);
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getString(i);
                }
                csv.writeRecord(row);
            }
        } catch (IOException e) {
            throw new JdbcExportException("IOException during export: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new JdbcExportException("Exception during export: " + e.getMessage(), e);
        }
    }
}
