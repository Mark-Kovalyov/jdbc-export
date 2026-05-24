package mayton.bigdata;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AvroTest {

    @Test
    void testSchemaConversion() {

        Schema emp = SchemaBuilder.record("emp")
                .namespace("com.oracle.scott")
                .fields()
                    .requiredString("empid")
                    .optionalString("opt_ename")
                    .requiredDouble("sal")
                    .optionalDouble("comm")
                .endRecord();

        Schema empExpected = new Schema.Parser().parse(
        """
        { "type":"record",
          "name":"emp",
          "namespace":"com.oracle.scott",
          "fields":[
             {"name":"empid","type":"string"},
             {"name":"opt_ename","type":["null","string"],"default":null},
             {"name":"sal","type":"double"},
             {"name":"comm","type":["null","double"],"default":null}]
        }
        """);

        assertEquals(empExpected, emp);


        Schema avroHttpRequestExpected = new Schema.Parser().parse(
        """
        { 
         "type":"record",
         "name":"AvroHttpRequest",
         "namespace":"com.oracle.scott",
         "fields":[
              {"name":"requestTime", "type":"long"},
              {"name":"clientIdentifier", "type":{
                  "type":"record", "name":"emp",
                  "fields":[
                     {"name":"empid", "type":"string"},
                     {"name":"opt_ename", "type":["null","string"],"default":null},
                     {"name":"sal","type":"double"},
                     {"name":"comm","type":["null","double"],"default":null}
                  ]}
              },
              {"name":"employeeNames","type":{"type":"array","items":"string"},"default":[]},
              {"name":"active","type":{"type":"enum","name":"Active","symbols":["YES","NO"]}}
          ]
         }
        """);

        Schema avroHttpRequest = SchemaBuilder.record("AvroHttpRequest")
                .namespace("com.oracle.scott")
                .fields()
                .requiredLong("requestTime")
                .name("clientIdentifier")
                .type(emp)
                .noDefault()
                .name("employeeNames")
                .type()
                .array()
                .items()
                .stringType()
                .arrayDefault(new ArrayList<>())
                .name("active")
                .type()
                .enumeration("Active")
                .symbols("YES","NO")
                .noDefault()
                .endRecord();

        assertEquals(avroHttpRequestExpected, avroHttpRequest);

    }

    static Schema ipgeoSchema = SchemaBuilder.record("ipgeo")
            .namespace("mayton.geo")
            .fields()
            .requiredDouble("x")
            .requiredDouble("y")
            .endRecord();

    @Test
    void test2() throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        GenericRecord genericRecord = new GenericData.Record(ipgeoSchema);
        genericRecord.put("x", 1.0);
        genericRecord.put("y", 2.1);

        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(ipgeoSchema);

        Encoder binaryEncoder = EncoderFactory.get().binaryEncoder(bos, null);

        writer.write(genericRecord, binaryEncoder);
        binaryEncoder.flush();
        bos.flush();

        byte[] buf = bos.toByteArray();

        assertEquals(16, buf.length);
        assertEquals("000000000000f03fcdcccccccccc0040", Hex.encodeHexString(buf));


    }






}
