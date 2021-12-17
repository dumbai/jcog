package jcog.net;


import com.fasterxml.jackson.core.JsonProcessingException;
import jcog.exe.Loop;
import jcog.io.Serials;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.Arrays;

/**
 * generic UDP server & utilities
 * TODO Datagram TLS
 */
public class UDP extends Loop {

    /**
     * in bytes
     * https://www.quora.com/What-is-the-maximum-size-of-a-UDP-datagram?share=1
     *
     * TODO decide this based on LAN/WAN usage
     */
    private static final int MAX_PACKET_SIZE = 65500;


    private static final Logger logger = LoggerFactory.getLogger(UDP.class);


    protected final DatagramChannel c;
    public final InetSocketAddress addr;


    static {

        System.setProperty("java.net.preferIPv4Stack", "false");
        System.setProperty("java.net.preferIPv6Addresses",
                "true"
                //"false"
        );
    }

    static void ipv6(byte[] address, byte[] target, int offset) {
        if (address.length == 4) {
            Arrays.fill(target, offset, 10, (byte)0);
            Arrays.fill(target, offset+10, 12, (byte)(0xff));
            System.arraycopy(address, 0, target, offset+12, 4);
        } else {
            System.arraycopy(address, 0, target, offset, 16);
        }
    }

    UDP(@Nullable InetAddress a, int port) throws IOException {
        super();

        if (a instanceof Inet4Address) {
            byte[] target = new byte[16];
            ipv6(a.getAddress(), target, 0);

            a = Inet6Address.getByAddress(a.getHostName(), target, null);
        }


        c = DatagramChannel.open(StandardProtocolFamily.INET6);
        c.configureBlocking(false);

        c.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 128);
        c.setOption(StandardSocketOptions.SO_SNDBUF, 1024 * 128);

        c.socket().setBroadcast(true);
        c.setOption(StandardSocketOptions.SO_BROADCAST, true);

        c.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        c.setOption(StandardSocketOptions.SO_REUSEPORT, true);

        InetSocketAddress aa = new InetSocketAddress(a, port);
        c.bind(aa);


        port = ((InetSocketAddress)c.getLocalAddress()).getPort();
        this.addr = new InetSocketAddress(a, port); //(InetSocketAddress) c.getLocalAddress();



    }

    @Override
    protected void starting() {
        logger.info("start {}", addr);
    }

    //    public UDP(int port) throws IOException {
//        this(null, port);
//    }
//
//    public UDP() throws IOException {
//        this(null, 0);
//    }

    public int port() {
        return addr.getPort();
    }

    public InetSocketAddress addr() { return addr; }

    @Override
    protected void stopping() {
        try {
            synchronized (c) {
                c.configureBlocking(true);
                c.disconnect();
                c.configureBlocking(false);
            }
            logger.info("stop {}", addr);
        } catch (IOException e) {
            logger.error("close {}", e);
        }
    }


    private final ByteBuffer b = ByteBuffer.allocate(MAX_PACKET_SIZE);

    @Override
    public boolean next() {
        try {

            synchronized (c) {
                SocketAddress from;
                //System.out.println(this.addr + " recv");
                while ((from = c.receive(b.rewind())) != null) {
                    if (!isRunning())
                        return false;
                    in((InetSocketAddress) from, b.array(), b.position());
                }
            }
        } catch (IOException t) {
            logger.error("recv", t);
        }
        return true;
    }

    protected SocketAddress receive(ByteBuffer byteBuffer, SelectionKey selectionKey) throws IOException {
        return c.receive(byteBuffer);
    }

    public boolean out(String data, String host, int port) throws UnknownHostException {
        return out(data.getBytes(), host, port);
    }

    public boolean out(byte[] data, int port) {
        return sendRaw(data, new InetSocketAddress(port));
    }

    public boolean out(byte[] data, String host, int port) throws UnknownHostException {
        return sendRaw(data, new InetSocketAddress(InetAddress.getByName(host), port));
    }

    public boolean outJSON(Object x, InetSocketAddress addr) {


        byte[] b;
        try {
            b = Serials.toBytes(x);
        } catch (JsonProcessingException e) {
            logger.error("outJSON", e);
            return false;
        }

        return sendRaw(b, addr);
    }


    boolean sendRaw(byte[] data, InetSocketAddress to) {
        return sendRaw(data, 0, data.length, to);
    }

    boolean sendRaw(byte[] data, int dataFrom, int dataTo, InetSocketAddress to) {
        try {
            //System.out.println(this.addr + " send " + to);

            int sent = c.send(ByteBuffer.wrap(data, dataFrom, dataTo), to);
            if (sent < data.length) {
                logger.warn("output overflow: {}/{} bytes sent to {}", sent, data.length, to);
            }
            return true;
        } catch (Exception e) {
            logger.error("send {} {}", to, e.getMessage());
            return false;
        }
    }


    /**
     * override in subclasses
     */
    protected void in(InetSocketAddress msgOrigin, byte[] data, int position) {

    }


}
























































