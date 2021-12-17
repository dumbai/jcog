package alice.tuprolog.lib;

import alice.tuprolog.event.ReadEvent;
import alice.tuprolog.event.ReadListener;
import jcog.data.list.Lst;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class UserContextInputStream extends InputStream {

    /**
     * Changed from a single EventListener to multiple (ArrayList) ReadListeners
     */
    private final List<ReadListener> readListeners;
    private boolean available;
    private boolean start;
    private int i;
    private InputStream result;

    /***/

    public UserContextInputStream() {
        this.available = false;
        this.start = true;
        this.readListeners = new Lst<>();
    }

    public synchronized InputStream getInput() {
        while (!available) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        available = false;
        notifyAll();
        return this.result;
    }

    public synchronized void putInput(InputStream input) {
        while (available) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



        this.result = input;
        available = true;
        notifyAll();
    }

    public void setCounter() {
        start = true;
        result = null;
    }

    @Override
    public int read() {
        if (start) {
            fireReadCalled();
            getInput();
            start = false;
        }

        do {
            try {
                i = result.read();

                if (i == -1) {
                    fireReadCalled();
                    getInput();
                    i = result.read();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (i < 0x20 && i >= -1);

        return i;
    }

    /**
     * Changed these methods because there are more readListeners
     * from the previous version
     */
    private void fireReadCalled() {
        ReadEvent event = new ReadEvent(this);

        for (ReadListener readListener : readListeners) {
            readListener.readCalled(event);
        }

    }





    /***/
}