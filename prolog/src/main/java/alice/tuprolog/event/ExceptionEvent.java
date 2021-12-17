/*Castagna 06/2011*/
package alice.tuprolog.event;

import alice.tuprolog.JavaException;

import java.util.EventObject;

public class ExceptionEvent extends EventObject {

    public final Throwable exception;

    public ExceptionEvent(Object source, Throwable e) {
        super(source);
        exception = e;
    }

    public String getException() {
        return exception instanceof JavaException ?
                ((JavaException)exception).getException().toString() :
                exception.toString();
    }

}
/**/