package mkovalev.bigdata.formatters.orc;

import java.io.PrintStream;

public class Utils {

    private Utils() {}

    public static void println( String arg) {
        System.out.println(arg);
    }

    public static PrintStream printf( String format, Object ...args) {
        return System.out.printf(format, args);
    }

}
