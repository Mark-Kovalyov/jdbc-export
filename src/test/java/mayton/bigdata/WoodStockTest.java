package mayton.bigdata;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxOutputFactory;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.codehaus.stax2.XMLStreamWriter2;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;

public class WoodStockTest {

    @Test
    void test() throws XMLStreamException, DecoderException {
        XMLOutputFactory factory = new WstxOutputFactory();

        factory.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_ATTR, false);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter writer = factory.createXMLStreamWriter(bos, "utf-8");
        writer.writeStartDocument();
        writer.writeStartElement("book");
        String demo = new String(Hex.decodeHex("3132383020416c6d6173202d2028323031362920446f6d657374696b6f201b"));

        writer.writeAttribute("x", demo.replaceAll("\u001b",""));
        writer.writeEndElement();
        writer.writeEndDocument();
    }

}
