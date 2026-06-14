package mkovalev.bigdata.formatters.orc;

import java.sql.Connection;

public interface TableColumnEnumerator {

     Iterable<TableColumnEntity> entities( Connection connection);

}
