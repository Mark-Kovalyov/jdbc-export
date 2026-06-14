package mkovalev.bigdata.formatters.orc.mysql;

import mkovalev.bigdata.formatters.orc.GenericTypeMapper;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.TypeDescription;


import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlMapper extends GenericTypeMapper {

    @Override
    public  String fromOrc( TypeDescription typeDescription) {
        return super.fromOrc(typeDescription);
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
