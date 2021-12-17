package jcog.net.http;

import jcog.data.list.MetalConcurrentQueue;
import jcog.data.list.MetalRing;
import org.java_websocket.SocketChannelIOHelper;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.Handshakedata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Joris
 */
class WebSocketSelector extends WebSocketAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketSelector.class);
    private final MetalRing<jcog.net.http.WebSocketSelector.NewChannel> newChannels = new MetalConcurrentQueue(1024);
    protected final HttpModel listener;
    private final Set<WebSocket> connections = new LinkedHashSet<>();
    transient Selector selector;


    WebSocketSelector(HttpModel listener) {
        this.listener = listener;
    }


    private boolean registerNext() {
        NewChannel newChannel = newChannels.poll();
        if (newChannel == null) {
            return false;
        }

        try {
            new WebSocketConnection(newChannel, this);
        } catch (IOException e) {
            logger.warn("{}", e);
        }
        return true;
    }

    private boolean readable(WebSocketImpl conn) throws IOException {
        buffer.clear();
        int read = conn.getChannel().read(buffer);
        buffer.flip();

        switch (read) {
            case -1:
                conn.eot();
                return true;
            case 0:
                return true;
            default:
                conn.decode(buffer);
                return false;
        }
    }

    private static boolean writable(SelectionKey key, WebSocketImpl conn) throws IOException {
        if (SocketChannelIOHelper.batch(conn, conn.getChannel())) {
            if (key.isValid()) {
                key.interestOps(SelectionKey.OP_READ);
            }
            return true;
        }

        return false;
    }

    private final ByteBuffer buffer = ByteBuffer.allocate(WebSocketImpl.RCVBUF);

    void start() throws IOException {
        selector = Selector.open();
    }

    void stop() throws IOException {
        try {
            for (WebSocket ws : connections)
                ws.close(CloseFrame.NORMAL);

            connections.clear();
        } finally {
            selector.close();
            selector = null;
        }
    }

    public void next() {


        try {
            selector.selectNow();
        } catch (ClosedSelectorException | IOException ex) {
            return;
        }

        while (registerNext()) {
        }

        Iterator<SelectionKey> it = selector.selectedKeys().iterator();
        while (it.hasNext()) {

            SelectionKey key = it.next();

            if (!key.isValid()) continue;

            it.remove();

            WebSocketImpl conn = null;
            try {
                if (key.isReadable()) {
                    conn = (WebSocketImpl) key.attachment();
                    if (readable(conn)) {

                    }

                }

                if (key.isValid() && key.isWritable()) {
                    if (conn == null)
                        conn = (WebSocketImpl) key.attachment();
                    if (writable(key, conn)) {

                    }
                }

            } catch (ClosedSelectorException | CancelledKeyException | IOException ex) {
                if (conn!=null) conn.close();
                key.cancel();
                handleException(conn, ex);
            }
        }


    }

    private void handleException(WebSocket conn, Exception ex) {
        onWebsocketError(conn, ex);

        try {
            if (conn != null) {
                conn.close(CloseFrame.ABNORMAL_CLOSE);
            }
        } catch (CancelledKeyException ex2) {
            onWebsocketClose(conn, CloseFrame.ABNORMAL_CLOSE, null, true);
        }
    }

    @Override
    public final void onWebsocketMessage(WebSocket conn, String message) {
        onMessage(conn, message);
    }

    @Override
    public final void onWebsocketMessage(WebSocket conn, ByteBuffer blob) {
        onMessage(conn, blob);
    }

    @Override
    public final void onWebsocketOpen(WebSocket conn, Handshakedata handshake) {

        if (this.connections.add(conn)) {
            onOpen(conn, (ClientHandshake) handshake);
        }

    }

    @Override
    public final void onWebsocketClose(WebSocket conn, int code, String reason, boolean remote) {
        try {
            selector.wakeup();
        } catch (IllegalStateException ex) {
            logger.error("{}", ex);
        }

        if (this.connections.remove(conn)) {
            onClose(conn, code, reason, remote);
        }

    }

    /**
     * @param conn may be null if the error does not belong to a single connection
     */
    @Override
    public final void onWebsocketError(WebSocket conn, Exception ex) {
        onError(conn, ex);
    }

    @Override
    public final void onWriteDemand(WebSocket w) {
        WebSocketImpl conn = (WebSocketImpl) w;
        conn.getSelectionKey().interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        try {
            selector.wakeup();
        } catch (IllegalStateException ex) {
            logger.error("{}", ex);
        }
    }


    private void onOpen(WebSocket conn, ClientHandshake handshake) {
        listener.wssOpen(conn, handshake);
    }

    private void onClose(WebSocket conn, int code, String reason, boolean remote) {
        listener.wssClose(conn, code, reason, remote);
    }

    private void onMessage(WebSocket conn, String message) {
        listener.wssMessage(conn, message);
    }

    private void onMessage(WebSocket conn, ByteBuffer message) {
        listener.wssMessage(conn, message);
    }

    private void onError(WebSocket conn, Exception ex) {
        listener.wssError(conn, ex);
    }


    void addNewChannel(HttpConnection http, ByteBuffer prependData) {
        if (!newChannels.offer(new NewChannel(http, prependData))) {
            System.err.println("newChannel queue overflow");
        }

        try {
            selector.wakeup();
        } catch (IllegalStateException | NullPointerException ex) {

            assert false;
        }
    }

    @Override
    public void onWebsocketClosing(WebSocket ws, int code, String reason, boolean remote) {
    }

    @Override
    public void onWebsocketCloseInitiated(WebSocket ws, int code, String reason) {
    }

    private static Socket socket(WebSocket conn) {
        return ((SocketChannel) ((WebSocketImpl) conn).getSelectionKey().channel()).socket();
    }

    @Override
    public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
        return (InetSocketAddress) socket(conn).getLocalSocketAddress();
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
        return (InetSocketAddress) socket(conn).getRemoteSocketAddress();
    }

    static final class NewChannel {
        final HttpConnection http;
        ByteBuffer prependData;

        NewChannel(HttpConnection http, ByteBuffer prependData) {
            this.http = http;
            this.prependData = prependData;
        }
    }

    @FunctionalInterface
    interface UpgradeWebSocketHandler {
        void upgradeWebSocketHandler(HttpConnection sChannel, ByteBuffer prependData);
    }

}