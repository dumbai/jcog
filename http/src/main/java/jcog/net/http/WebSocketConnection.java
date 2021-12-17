package jcog.net.http;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.CloseFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Joris
 */
public class WebSocketConnection extends WebSocketImpl {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConnection.class);

    static final AtomicInteger serial = new AtomicInteger();

    private final int hash;
    public final HttpConnection http;

    WebSocketConnection(WebSocketSelector.NewChannel newChannel, WebSocketSelector ws) throws IOException {
        super(ws, List.of(new Draft_6455()));
        this.http = newChannel.http;
        this.hash = serial.incrementAndGet();

        SocketChannel chan = http.channel;
        chan.configureBlocking(false);
        chan.socket().setTcpNoDelay(true);

        setSelectionKey(chan.register(ws.selector, SelectionKey.OP_READ, this));

        if (ws.listener.wssConnect(this)) {
            setChannel(chan);

            ByteBuffer prependData = newChannel.prependData;
            newChannel.prependData = null;

            decode(prependData);
            logger.info("connect {} {}", chan.getRemoteAddress(), getResourceDescriptor());
        } else {
            close(CloseFrame.REFUSE);
            getSelectionKey().cancel();
            logger.info("non-connect {} {}", chan.getRemoteAddress(), this);
        }

    }

    @Override
    public int hashCode() {
        return hash;
    }

    public URI url() {
        return http.url();
    }
}
