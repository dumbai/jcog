package jcog.net;

import jcog.io.Serials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * https:
 */
public abstract class UDiscover<P> {

    protected static final Logger logger = LoggerFactory.getLogger(UDiscover.class);

    static final String address = "228.5.6.7";
    static final int port = 6576;
    static final int MAX_PAYLOAD_ID = 256;

    /**
     * SO_TIMEOUT value
     */
    private static final int TIMEOUT_MS = 500;
    final AtomicBoolean busy = new AtomicBoolean(false);
    private final P id;
    MulticastSocket ms;
    private DatagramPacket p;
    private DatagramPacket q;
    private byte[] myID;


    public UDiscover(P payloadID) {
        this.id = payloadID;
    }


    protected abstract void found(P theirs, InetAddress who, int port);


    public void start() {
        //synchronized (this) {


            try {
                //.getLocalHost();
                //.getLoopbackAddress();
                InetAddress ia = InetAddress
                        //.getLocalHost();
                        //.getLoopbackAddress();
                        .getByName(address);


                ms = new MulticastSocket(port);

                ms.setBroadcast(true);


                ms.setReuseAddress(true);
                ms.setTimeToLive(3);


                ms.setSoTimeout(TIMEOUT_MS);
                ms.joinGroup(ia);
                //ms.joinGroup(new InetSocketAddress(ia, port), null);
                //System.out.println("ttl=" + ms.getTimeToLive());

                //HACK
//                NetworkInterface bestNic = NetworkInterface.networkInterfaces().max(Comparator.comparingInt(n -> n.getInterfaceAddresses().size())).get();
//                ms.setNetworkInterface(bestNic);
                //System.out.println("nic=" + ms.getNetworkInterface());

                myID = Serials.toBytes(id);
                p = new DatagramPacket(myID, myID.length, ia, port);
                byte[] theirID = new byte[MAX_PAYLOAD_ID];
                q = new DatagramPacket(theirID, theirID.length);

                busy.set(false);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        //}
    }


    public boolean update() {

        MulticastSocket ms = this.ms;
        if (ms == null)
            return false;

        if (!busy.compareAndSet(false, true))
            return false;

        try {



            //logger.info("send... {}", Thread.currentThread());
            try {
                ms.send(p);
            } catch (IOException e) {
                logger.warn("{}", e);
            }


            try {
                //logger.info("recv... {}", Thread.currentThread());
                ms.receive(q);
                try {

                    int len = q.getLength();
                    byte[] qd = q.getData();
                    if (!Arrays.equals(p.getData(), qd) && !Arrays.equals(myID, 0, myID.length, qd, 0, len)) {
                        P theirPayload = (P) Serials.fromBytes(qd, len, id.getClass());
                        found(theirPayload, q.getAddress(), q.getPort());

                    }
                    Arrays.fill(qd, (byte) 0);
                } catch (Exception e) {
                    logger.error("deserializing {}", e);
                }
            } catch (SocketTimeoutException ignored) {
                //ignored.printStackTrace();
            }

        } catch (Exception e) {
            logger.error("{} {}", this, e);
        } finally {
            busy.set(false);
        }

        return true;
    }

    public void stop() {
        //synchronized (this) {
        //busy.setAt(true);
        if (ms != null) {
            ms.close();
            ms = null;
        }
        //}
    }


}
































































































































































































































































































































































































































