package mayton.bigdata.formatters;

import com.ctc.wstx.stax.WstxOutputFactory;
import mayton.bigdata.JdbcExportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.Map;

public class XmlFormatter implements ExportFormatter{

    static Logger logger = LoggerFactory.getLogger("xml-formatter");

    private static String sanitizeForXml(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 0x20 && c < 128) {
                sb.append(c);
            } else {
                sb.append("&#x").append(Integer.toHexString(c)).append(";");
            }
        }
        return sb.toString();
    }

    @Override
    @SuppressWarnings("java:S2629")
    public void export(ResultSet rs, String query, int columnCount, String[] columnNames, String[] columnTypes,
                       String path, Map<String,String> props) throws JdbcExportException {
        XMLOutputFactory factory = new WstxOutputFactory();
        factory.setProperty("javax.xml.stream.isRepairingNamespaces", true);
        logger.info("factory class created {}", factory.getClass());
        try(OutputStream fos = new FileOutputStream(path)) {
            XMLStreamWriter writer = factory.createXMLStreamWriter(fos, "utf-8");
            writer.writeStartDocument();
            writer.writeStartElement("table");
            while (rs.next()) {
                writer.writeStartElement("row");
                for (int i = 1; i <= columnCount; i++) {
                    if (rs.getObject(i) != null) {
                        String v = sanitizeForXml(rs.getString(i));
                        writer.writeAttribute(columnNames[i], v);
                    }
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
        } catch (IOException e) {
            throw new JdbcExportException("IOException during export: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new JdbcExportException("Exception during export: " + e.getMessage(), e);
        }
    }
}
