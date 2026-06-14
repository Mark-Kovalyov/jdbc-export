package mkovalev.bigdata.formatters.orc.oracle;

import mkovalev.bigdata.formatters.orc.GenericTypeMapper;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.TypeDescription;

import javax.annotation.concurrent.ThreadSafe;
import java.sql.ResultSet;
import java.sql.SQLException;

@ThreadSafe
public class OracleTypeMapper extends GenericTypeMapper {

    @Override
    public  String fromOrc( TypeDescription typeDescription) {
        // TODO:
        return "VARCHAR2(4000)";
    }

    @Override
    public  TypeDescription toOrc( String databaseType,  Integer dataTypeLength,  Integer dataTypeScale, boolean isNullable) {
        // TODO:
        return TypeDescription.createString();
    }

    @Override
    public void toOrcVectorized( VectorizedRowBatch batch, @Range(from = 0, to = Integer.MAX_VALUE) int rowInBatch,  ResultSet resultSet) throws SQLException {
        // TODO:
    }

}
