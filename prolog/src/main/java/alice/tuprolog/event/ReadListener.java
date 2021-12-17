package alice.tuprolog.event;

import java.util.EventListener;

public interface ReadListener extends EventListener{

	void readCalled(ReadEvent event);
}
