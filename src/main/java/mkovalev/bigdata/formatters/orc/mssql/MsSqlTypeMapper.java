package mkovalev.bigdata.formatters.orc.mssql;

import mkovalev.bigdata.formatters.orc.GenericTypeMapper;
import org.apache.orc.TypeDescription;


public class MsSqlTypeMapper extends GenericTypeMapper {

    @Override
    public  String fromOrc( TypeDescription typeDescription) {
        return "VARCHAR";
    }

}
