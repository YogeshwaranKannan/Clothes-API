package Farme_rich.ErrorHandling;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandling {
    public static String GetFullExceptionDetails(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
