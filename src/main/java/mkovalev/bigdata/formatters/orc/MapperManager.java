package mkovalev.bigdata.formatters.orc;

import mkovalev.bigdata.formatters.orc.maria.MariaDbTypeMapper;
import mkovalev.bigdata.formatters.orc.mssql.MsSqlTypeMapper;
import mkovalev.bigdata.formatters.orc.mysql.MySqlMapper;
import mkovalev.bigdata.formatters.orc.oracle.OracleTypeMapper;
import mkovalev.bigdata.formatters.orc.pg.PgTypeMapper;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapperManager {

    static org.slf4j.Logger logger = LoggerFactory.getLogger("orc-formatter");

    public static MapperManager instance = new MapperManager();

    private MapperManager() {}

    
    ITypeMapper detect( String jdbcUrl) {
        Pattern jdbcPattern = Pattern.compile("^(?<prefix>jdbc:[a-z]+:).+");
        Matcher matcher = jdbcPattern.matcher(jdbcUrl);
        if (matcher.matches()) {
            String jdbcUrlPrefix = matcher.group("prefix");
            logger.info("jdbcUrlPrefix = {}", jdbcUrlPrefix);
            switch (jdbcUrlPrefix) {
                case "jdbc:postgresql:":
                    return new PgTypeMapper();
                case "jdbc:oracle:":
                    return new OracleTypeMapper();
                case "jdbc:sqlserver:":
                    return new MsSqlTypeMapper();
                case "jdbc:mysql:":
                    return new MySqlMapper();
                case "jdbc:mariadb:":
                    return new MariaDbTypeMapper();
                default:
                    logger.error("Warning! Unable to detect MapperManager from url = {}. Trying to use default.", jdbcUrl);
                    return new GenericTypeMapper();
            }
        } else {
            throw new IllegalArgumentException("jdbcUrl doesnt match to pattern " + jdbcPattern.pattern());
        }
    }

}
