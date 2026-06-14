package mkovalev.bigdata.formatters.orc.h2;

import mkovalev.bigdata.formatters.orc.GenericTypeMapper;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.TypeDescription;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <pre>
 * INT
 * BOOLEAN
 * TINYINT
 * SMALLINT
 * BIGINT
 * IDENTITY
 * DECIMAL
 * DOUBLE
 * REAL
 * TIME
 * TIME WITH TIME ZONE
 * DATE
 * TIMESTAMP
 * TIMESTAMP WITH TIME ZONE
 * BINARY
 * OTHER
 * VARCHAR
 * VARCHAR_IGNORECASE
 * CHAR
 * BLOB
 * CLOB
 * UUID
 * ARRAY
 * ENUM
 * GEOMETRY
 * JSON
 * INTERVAL
 * </pre>
 */
public class H2GenericTypeMapper extends GenericTypeMapper {

    @Override
    public String fromOrc( TypeDescription typeDescription) {
        String orcType = typeDescription.getCategory().getName();
        if (orcType.equalsIgnoreCase("STRING")) {
            int length = typeDescription.getMaxLength();
            int precision = typeDescription.getPrecision();
            return "VARCHAR(" + length +")";
        } else if (orcType.equalsIgnoreCase("DECIMAL")) {
            return "DECIMAL";
        } else if (orcType.equalsIgnoreCase("DOUBLE")) {
            return "DOUBLE";
        } else if (orcType.equalsIgnoreCase("DATE")) {
            return "DATE";
        } else if (orcType.equalsIgnoreCase("BIGINT")) {
            return "BIGINT";
        } else {
            throw new RuntimeException("Unable to map " + orcType);
        }
    }

    @Override
    public  TypeDescription toOrc( String databaseType,  Integer dataTypeLength,  Integer dataTypeScale, boolean isNullable) {
        return super.toOrc(databaseType, dataTypeLength, dataTypeScale, isNullable);
    }

    @Override
    public void toOrcVectorized( VectorizedRowBatch batch, int rowInBatch,  ResultSet resultSet) throws SQLException {
        super.toOrcVectorized(batch, rowInBatch, resultSet);
    }


}
