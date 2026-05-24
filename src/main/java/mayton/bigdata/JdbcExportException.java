package mayton.bigdata;

public class JdbcExportException extends Exception {

    public JdbcExportException(String comment) {
        super(comment);
    }

    public JdbcExportException(String comment, Throwable cause) {
        super(comment, cause);
    }

}
