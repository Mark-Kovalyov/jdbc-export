package mayton.bigdata;

import mayton.bigdata.formatters.*;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("java:S1192")
public class JdbcExport {

    static Logger logger = LoggerFactory.getLogger("jdbc-export");

    static String logo =
            """
                _  ____  ____  ____        ________  _ ____  ____  ____ _____\s
               / |/  _ \\/  __\\/   _\\      /  __/\\  \\///  __\\/  _ \\/  __Y__ __\\
               | || | \\|| | //|  /  _____ |  \\   \\  / |  \\/|| / \\||  \\/| / \\ \s
            /\\_| || |_/|| |_\\\\|  \\__\\____\\|  /_  /  \\ |  __/| \\_/||    / | | \s
            \\____/\\____/\\____/\\____/      \\____\\/__/\\\\\\_/   \\____/\\_/\\_\\ \\_/ \s
            """;

    static Options createOptions() {
        return new Options()
                .addRequiredOption("u", "url", true, "JDBC url. (ex:jdbc:oracle:thin@localhost:1521/XE")
                .addRequiredOption("q", "query", true, "SELECT-expression (ex: SELECT * FROM EMP)")
                .addOption("c", "compression", true, "Optional parameter for AVRO and Parquet compression. See the documentation.")
                .addOption("r", "recordname", true, "Optional parameter for AVRO and Parquet")
                .addOption("n", "namespace", true, "Optional parameter for AVRO and Parquet")
                .addRequiredOption("f", "format", true, "Export format: csv|jsonl|xml|avro|parquet|protobuf")
                .addRequiredOption("o", "outputfile", true, "Output file name (ex: emp.csv)");
    }

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        Options options = createOptions();
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("\n\n" + logo + "\n", createOptions());
        } else {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("\n\n" + logo + "\n", createOptions());
                return;
            }
            String url    = line.getOptionValue("url");
            String query  = line.getOptionValue("query");
            String outputFile = line.getOptionValue("outputfile");
            String format = line.getOptionValue("format");

            Map<String, String> props = new HashMap<>();

            if (line.hasOption("compression")) {
                props.put("compression", line.getOptionValue("compression"));
            }

            if (line.hasOption("recordname")) {
                props.put("recordname", line.getOptionValue("recordname"));
            }

            if (line.hasOption("namespace")) {
                props.put("namespace", line.getOptionValue("namespace"));
            }



            try (Connection conn = DriverManager.getConnection(url);
                 Statement st = conn.createStatement()) {
                logger.info("Start analyze schema");
                ResultSet rs = st.executeQuery(String.format("%s LIMIT 1", query));
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                String []columnTypeNames = new String[columnCount + 1];
                String []columnNames     = new String[columnCount + 1];
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnTypeName = metaData.getColumnTypeName(i);
                    int columnTypeCode = metaData.getColumnType(i);
                    logger.info("Column {}: {} ({}, JDBC type {})", i, columnTypeNames, columnTypeName, columnTypeCode);
                    columnTypeNames[i] = columnTypeName;
                    columnNames[i] = columnName;
                }

                logger.info("Start export");
                ExportFormatter formatter = null;
                switch (format) {
                    case "csv"     : formatter = new CsvFormatter(); break;
                    case "jsonl"   : formatter = new JsonLineFormatter(); break;
                    case "xml"     : formatter = new XmlFormatter(); break;
                    case "avro"    : formatter = new AvroFormatter(); break;
                    case "parquet" : formatter = new ParquetFormatter(); break;
                    case "protobuf": formatter = new ProtoFormatter(); break;
                    default:
                        throw new JdbcExportException("Unknown format : " + format);
                }
                ResultSet rs2 = st.executeQuery(query);
                formatter.export(rs2, query, columnCount, columnNames, columnTypeNames, outputFile, props);
                logger.info("Finish export");
            } catch (Exception ex) {
                logger.error("{}", ex.getMessage());
            }
        }
    }



}
