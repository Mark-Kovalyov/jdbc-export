package mkovalev.bigdata.formatters;

import mkovalev.bigdata.JdbcExportException;
import mkovalev.bigdata.formatters.orc.OrcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;

import static org.apache.parquet.example.Paper.schema;

public class OrcFormatter implements ExportFormatter {

    static Logger logger = LoggerFactory.getLogger("orc-formatter");


    @Override
    public long export(ResultSet rs, String query, int columnCount, String[] columnNames, String[] columnTypes, String path,
                       Map<String, String> props) throws JdbcExportException {
        long count = 0;
        Configuration conf = new Configuration();
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
        String userDir = System.getProperty("user.dir");

        logger.info("User dir = {}", userDir);

        org.apache.hadoop.fs.Path currentDirPath = new org.apache.hadoop.fs.Path(userDir);

        logger.info("currentDirPath = {}", currentDirPath);

        org.apache.hadoop.fs.FileSystem currentDirPathFileSystem = currentDirPath.getFileSystem(conf);

        logger.info("fs.canonicalServName = {}", currentDirPathFileSystem.getCanonicalServiceName());
        boolean deleteResult = currentDirPathFileSystem.delete(new Path(path), false);
        logger.trace("Deleted res = {}", deleteResult);
        logger.info("create Orc-Writer with schema");



        int batchSize = Integer.parseInt(props.getOrDefault("batchsize", "1000"));
        int fetchSize = Integer.parseInt(props.getOrDefault("fetchsize", "50"));

        String tableName = props.getOrDefault("tablename");

        long estimatedRows = -1;
        try (Writer writer = OrcUtils.createWriter(currentDirPathFileSystem, path, schema, props)) {
            //processWithWriter(writer, schema, rs, "SELECT * FROM " + tableName, batchSize, fetchSize, estimatedRows);
            while (rs.next()) {
                count++;
            }
        } catch (Exception e) {
            throw new JdbcExportException("Exception during export: " + e.getMessage(), e);
        }
        return count;
    }
}
