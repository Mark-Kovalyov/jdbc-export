package mkovalev.bigdata.formatters.orc;

import org.apache.orc.TypeDescription;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Orc DataTypes
 *
 *     Integer
 *         boolean (1 bit)
 *         tinyint (8 bit)
 *         smallint (16 bit)
 *         int (32 bit)
 *         bigint (64 bit)
 *
 *     Floating point
 *         float
 *         double
 *
 *     String types
 *         string
 *         char
 *         varchar
 *
 *     Binary blobs
 *         binary
 *
 *     Date/time
 *         timestamp
 *         timestamp with local time zone
 *         date
 *
 *     Compound types
 *         struct
 *         list
 *         map
 *         union
 */
public class GenericTypeMapper implements ITypeMapper {

    @Override
    public  String fromOrc( TypeDescription typeDescription) {
        return "VARCHAR";
    }

    @Override
    public  TypeDescription toOrc( String databaseType,  Integer dataTypeLength,  Integer dataTypeScale, boolean isNullable) {
        return TypeDescription.createString();
    }

    @Override
    public void toOrcVectorized( VectorizedRowBatch batch,  int rowInBatch,  ResultSet resultSet) throws SQLException {
        // Nothing to do
    }


}