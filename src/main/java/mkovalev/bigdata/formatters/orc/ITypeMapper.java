package mkovalev.bigdata.formatters.orc;

import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.TypeDescription;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ITypeMapper {

    String fromOrc( TypeDescription typeDescription);

    TypeDescription toOrc( String databaseType,  Integer dataTypeLength,  Integer dataTypeScale, boolean isNullable);

    void toOrcVectorized( VectorizedRowBatch batch, @Range(from = 0, to = Integer.MAX_VALUE) int rowInBatch,  ResultSet resultSet) throws SQLException;

}
