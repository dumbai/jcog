/*Castagna 06/2011 >*/
package alice.tuprolog.event;

import java.util.EventListener;

@FunctionalInterface
public interface ExceptionListener extends EventListener {
    void onException(ExceptionEvent e);
}
/**/