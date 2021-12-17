package jcog.net.http;

import jcog.net.http.HttpConnection.ConnectionStateChangeListener;
import jcog.net.http.HttpConnection.STATE;
import org.jctools.queues.MpmcArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Queue;

/**
 * @author Joris
 */
class HttpSelector implements ConnectionStateChangeListener {

    //private final long TIMEOUT_CHECK_PERIOD_ms = 1_000;
    private static final long TIMEOUT_PERIOD_ms = 5_000;

    private static final Logger logger = LoggerFactory.getLogger(HttpSelector.class);
    private final WebSocketSelector.UpgradeWebSocketHandler upgradeWebSocketHandler;
    private final ByteBuffer buf = ByteBuffer.allocateDirect(HttpServer.BUFFER_SIZE);
    private final Queue<SocketChannel> newChannels = new MpmcArrayQueue<>(64);
    private final HttpModel model;


    private Selector selector;


    HttpSelector(HttpModel model, WebSocketSelector.UpgradeWebSocketHandler upgradeWebSocketHandler) {
        this.model = model;
        this.upgradeWebSocketHandler = upgradeWebSocketHandler;
    }


    @Override
    public void connectionStateChange(HttpConnection conn, STATE oldState, STATE newState) {
        switch (newState) {
            case CLOSED -> {
                conn.key.attach(null);
                conn.key.cancel();
                try {
                    conn.channel.close();
                } catch (IOException ex) {
                    logger.error("{}", ex);
                }
            }
            case UPGRADE -> {
                conn.key.attach(null);
                conn.key.cancel();
                if (conn.websocket && upgradeWebSocketHandler != null) {
                    ByteBuffer rawHead = conn.rawHead;
                    conn.rawHead = null;
                    rawHead.flip();
                    upgradeWebSocketHandler.upgradeWebSocketHandler(conn, rawHead);
                } else {
                    try {
                        conn.channel.close();
                    } catch (IOException ex) {
                        logger.error("{}", ex);
                    }
                }
            }
        }
    }

    void start() throws IOException {
        selector = Selector.open();
    }


    public void next(ServerSocketChannel ssChannel) {

        try {
            SocketChannel sChannel;
            while ((sChannel = ssChannel.accept()) != null) {
                addNewChannel(sChannel);
            }
        } catch (IOException ex) {
            logger.warn("error in accept {}", ex);
        }

        try {
            selector.selectNow();
        } catch (ClosedSelectorException | IOException ex) {
            return;
        }

        {
            SocketChannel sChannel;
            while ((sChannel = newChannels.poll()) != null) {
                try {
                    sChannel.configureBlocking(false);
                    sChannel.socket().setTcpNoDelay(false);
                    SelectionKey key = sChannel.register(selector, SelectionKey.OP_READ);
                    key.attach(new HttpConnection(this, model, key, sChannel));
                } catch (IOException e) {
                    logger.error("connect {}", e);
                }
            }
        }


        long now = System.nanoTime();

        {
			for (SelectionKey key : selector.keys()) {
				HttpConnection conn = (HttpConnection) key.attachment();
				if (now - conn.lastReceivedNS >= TIMEOUT_PERIOD_ms * 1_000_000L) {
					key.attach(null);
					key.cancel();
					conn.timeout();
				}
			}
        }

        {
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();

                HttpConnection conn = (HttpConnection) key.attachment();

                it.remove();

                if (conn == null) {
                    continue;
                }

                try {

                    if (key.isReadable()) {
                        if (!readable(conn)) {
                            key.attach(null);
                            key.cancel();
                            conn.close();
                            continue;
                        }
                    }

                    if (key.isValid() && key.isWritable()) {
                        conn.writeable();
                    }
                } catch (IOException ex) {
                    key.attach(null);
                    key.cancel();
                    conn.close(ex);
                }
            }
        }
    }


    void stop() throws IOException {
        selector.close();
    }


    /**
     * @return false if this connection should be removed
     */
    private boolean readable(HttpConnection conn) throws IOException {
        buf.clear();


        buf.limit(buf.capacity());
        buf.position(HttpServer.LINEBUFFER_SIZE);
        buf.mark();

        int read;

        try {
            read = conn.channel.read(buf);
        } catch (ClosedChannelException ex) {
            return false;
        }

        if (read > 0) {
            buf.limit(buf.position());
            buf.reset();

            conn.read(buf);
            return true;
        } else {
            return false;
        }


    }

    /**
     * Add a new socket channel to be handled by this thread.
     */


    void addNewChannel(SocketChannel sChannel) {
        newChannels.add(sChannel);

        try {
            selector.wakeup();
        } catch (IllegalStateException | NullPointerException ex) {

            assert false;
        }
    }
}
