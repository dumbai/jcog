package jcog.net.http;

import jcog.TODO;
import jcog.net.http.HttpUtil.HttpException;
import jcog.net.http.HttpUtil.METHOD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.regex.Matcher;

/**
 * @author Joris
 */
public class HttpConnection {

    private static final Logger logger = LoggerFactory.getLogger(HttpConnection.class);
    private static final int RAWHEAD_SIZE = 512;
    final SelectionKey key;
    final SocketChannel channel;

    private final ConnectionStateChangeListener stateChangeListener;

    private final Deque<HttpResponse> responses = new ArrayDeque<>(4);
    public final Map<String, String> request = new HashMap<>();

    private final HttpModel model;

    long lastReceivedNS;
    boolean websocket = false;

    ByteBuffer rawHead;
    private ByteBuffer lineBuffer;


    private HttpResponse currentResponse;

    private boolean keepAlive;

    private STATE state;
    private METHOD method;
    private int clientHttpMinor;
    private URI requestUri;

    HttpConnection(ConnectionStateChangeListener stateChangeListener, HttpModel model, SelectionKey key, SocketChannel sChannel) {
        this.stateChangeListener = stateChangeListener;
        this.key = key;
        this.channel = sChannel;
        this.model = model;

        setState(STATE.WAIT_FOR_REQUEST_LINE);

        lastReceivedNS = System.nanoTime();
    }


    @SuppressWarnings("unchecked")
    public void read(ByteBuffer buf) {
        lastReceivedNS = System.nanoTime();


        for (STATE v : Arrays.asList(STATE.CLOSED, STATE.BAD_REQUEST, STATE.UPGRADE)) {
            if (state == v) {
                return;
            }
        }

        while (buf.hasRemaining()) {
            boolean requestReady;
            try {
                requestReady = decodeRequest(buf);

                logger.info("connect {}", requestUri);

            } catch (HttpException ex) {
                respond(new HttpResponse(method, ex.status, ex.getMessage(), ex.fatal || !this.keepAlive, null));

                setState(ex.fatal ? STATE.BAD_REQUEST : STATE.WAIT_FOR_REQUEST_LINE);

                logger.error("{}", ex);
                return;
            }

            if (requestReady) {

                int r = responses.size();

                model.response(this);

                if (r == responses.size() /* unchanged */) {
                    respondNull();
                }


                setState(this.keepAlive ? STATE.WAIT_FOR_REQUEST_LINE : STATE.CLOSED);
            }

        }
    }

    public void respond(String content) {
        respond(new HttpResponse(method,
                /*(Map<String, String>) headers.clone()*/ 200,
                content, !this.keepAlive, null));
    }

    public static void respond(byte[] content) {
        throw new TODO();
    }

    public static void respond(InputStream content) {
        throw new TODO();
    }

    public void respond(File file) {


        respond(new HttpResponse(method,
                /*(Map<String, String>) headers.clone()*/ 200, "", !this.keepAlive, file));


    }

    private void respondNull() {
        respond(new HttpResponse(method,
                /*(Map<String, String>) headers.clone()*/ 404, "", !this.keepAlive, null));
    }


    private void respond(HttpResponse resp) {
        responses.add(resp);
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    /**
     * @return true if the reqeust has been fully read and a response may be sent
     */
    private boolean decodeRequest(ByteBuffer buf) throws HttpException {
        /*
         * Request       = Request-Line              ; Section 5.1
         *                 *(( general-header        ; Section 4.5
         *                  | request-header         ; Section 5.3
         *                  | entity-header ) CRLF)  ; Section 7.1
         *                 CRLF
         *                 [ message-body ]          ; Section 4.3
         */


        if (lineBuffer != null && lineBuffer.limit() > 0) {
            assert lineBuffer.position() == 0 : "lineBuffer should have been flip()ed";
            int pos = buf.position() - lineBuffer.limit();
            buf.position(pos);
            buf.put(lineBuffer);
            lineBuffer.clear();
            buf.position(pos);
        }


        if (state == STATE.WAIT_FOR_REQUEST_LINE) {
            readRequestLine(buf);
        }


        if (state == STATE.READING_HEADERS) {


            readHeaders(buf);
        }

        if (state == STATE.DONE_READING) {
            String upgradeField = request.get("upgrade");
            if (upgradeField != null && "websocket".equalsIgnoreCase(upgradeField)) {
                websocket = true;
                setState(STATE.UPGRADE);
                return false;
            }

            if (method != METHOD.GET && method != METHOD.HEAD) {
                throw new HttpException(405, true, "Method Not Allowed");
            }


            if (request.containsKey("content-length")) {

                throw new HttpException(400, true, "Request body is not allowed for this method");
            }


            return true;
        }


        if (buf.hasRemaining()) {
            if (state == STATE.WAIT_FOR_REQUEST_LINE || state == STATE.READING_HEADERS) {
                if (lineBuffer == null) {
                    lineBuffer = ByteBuffer.allocate(HttpServer.LINEBUFFER_SIZE);
                }

                try {
                    lineBuffer.put(buf);
                    lineBuffer.flip();
                } catch (BufferOverflowException ex) {
                    throw new HttpException(413, true, "Line in Request-Entity is too Large", ex);
                }
            }
        }

        return false;
    }

    private void ensureRawHeadHasRemaining(int remaining) {
        if (rawHead == null) {

            rawHead = ByteBuffer.allocate((remaining / RAWHEAD_SIZE + 1) * RAWHEAD_SIZE);
        } else if (remaining > rawHead.remaining()) {
            int newCap = rawHead.capacity();
            newCap += remaining - rawHead.remaining();
            newCap = (newCap / RAWHEAD_SIZE + 1) * RAWHEAD_SIZE;

            ByteBuffer newBuf = ByteBuffer.allocate(newCap);

            rawHead.flip();
            newBuf.put(rawHead);
            newBuf.limit(newBuf.capacity());
            rawHead = newBuf;

            assert rawHead.remaining() >= remaining;
        }
    }

    private void rawHead_putToPos(ByteBuffer buf, int beforeLinePosition) {
        int position = buf.position();
        int limit = buf.limit();


        buf.position(beforeLinePosition);
        buf.limit(position);

        ensureRawHeadHasRemaining(buf.remaining());


        rawHead.put(buf);


        buf.position(position);
        buf.limit(limit);
    }

    static final ThreadLocal<StringBuilder> lineParser = ThreadLocal.withInitial(() -> new StringBuilder(4 * 1024));

    /**
     * Read the request line and move the buffer position beyond the request line
     */
    private void readRequestLine(ByteBuffer buf) throws HttpException {
        StringBuilder line = lineParser.get();

        try {
            while (true) {
                line.setLength(0);

                int beforeLinePosition = buf.position();

                if (!HttpUtil.readLine(line, buf, false)) {
                    return;
                }

                rawHead_putToPos(buf, beforeLinePosition);

                if (line.length() == 0) {
                    continue;
                }

                if (line.length() > 300) {
                    throw new HttpException(414, true, "Request-Line Too Long");
                } else {
                    Matcher requestLine = HttpUtil.requestLine.matcher(line.subSequence(0, line.length()));
                    if (requestLine.matches()) {
                        method = METHOD.fromRequestLine(requestLine.group(1));

                        if (method == METHOD.UNKNOWN) {
                            throw new HttpException(501, true, "Unknown Method");
                        }

                        String uri = requestLine.group(2);
                        if (uri.length() > 255) {
                            throw new HttpException(414, true, "Request-URI Too Long");
                        } else {
                            this.requestUri = new URI(uri);
                            this.clientHttpMinor = Integer.parseInt(requestLine.group(3), 10);
                        }
                    } else {
                        throw new HttpException(400, true, "Invalid Request-Line (regexp)");
                    }

                    setState(STATE.READING_HEADERS);
                    return;
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            throw new HttpException(400, true, "Invalid Request-Line (iob)", ex);
        } catch (NumberFormatException ex) {
            throw new HttpException(400, true, "Invalid Request-Line. Expected integer", ex);
        } catch (URISyntaxException ex) {
            throw new HttpException(400, true, "Invalid Request-Line. Malformed URI: " + requestUri, ex);
        }
    }

    private void readHeaders(ByteBuffer buf) throws HttpException {
        StringBuilder line = new StringBuilder();
        try {
            while (true) {
                line.setLength(0);

                int beforeLinePosition = buf.position();
                if (!HttpUtil.readLine(line, buf, true)) {
                    return;
                }

                rawHead_putToPos(buf, beforeLinePosition);


                Matcher headerLine = HttpUtil.headerLine.matcher(line.subSequence(0, line.length()));
                if (headerLine.matches()) {
                    String name = headerLine.group(1);
                    String value = headerLine.group(2);

                    request.put(name.toLowerCase(), value.trim());
                    if (request.size() > 50) {
                        throw new HttpException(400, true, "Too many headers");
                    }
                } else {
                    throw new HttpException(400, true, "Invalid message-header (regexp)");
                }

                if (buf.remaining() >= 2) {

                    if (HttpUtil.isCR(buf.get(buf.position()))
                            && HttpUtil.isLF(buf.get(buf.position() + 1))) {
                        ensureRawHeadHasRemaining(2);


                        rawHead.put(buf.get());
                        rawHead.put(buf.get());

                        setState(STATE.DONE_READING);
                        return;
                    }
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            throw new HttpException(400, true, "Invalid message-header (iob)", ex);
        }
    }

    private void setState(STATE newState) {
        if (this.state == newState) {
            return;
        }

        STATE oldState = this.state;
        this.state = newState;

        if (newState == STATE.DONE_READING) {
            this.keepAlive = this.clientHttpMinor > 0 && "keep-alive".equalsIgnoreCase(request.get("connection"));
        }

        stateChangeListener.connectionStateChange(this, oldState, newState);


        if (newState == STATE.WAIT_FOR_REQUEST_LINE || newState == STATE.CLOSED || newState == STATE.BAD_REQUEST) {
            this.requestUri = null;
            this.clientHttpMinor = 0;
            this.websocket = false;

            if (this.lineBuffer != null) {
                this.lineBuffer.clear();
            }

            if (this.rawHead != null) {
                this.rawHead.clear();
            }

            this.request.clear();
            this.keepAlive = false;
        }
    }


    public void writeable() throws IOException {
        while (!responses.isEmpty() || currentResponse != null) {
            if (currentResponse == null) {
                currentResponse = responses.removeFirst();
                currentResponse.prepare(this);
            }

            if (currentResponse.write(channel)) {
                if (currentResponse.close) {
                    setState(STATE.CLOSED);
                    channel.close();
                    logger.info("closed {}", this.keepAlive);
                }


                currentResponse = null;
            } else {
                return;
            }

        }


        key.interestOps(SelectionKey.OP_READ);
    }

    public final void close() {
        try {
            channel.close();
        } catch (IOException e) {

        }
        setState(STATE.CLOSED);
    }

    public final URI url() {
        return requestUri;
    }

    void timeout() {

        if (logger.isDebugEnabled()) {
            SocketAddress remote = null;
            try {
                remote = channel.getRemoteAddress();
                close();
            } catch (IOException e) {
                logger.debug("timeout {}", remote);
            }
        } else {
            close();
        }

    }

    public final void close(IOException ex) {
        close();
        logger.warn("{}", ex);
    }


    enum STATE {
        WAIT_FOR_REQUEST_LINE,
        READING_HEADERS,


        DONE_READING,
        UPGRADE,
        BAD_REQUEST,
        CLOSED
    }

    @FunctionalInterface
    public interface ConnectionStateChangeListener {
        void connectionStateChange(HttpConnection conn, STATE oldState, STATE newState);
    }
}