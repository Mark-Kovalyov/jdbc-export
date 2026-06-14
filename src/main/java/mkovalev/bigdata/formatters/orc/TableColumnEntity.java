package mkovalev.bigdata.formatters.orc;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class TableColumnEntity {

    public final String columnName;

    public TableColumnEntity(String columnName) {
        this.columnName = columnName;
    }
}
