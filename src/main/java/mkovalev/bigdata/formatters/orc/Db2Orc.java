package mkovalev.bigdata.formatters.orc;

import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.*;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;

@SuppressWarnings({"java:S2629","java:S1192","java:S112","java:S1135"})
public class Db2Orc extends GenericMainApplication {

    static org.slf4j.Logger logger = LoggerFactory.getLogger("orc-formatter");

    
    @Override
    String createLogo() {
        return "\n     888 888       .d8888b.                           \n" +
                "     888 888      d88P  Y88b                          \n" +
                "     888 888             888                          \n" +
                " .d88888 88888b.       .d88P  .d88b.  888d888 .d8888b \n" +
                "d88\" 888 888 \"88b  .od888P\"  d88\"\"88b 888P\"  d88P\"    \n" +
                "888  888 888  888 d88P\"      888  888 888    888      \n" +
                "Y88b 888 888 d88P 888\"       Y88..88P 888    Y88b.    \n" +
                " \"Y88888 88888P\"  888888888   \"Y88P\"  888     \"Y8888P\n\n";
    }

    
    @Override
    Options createOptions() {
        return new Options()
                .addOption("u", "url",       true, "JDBC url. (ex:jdbc:oracle:thin@localhost:1521/XE")
                .addOption("l", "login",     true, "JDBC login")
                .addOption("p", "password",  true, "JDBC password")
                .addOption("o", "orcfile",   true, "Orc file. (ex:big-data.orc)")
                .addOption("fs","fetchsize", true, "JDBC fetch size (ex:50)")
                .addOption("h", "help",      false, "Print this help")

                .addOption("s", "selectexpr", true, "SELECT-expression (ex: SELECT * FROM EMP)")
                .addOption("t", "tablename",  true, "Table or View name")
                .addOption("b", "batchsize",  true, "Batch size (rows) default = 50 000")

                // TODO: Test all
                .addOption("bs", "orc.buffersize",     true, "Orc buffersize (integer)")
                .addOption("co", "orc.compression",    true, "Orc file compression := { NONE, ZLIB, SNAPPY, LZO, LZ4, ZSTD }")
                .addOption("bc", "orc.bloomcolumns",   true, "Orc file bloom filter columns (comma-separated)")
                .addOption("bf", "orc.bloomfilterfpp", true, "False positive probability (float) for bloom filter [0.75..0.99]")
                .addOption("ss", "orc.stripesize",     true, "The writer stores the contents of the" +
                                   " stripe in memory until this memory limit is reached and the stripe" +
                                   " is flushed to the HDFS file and the next stripe started")

                .addOption("ri", "orc.rowindexstride", true, "Row index stride [0..1000], 0 - means no index will be.");
    }

    /**
     *
     * <p>Getting results based on a cursor</p>
     *
     * <p>By default the driver collects all the results for the query at once. This can be inconvenient for
     * large data sets so the JDBC driver provides a means of basing a ResultSet on a database cursor and
     * only fetching a small number of rows.</p>
     *
     * <p>A small number of rows are cached on the client side of the connection and when exhausted the next
     * block of rows is retrieved by repositioning the cursor.</p>
     *
     * <p>Note</p>
     *
     *     Cursor based ResultSets cannot be used in all situations. There a number of restrictions which
     *     will make the driver silently fall back to fetching the whole ResultSet at once.
     *
     *     <ul>
     *     <li>The connection to the server must be using the V3 protocol. This is the default for (and is only supported by)
     *       server versions 7.4 and later.
     *     <li>The Connection must not be in autocommit mode. The backend closes cursors at the end of transactions,
     *       so in autocommit mode the backend will have closed the cursor before anything can be fetched from it.
     *     <li>The Statement must be created with a ResultSet type of ResultSet.TYPE_FORWARD_ONLY. This is the default,
     *       so no code will need to be rewritten to take advantage of this, but it also means that you cannot scroll
     *       backwards or otherwise jump around in the ResultSet.
     *     <li>The query given must be a single statement, not multiple statements strung together with semicolons.
     *     </ul>
     *
     */
    public void processWithWriter( Writer writer,  TypeDescription schema,  Connection connection,
                                   String query, int orcBatchSize, int jdbcFetchSize,
                                   ITypeMapper genericTypeMapper, long estimatedRows) throws IOException, SQLException {
        logger.traceEntry("processWithWriter orcBatchSize = {}, jdbcFetchSize = {}", orcBatchSize, jdbcFetchSize);

        // TODO: Consider BCEL/Asm to implement table-per-assembly jvm code
        VectorizedRowBatch batch = schema.createRowBatch(orcBatchSize);
        connection.setAutoCommit(false);
        int allRows = 0;
        try (Statement statement = connection.createStatement()) {
            logger.info("execute query : {}", query);
            statement.setFetchSize(jdbcFetchSize);
            statement.executeQuery(query);
            // TODO: Bencmark with : { ResultSet.TYPE_FORWARD_ONLY , .... }
            try(ResultSet resultSet = statement.getResultSet()) {
                int rows = 0;
                long batches = 0;
                while (true) {
                    boolean fetchResult = resultSet.next();
                    if (!fetchResult) {
                        break;
                    }
                    ToOrcVectorizedEvent toOrcVectorizedEvent = new ToOrcVectorizedEvent();
                    toOrcVectorizedEvent.begin();
                    genericTypeMapper.toOrcVectorized(batch, rows, resultSet);
                    toOrcVectorizedEvent.end();
                    toOrcVectorizedEvent.commit();
                    batch.size++;
                    rows++;
                    allRows++;

                    if (batch.size >= orcBatchSize) {
                        if (batches % 100 == 0) {
                            logger.trace(sofarTracker.toString());
                        }

                        writer.addRowBatch(batch);
                        batches++;
                        batch.reset();
                        rows=0;
                    }
                }
                if (batch.size > 0) {
                    logger.trace(sofarTracker.toString());
                    writer.addRowBatch(batch);
                    batches++;
                    batch.reset();
                }
                logger.info("Successfully write {} rows and {} batches", allRows, batches);
                logger.trace(sofarTracker.toString());
            }
            catch (SQLException ex) {
                logger.error("SQLException during processWithWriter", ex);
            }
        }
    }

    public TypeDescription prepareTypeDescription( Connection connection,  CommandLine line) throws SQLException {
        logger.info("prepareTypeDescription");
        DatabaseMetaData metadata = connection.getMetaData();
        String url        = line.getOptionValue("url");
        String tableName  = line.getOptionValue("tablename");
        String selectExpr = line.getOptionValue("selectexpr");
        TypeDescription schema = TypeDescription.createStruct();
        ITypeMapper genericTypeMapper = MapperManager.instance.detect(url);
        if (tableName != null) {
            logger.info("Table name mode");
            ResultSet resultSetColumns = SQLUtils.getColumns(metadata, tableName);
            logger.trace("[3.2] Process type mapper");
            int cnt = 0;
            while (resultSetColumns.next()) {
                /*if (logger.isDebugEnabled()) {
                    for (int i = 1; i <= 23; i++) {
                        logger.trace("[3.3] :: {}, column# = {}, object = {}, type = {}", cnt, i, resultSetColumns.getObject(i), resultSetColumns.getType());
                    }
                }*/
                cnt++;
                String columnName = resultSetColumns.getString("COLUMN_NAME");
                int dataType      = resultSetColumns.getInt("DATA_TYPE");
                String typeName   = resultSetColumns.getString("TYPE_NAME");
                int nullAllowed   = resultSetColumns.getInt("NULLABLE");
                int columnSize    = resultSetColumns.getInt("COLUMN_SIZE");
                int scale         = resultSetColumns.getInt(9); // TODO: What is the field name?
                logger.trace("[3.1] columnName {} : dataType = {}, columnSize = {}, scale = {}, typeName = {}, nullAllowred = {}",
                        columnName,
                        dataType,
                        columnSize,
                        scale,
                        typeName,
                        nullAllowed);
                // TODO: Pass nullable
                TypeDescription typeDescription = genericTypeMapper.toOrc(
                        typeName,
                        columnSize,
                        scale,
                        true);

                logger.info("typeDescription = {}", typeDescription);
                schema.addField(columnName, typeDescription);
            }
            resultSetColumns.close();
        } else if (selectExpr != null) {
            logger.info("'Select-expr' mode");
            try (Statement statement = connection.createStatement();
                 // TODO : Adopt to oracle-style, my-sql e.t.c. 'limit' expression
                 ResultSet resultSet = statement.executeQuery(selectExpr + " LIMIT 1")) {
                ResultSetMetaData rsmd = resultSet.getMetaData();
                int numberOfColumns = rsmd.getColumnCount();
                logger.trace("[3.5] Number of columns : {}", numberOfColumns);
                for (int i = 1; i <= numberOfColumns; i++) {
                    String columnName = rsmd.getColumnName(i);
                    String typeName   = rsmd.getColumnTypeName(i);
                    int precision     = rsmd.getPrecision(i);
                    int scale         = rsmd.getScale(i);
                    // TODO: pass nullable
                    TypeDescription typeDescription = genericTypeMapper.toOrc(
                            typeName,
                            precision,
                            scale,
                            true);

                    logger.trace("[3.6] Column {} : typeName = {}, precision = {}, scale = {}",
                            columnName,
                            typeName,
                            precision,
                            scale);

                    schema.addField(columnName, typeDescription);
                }
            }
        } else {
            throw new RuntimeException("Unable to detect 'tablename' or 'selectexpr' parameter!");
        }
        return schema;
    }

    public long estimateRows( Connection connection,  String selectCountExpression) throws SQLException {
        long resultRows = -1;
        try(Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(selectCountExpression);
            result.next();
            resultRows = result.getLong(1);
        }
        logger.info("Estimated rows = {}", resultRows);
        return resultRows;
    }

    public void processRows( Connection connection,  TypeDescription schema,  CommandLine line) throws IOException {
        String orcfile = line.getOptionValue("orcfile");
        logger.info("Export ORC file = {}", orcfile);
        Configuration conf = new Configuration();
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
        String userDir = System.getProperty("user.dir");
        logger.info("User dir = {}", userDir);
        org.apache.hadoop.fs.Path currentDirPath = new org.apache.hadoop.fs.Path(userDir);
        logger.info("currentDirPath = {}", currentDirPath);
        org.apache.hadoop.fs.FileSystem currentDirPathFileSystem = currentDirPath.getFileSystem(conf);
        logger.info("fs.canonicalServName = {}", currentDirPathFileSystem.getCanonicalServiceName());
        logger.trace("Delete old file {}", orcfile);
        boolean deleteResult = currentDirPathFileSystem.delete(new Path(orcfile), false);
        logger.trace("Deleted res = {}", deleteResult);
        logger.info("create Orc-Writer with schema");

        int batchSize = 1000;
        if (line.hasOption("batchsize")) {
            batchSize = Integer.parseInt(line.getOptionValue("batchsize"));
        }
        int fetchSize = 50;
        if (line.hasOption("fetchsize")) {
            fetchSize = Integer.parseInt(line.getOptionValue("fetchsize"));
        }
        String tableName = line.getOptionValue("tablename");
        String selectExpr = line.getOptionValue("selectexpr");
        ITypeMapper genericTypeMapper = MapperManager.instance.detect(line.getOptionValue("url"));

        long estimatedRows = -1;
        try (Writer writer = OrcUtils.createWriter(currentDirPathFileSystem, orcfile, schema, line)) {
            if (selectExpr != null) {
                estimatedRows = estimateRows(connection, "SELECT COUNT(*) FROM (" + selectExpr + ") AS TEMP");
                processWithWriter(writer, schema, connection, selectExpr, batchSize, fetchSize, genericTypeMapper, estimatedRows);
            } else if (tableName != null) {
                estimatedRows = estimateRows(connection, "SELECT COUNT(*) FROM " + tableName);
                processWithWriter(writer, schema, connection, "SELECT * FROM " + tableName, batchSize, fetchSize, genericTypeMapper, estimatedRows);
            } else {
                throw new IllegalArgumentException("Undefined table or SQL-expression for export!");
            }
        } catch (IOException | SQLException ex) {
            logger.error("IOException : ", ex);
        }
        logger.info("Orc-Writer closed");
    }

    public void process( CommandLine line) throws SQLException, IOException {
        logger.info("Start process");

        long pid = ProcessHandle.current().pid();
        File pidFile = new File("db2orc.pid");
        try (PrintWriter pw = new PrintWriter(new FileWriter(pidFile))) {
            pw.println(pid);
            pw.flush();
        } catch (Exception ex) {
            logger.warn("Unable to write pid file", ex);
        }

        String url = line.getOptionValue("url");
        logger.trace("url = {}", url);

        try (Connection connection = DriverManager.getConnection(
                url,
                line.getOptionValue("login"),
                line.getOptionValue("password"))) {

            logger.info("Read metadata from DB");

            TypeDescription schema = prepareTypeDescription(connection, line);

            processRows(connection, schema, line);

        } catch (SQLException ex) {
            logger.error("SQLException : ", ex);
        } finally {
            pidFile.delete();
        }
        logger.info("Finish!");
    }

    public void process(String[] args) throws SQLException, ParseException, IOException {
        CommandLineParser parser = new DefaultParser();
        Options options = createOptions();
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(createLogo(), createOptions());
        } else {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(createLogo(), createOptions());
                return;
            }
            process(line);
        }

    }

    public static void main(String[] args) throws SQLException, ParseException, IOException {
        System.setProperty("log4j1.compatibility", "true");
        System.setProperty("log4j.configuration", "log4j.properties");
        new Db2Orc().process(args);
    }


}
