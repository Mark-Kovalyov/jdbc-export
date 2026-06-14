package mkovalev.bigdata.formatters.orc;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.orc.CompressionKind;
import org.apache.orc.OrcFile;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class OrcUtils {

    static org.slf4j.Logger logger = LoggerFactory.getLogger("orc-formatter");

    private OrcUtils(){}

    public static Writer createWriter( FileSystem fileSystem,  String destPath,  TypeDescription schema,  Map<String, String> props) throws IOException {
        Path destHadoopPath = new Path(destPath);
        OrcFile.WriterOptions opts = OrcFile.writerOptions(fileSystem.getConf()).setSchema(schema);

        opts = opts.compress(CompressionKind.valueOf(props.getOrDefault("orc.compression", "none")));

        opts = opts.rowIndexStride(Integer.parseInt(props.getOrDefault("orc.rowindexstride", "0")));

        if (props.containsKey("orc.bloomcolumns")) {
            String bloomColumns = props.get("orc.bloomcolumns");
            logger.trace("Detected bloomColumns = {}", bloomColumns);
            opts = opts.bloomFilterColumns(bloomColumns);
        }

        opts = opts.bloomFilterFpp(Double.parseDouble(props.getOrDefault("orc.bloomfilterfpp", "0.99")));

        if (line.hasOption("orc.stripesize"))
            opts = opts.stripeSize(Long.parseLong(line.getOptionValue("orc.stripesize")));

        if (line.hasOption("orc.buffersize"))
            opts = opts.bufferSize(Integer.parseInt(line.getOptionValue("orc.buffersize")));

        opts = opts.enforceBufferSize();

        return OrcFile.createWriter(destHadoopPath, opts);
    }


    public static Writer createWriter( FileSystem fileSystem,  String destPath,  TypeDescription schema) throws IOException {
        Path destHadoopPath = new Path(destPath);
        return OrcFile.createWriter(destHadoopPath, OrcFile.writerOptions(fileSystem.getConf())
                .setSchema(schema)
                .compress(CompressionKind.NONE)
                .stripeSize(128L * 1024 * 1024)
                .bufferSize(256 * 1024)
                .rowIndexStride(10000)
                .version(OrcFile.Version.V_0_12));
    }

}
