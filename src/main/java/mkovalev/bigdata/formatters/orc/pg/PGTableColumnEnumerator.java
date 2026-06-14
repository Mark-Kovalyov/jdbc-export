package mkovalev.bigdata.formatters.orc.pg;

import mkovalev.bigdata.formatters.orc.TableColumnEntity;
import mkovalev.bigdata.formatters.orc.TableColumnEnumerator;

import java.sql.Connection;

public class PGTableColumnEnumerator implements TableColumnEnumerator {

    @Override
    public  Iterable<TableColumnEntity> entities( Connection connection) {
        return null;
    }
}
