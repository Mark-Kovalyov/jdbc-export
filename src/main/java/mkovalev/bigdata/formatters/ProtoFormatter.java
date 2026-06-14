package mkovalev.bigdata.formatters;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import mkovalev.bigdata.JdbcExportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Map;

public class ProtoFormatter implements ExportFormatter{

    static Logger logger = LoggerFactory.getLogger("proto-formatter");

    public long export(ResultSet rs, String query, int columnCount, String[] columnNames, String[] columnTypes,
                       String path, Map<String, String> props) throws JdbcExportException {
        long rows = 0L;
        String table = props.getOrDefault("table", "table1");
        DescriptorProtos.DescriptorProto.Builder protoBuilder = DescriptorProtos.DescriptorProto.newBuilder();
        protoBuilder.setName(table);
        for(int i = 1 ; i <= columnCount ; i++) {
            DescriptorProtos.FieldDescriptorProto.Type fieldType;
            switch (columnTypes[i]) {
                case "TEXT", "CHARACTER VARYING","text","timestamp" -> fieldType = DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
                case "INT", "INTEGER", "NUMERIC", "int4" -> fieldType = DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32;
                case "BIGINT","int8"             -> fieldType = DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64;
                case "REAL"                      -> fieldType = DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT;
                case "DOUBLE PRECISION","float8" -> fieldType = DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE;
                case "BLOB"                      -> fieldType = DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES;
                default -> throw new JdbcExportException("Unable to handle type " + columnTypes[i]);

            }
            protoBuilder.addField(DescriptorProtos.FieldDescriptorProto.newBuilder()
                    .setName(columnNames[i])
                    .setNumber(i)
                    .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                    .setType(fieldType));
        }
        try(FileOutputStream os = new FileOutputStream(path)) {
            DescriptorProtos.FileDescriptorProto fileDescriptorProto =
                    DescriptorProtos.FileDescriptorProto.newBuilder()
                            .setName(table)
                            .setSyntax("proto3")
                            .addMessageType(protoBuilder.build())
                            .build();

            Descriptors.FileDescriptor descFile = Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, new Descriptors.FileDescriptor[]{});

            Descriptors.Descriptor descProto = descFile.findMessageTypeByName(table);
            DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(descProto);
            // TODO: Works too slow. Should be profiled using Perf / async-profiler
            while (rs.next()) { // 6s
                messageBuilder.clear(); // 6s
                for (int i = 1; i <= columnCount; i++) { // 6s
                    if (rs.getObject(i) != null) { // 8 s
                        Object value = rs.getObject(i);
                        Class<? extends Object> objType = rs.getObject(i).getClass(); // 15 s
                        // TODO: Test for correctness of index:
                        Descriptors.FieldDescriptor fieldDescriptor = descProto.findFieldByNumber(i);
                        //Descriptors.FieldDescriptor fieldDescriptor = descProto.findFieldByName(columnNames[i]);
                        if (objType == Timestamp.class) {
                            Timestamp ts = rs.getTimestamp(i);
                            messageBuilder.setField(fieldDescriptor, ts.toLocalDateTime().toString());
                        } else {
                            messageBuilder.setField(fieldDescriptor, value);
                        }
                    }
                }
                DynamicMessage message = messageBuilder.build();
                message.writeTo(os);
                rows++;
            }
        } catch (Exception ex) {
            throw new JdbcExportException(ex.getMessage());
        }
        return rows;
    }



}
