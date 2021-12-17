package alice.tuprolog;

import jcog.Util;

/**
 * @author Matteo Iuliani
 */
public class JavaException extends Throwable {

    private final Throwable e;

    public JavaException(Throwable e) {
        this.e = e;
    }

    public Struct getException() {
        
        String java_exception = e.getClass().getName();
        
        Throwable cause = e.getCause();
        Term causeTerm = cause != null ? new Struct(cause.toString()) : new NumberTerm.Int(0);
        
        String message = e.getMessage();
        Term messageTerm = message != null ? new Struct(message) : new NumberTerm.Int(0);
        

        StackTraceElement[] elements = e.getStackTrace();
        
        return new Struct(java_exception, causeTerm, messageTerm,
                new Struct(Util.map(e -> new Struct(e.toString()), new Term[elements.length], e.getStackTrace())));
    }

}