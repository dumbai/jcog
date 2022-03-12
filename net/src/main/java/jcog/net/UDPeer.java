package jcog.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import jcog.Log;
import jcog.Str;
import jcog.Util;
import jcog.data.byt.DynBytes;
import jcog.event.ListTopic;
import jcog.event.Topic;
import jcog.exe.Every;
import jcog.io.BinTxt;
import jcog.io.Serials;
import jcog.math.RecycledSummaryStatistics;
import jcog.net.attn.HashMapTagSet;
import jcog.pri.Prioritizable;
import jcog.pri.bag.Bag;
import jcog.pri.bag.impl.HijackBag;
import jcog.pri.bag.impl.hijack.PriPriHijackBag;
import jcog.pri.op.PriMerge;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.signal.NumberX;
import jcog.util.ArrayUtil;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static jcog.net.UDPeer.Command.*;
import static jcog.net.UDPeer.Msg.ADDRESS_BYTES;

/**
 * UDP peer - self-contained generic p2p/mesh network node
 * <p>
 * see:
 * Gossip Protocols
 * Gnutella
 * GNU WASTE
 * <p>
 * TODO https://github.com/komamitsu/phi-accural-failure-detector/blob/master/src/main/java/org/komamitsu/failuredetector/PhiAccuralFailureDetector.java
 */
public class UDPeer extends UDP {

    protected static final Logger logger = Log.log(UDPeer.class);


    /**
     * active routing table capacity
     * TODO make this IntParam mutable
     */
    private static final int PEERS_CAPACITY = 64;
    /**
     * message memory
     */
    private static final int SEEN_CAPACITY = 32 * 1024;
    private static final int UNKNOWN_ID = Integer.MIN_VALUE;


    private static final byte DEFAULT_PING_TTL = 2;
    private static final byte DEFAULT_ATTN_TTL = DEFAULT_PING_TTL;

    public final Bag<Integer, UDProfile> them;

    public final PriPriHijackBag<Msg> seen;

    public final UDiscover<Discoverability> discover;
    private Every discoverEvery = Every.Never;

    /**
     * TODO use a variable size identifier, 32+ bit. ethereumj uses 512bits.
     * smaller id's will work better for closed networks with a known population
     */
    public final int me;
    public final Topic<MsgReceived> receive = new ListTopic<>();
    private final Random rng;
//    private final AtomicBoolean needChanged = new AtomicBoolean(false);
//    private final AtomicBoolean canChanged = new AtomicBoolean(false);
//    public final HashMapTagSet can = new HashMapTagSet("C");
//    public final HashMapTagSet need = new HashMapTagSet("N");
    /**
     * rate of sharing peer needs
     */
    //public final FloatRange empathy = new FloatRange(0.5f, 0f, 1f);

    public UDPeer() throws IOException {
        this(true);
    }

    /**
     * assigned a random port
     */
    public UDPeer(boolean discovery) throws IOException {
        this(InetAddress.getLocalHost(), 0, discovery);
    }

    public UDPeer(int port) throws IOException {
        this(InetAddress.getLocalHost(), port, true);
    }

    public UDPeer(InetAddress address, int port) throws IOException {
        this(address, port, false);
    }

    public UDPeer(int port, boolean discovery) throws IOException {
        this(InetAddress.getLocalHost(), port, discovery);
    }

    public UDPeer(InetAddress address, int port, boolean discovery) throws IOException {
        super(address, port);

        this.rng = new XoRoShiRo128PlusRandom(System.nanoTime());

        int me;
        while ((me = (int) (UUID.randomUUID().getLeastSignificantBits() & 0xffff)) == UNKNOWN_ID) ;
        this.me = me;

        them = new HijackBag<>(3) {

            @Override
            public void onAdd(UDProfile p) {
                logger.debug("connect {}", p);
                onAddRemove(p, true);
            }

            @Override
            public void onRemove(UDProfile p) {
                logger.debug("disconnect {}", p);
                onAddRemove(p, false);
            }

            @Override
            protected UDProfile merge(@Nullable UDProfile existing, UDProfile incoming, NumberX overflowing) {
                return (existing != null ? existing : incoming);
            }

            @Override
            public Consumer<UDProfile> forget(float rate) {
                return null;
            }

            /** TODO refine */
            @Override public float pri(UDProfile key) {
                long latency = key.latency();
                return 1f / (1f + Util.sqr(latency / 100f));
            }

            @Override
            public Integer key(UDProfile value) {
                return value.id;
            }

        };

        them.setCapacity(PEERS_CAPACITY);

        seen = new PriPriHijackBag<>(PriMerge.plus, SEEN_CAPACITY, 3) {

            @Override
            public Consumer<Msg> forget(float rate) {
                return null;
            }
        };

        discover = discovery ? new UDiscover<>(new Discoverability(me, addr)) {
            @Override
            protected void found(Discoverability who, InetAddress addr, int port) {
                //TODO hard exclude the UDPeer itself (ie. if addr and port equal)
                if (!who.addr.equals(UDPeer.this.addr) && !them.contains(who.id)) {
                    logger.debug("discovered {} at {}:{}", who.id, who.addr);
                    ping(who.addr);
                }
            }
        } : null;

    }

    public static byte[] bytes(InetSocketAddress addr) {
        byte[] x = new byte[ADDRESS_BYTES];
        int port = addr.getPort();
        x[0] = (byte) ((port >> 8) & 0xff);
        x[1] = (byte) (port & 0xff);
        ipv6(addr.getAddress().getAddress(), x, 2);
        return x;
    }

    private void onAddRemove(UDProfile p, boolean addedOrRemoved) {

    }


    public final boolean connected() {
        return !them.isEmpty();
    }

    /**
     * broadcast
     * TODO handle oversized message
     *
     * @return how many sent
     */
    private int tellSome(Msg o, float pri, boolean onlyIfNotSeen) {

        if (!connected() || pri <= 0) {

            return 0;
        } else {

            if (onlyIfNotSeen && seen(o, pri))
                return 0;

            byte[] bytes = o.arrayCompactDirect();

            int[] remain = {Math.round(them.size() * pri)};
            them.sample(rng, (Predicate<UDProfile>) ((to) -> {
                if (o.id() != to.id /*&& (pri >= 1 || rng.nextFloat() <= pri)*/)
                    sendRaw(bytes, to.addr);

                return ((remain[0]--) > 0);
            }));
            return remain[0];

        }
    }

    @Override
    protected void starting() {
        //synchronized (this) {
        super.starting();
        if (discover != null) {
            discover.start();
            discoverEvery = new Every(discover::update, 250);
        } else {
            discoverEvery = Every.Never;
        }
        //}
    }

    @Override
    protected void stopping() {
        //synchronized (this) {
        if (discover != null) {
            discover.stop();
            discoverEvery = Every.Never;
        }
        them.clear();
        super.stopping();
        //}
    }


    public boolean seen(Msg o, float pri) {
        o.priAdd(pri);
        return seen.put(o) != o;
    }

    protected void tellSome(byte[] msg, int ttl) {
        tellSome(msg, ttl, false);
    }

    @Deprecated public int tellSome(byte[] msg, int ttl, boolean onlyIfNotSeen) {
        return tellSome(TELL.id, msg, ttl, onlyIfNotSeen);
    }

    public int tellSome(byte cmd, byte[] msg, int ttl, boolean onlyIfNotSeen) {
        Msg x = new Msg(cmd, (byte) ttl, me, null, msg);
        int y = tellSome(x, 1f, onlyIfNotSeen);
        seen(x, 1f);
        return y;
    }

    public int tellSome(byte cmd, Object msg, int ttl) throws JsonProcessingException {
        return tellSome(cmd, msg, ttl, false);
    }

    @Deprecated public int tellSome(Object msg, int ttl, boolean onlyIfNotSeen) throws JsonProcessingException {
        return tellSome(TELL.id, msg, ttl, onlyIfNotSeen);
    }

    public int tellSome(byte cmd, Object msg, int ttl, boolean onlyIfNotSeen) throws JsonProcessingException {
        byte[] b = Serials.toBytes(msg, Object.class);
        return tellSome(cmd, b, ttl, onlyIfNotSeen);
    }

    public int tellSome(JsonNode msg, int ttl, boolean onlyIfNotSeen) throws JsonProcessingException {
        byte[] b = Serials.toBytes(msg, JsonNode.class);
        return tellSome(b, ttl, onlyIfNotSeen);
    }

    public final void send(byte cmd, Object o, UDProfile p) throws JsonProcessingException {
        send(cmd, o, p.addr);
    }

    public void send(byte cmd, Object o, InetSocketAddress to) throws JsonProcessingException {
        byte[] payload = Serials.toBytes(o, Object.class);
        send(new Msg(cmd, (byte) 1, me, addr, payload), to);
    }

    /**
     * send to a specific known recipient
     */
    public void send(Msg o, InetSocketAddress to) {
        sendRaw(o.arrayDirect(), 0, o.length, to);
    }

    @Override
    public boolean next() {

        discoverEvery.next();

        if (!super.next())
            return false;

        seen.commit();

//        boolean updateNeed, updateCan;
//        if (needChanged.compareAndSet(true, false)) {
//            updateNeed = true;
//            tellSome(need);
//            onUpdateNeed();
//        }
//
//        if (canChanged.compareAndSet(true, false)) {
//            updateCan = true;
//            tellSome(can);
//        }

        return true;
    }

//    protected void tellSome(HashMapTagSet setAt) {
//        tellSome(new Msg(ATTN.id, DEFAULT_ATTN_TTL, me, null,
//                setAt.toBytes()), 1f, false);
//    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + name() + ')';
    }

    public String name() {
        return BinTxt.toString(me);
    }

    @Override
    protected void in(InetSocketAddress p, byte[] data, int len) {

        Msg m = new Msg(data, len);
        if (/*m == null || */m.id() == me)
            return;

        boolean seen = seen(m, 1f);
        if (seen)
            return;

//        Command cmd = Command.get(m.cmd());
//        if (cmd == null)
//            return;

        if (m.port() == 0) {
            byte[] msgOriginBytes = bytes(p);
            if (!m.originEquals(msgOriginBytes))
                m = m.clone(msgOriginBytes);
        }

        //m.compact(inputArray, false);

        long now = System.currentTimeMillis();

        @Nullable UDProfile from = them.get(m.id());
        if (from!=null)
            from.lastMessage = now;


        byte cmd = m.cmd();
        //            case ATTN:
        //                if (you != null) {
        //                    HashMapTagSet h = HashMapTagSet.fromBytes(m.data());
        //                    if (h != null) {
        //
        //                        switch (h.id()) {
        //                            case "C":
        //                                you.can = h;
        //
        //                                break;
        //                            case "N":
        //                                you.need = h;
        //                                need(h, empathy.floatValue());
        //                                break;
        //                            default:
        //                                return;
        //                        }
        //                        if (logger.isDebugEnabled())
        //                            logger.debug("{} attn {}", you.name(), h);
        //                    }
        //                }
        //                break;
        switch (cmd) {
            case 'p' -> from = onPong(p, m, from, now);
            case 'P' -> sendPong(p, m);
            case 't' -> receive(from, m);
        }

        accept(m);

        if (from == null && m.cmd()!='p') {
            //ping an unknown sender
            if (them.size() < them.capacity())
                ping(p);
        }

        boolean live = m.live();
        if (live && share(m,from))
            tellSome(m, 1f, false /* did a test locally already */);
    }

    /** handle additional message types */
    protected void accept(Msg m) {
        //nothing
    }

    protected static boolean share(Msg m, @Nullable UDProfile from) {
        return from!=null; //default: only share from known peers
    }

    protected void receive(@Nullable UDProfile from, Msg m) {
        receive.accept(() -> new MsgReceived(m, from));
    }

    private RecycledSummaryStatistics latencyAvg() {
        RecycledSummaryStatistics r = new RecycledSummaryStatistics();
        for (UDProfile x : them)
            r.accept(x.latency());

        return r;
    }

    public String summary() {
        return them.size() + " peers, (avg latency=" + latencyAvg() + ')';
    }

    /**
     * ping same host, different port
     */
    public void ping(int port) {
        ping(new InetSocketAddress(port));
    }

    public void ping(String host, int port) {
        ping(new InetSocketAddress(host, port));
    }

    public void ping(InetAddress host, int port) {
        ping(new InetSocketAddress(host, port));
    }

    public void ping(@Nullable UDPeer x) {
        assert (this != x);
        ping(x.addr);
    }

    public void ping(@Nullable InetSocketAddress to) {
        if (to.equals(UDPeer.this.addr))
            return;
        //throw new UnsupportedOperationException("dont ping self");

        //if (logger.isDebugEnabled())
        logger.info("{} {} ping {}", name(), addr, to);
        send(ping(), to);
    }


    private Msg ping() {
        return new Msg(PING.id, DEFAULT_PING_TTL, me, null, System.currentTimeMillis());
    }


    private @Nullable UDProfile onPong(InetSocketAddress p, Msg m, @Nullable UDProfile connected, long now) {

        long sent = m.dataLong(0);
        long latencyMS = now - sent;

        //if (logger.isDebugEnabled())
        logger.info("{} pong {} from {} ({})", name(), m, connected, Str.timeStr(1E6 * latencyMS));

        if (connected != null) {
            connected.onPing(latencyMS);
        } else {
            int pinger = m.dataInt(8, UNKNOWN_ID);
            if (pinger == me) {
                connected = them.put(new UDProfile(m.id(), p, latencyMS));
            }
        }
        return connected;
    }

    private void sendPong(InetSocketAddress from, Msg ping) {
        Msg m =
                new Msg(PONG.id, (byte) 1, me, from,
                        ArrayUtil.addAll(
                                Longs.toByteArray(ping.dataLong(0)),
                                Ints.toByteArray(ping.id())
                        ));

        //if (logger.isDebugEnabled())
        logger.info("{} pong {} to {}", addr, m, from);

        seen(m, 1f);
        send(m, from);
    }


//    public void can(String tag, float pri) {
//        if (can.addAt(tag, pri))
//            canChanged.setAt(true);
//    }
//
//    public void need(String tag, float pri) {
//        if (need.addAt(tag, pri))
//            needChanged.setAt(true);
//    }
//
//    public void need(HashMapTagSet tag, float pri) {
//        if (need.addAt(tag, pri))
//            needChanged.setAt(true);
//    }

    public enum Command {

        /**
         * measure connectivity
         */
        PING('P'),

        /**
         * answer a ping
         */
        PONG('p'),

        /**
         * ping / report known peers?
         */
        WHO('w'),

//        /**
//         * share my attention
//         */
//        ATTN('a'),

        /**
         * share a belief claim
         */
        TELL('t'),
        ;

        public final byte id;


        Command(char id) {
            this.id = (byte) id;
        }

        public static @Nullable Command get(byte cmdByte) {
            //                case 'a':
            //                    return ATTN;
            return switch (cmdByte) {
                case 'P' -> PING;
                case 'p' -> PONG;
                case 't' -> TELL;
                default -> null;
            };
        }
    }

    static class Discoverability implements Serializable {
        public int id;
        public InetSocketAddress addr;

        Discoverability() {
        }

        Discoverability(int id, InetSocketAddress addr) {
            this.id = id;
            this.addr = addr;
        }
    }

    /**
     * Msg extended with a UDProfile instance
     */
    public static class MsgReceived extends Msg {

        public final @Nullable UDProfile from;

        MsgReceived(Msg m, @Nullable UDProfile from) {
            super(m.arrayCompactDirect());
            this.from = from;
        }

    }

    public static class Msg extends DynBytes implements Prioritizable {

        static final int TTL_BYTE = 0;
        static final int CMD_BYTE = 1;
        static final int ID_BYTE = 2;
        static final int PORT_BYTE = 6;
        static final int ORIGIN_BYTE = 8;
        static final int DATA_START_BYTE = 24;

        static final int HEADER_SIZE = DATA_START_BYTE;
        static final int ADDRESS_BYTES = 16 /* ipv6 */ + 2 /* port */;

        final int hash;
        private float pri;

        Msg(byte[] data, int len) {
            super(data, len);
            hash = hash();
        }

        Msg(byte... data) {
            this(data, data.length);
        }

        public Msg(byte cmd, byte ttl, int id, InetSocketAddress origin, byte[] payload) {
            super(HEADER_SIZE + payload.length);
            init(cmd, ttl, id, origin);

            if (payload.length > 0)
                write(payload);

            hash = hash();
        }

        Msg(byte cmd, byte ttl, int id, InetSocketAddress origin, long payload) {
            super(HEADER_SIZE + 8);
            init(cmd, ttl, id, origin);

            writeLong(payload);

            hash = hash();
        }

        @Override
        public void priAdd(float a) {
            this.pri += a;
        }

        @Override
        public final float pri() {
            return pri;
        }

        private void init(byte cmd, byte ttl, int id, @Nullable InetSocketAddress origin) {
            writeByte(ttl);
            writeByte(cmd);
            writeInt(id);

            if (origin != null) {
                write(bytes(origin));
            } else {
                fillBytes((byte) 0, ADDRESS_BYTES);
            }


        }

        private int hash() {
            compact();
            /* skip TTL byte */
            return Util.hash(this, 1, length);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            Msg m = (Msg) obj;
            if (hash == m.hash) {
                int len = this.length;
                return m.length == len && Arrays.equals(m.bytes, 1, len, bytes, 1, len);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        public byte cmd() {
            return bytes[CMD_BYTE];
        }

        public byte ttl() {
            return bytes[TTL_BYTE];
        }

        public boolean live() {
            int ttl = ttl();
            if (ttl <= 0)
                return false;
            return (--bytes[TTL_BYTE]) >= 0;
        }

        @Override
        public String toString() {

            return BinTxt.toString(id()) + ' ' +
                    ((char) cmd()) + '+' + ttl() +
                    '[' + dataLength() + ']';


        }


        /**
         * clones a new copy with different command
         */
        public Msg clone(byte newCmd) {
            byte[] b = Arrays.copyOf(bytes, length);
            b[CMD_BYTE] = newCmd;
            return new Msg(b);
        }

        public Msg clone(@Nullable byte[] newOrigin) {
            return clone(cmd(), newOrigin);
        }

        public Msg clone(byte newCmd, @Nullable byte[] newOrigin) {
            byte[] b = Arrays.copyOf(bytes, length);
            b[CMD_BYTE] = newCmd;

            if (newOrigin != null) {
                System.arraycopy(newOrigin, 0, b, PORT_BYTE, ADDRESS_BYTES);
            } else {
                Arrays.fill(b, PORT_BYTE, ADDRESS_BYTES, (byte) 0);
            }
            return new Msg(b);
        }

        public Msg clone(byte newCmd, int id, @Nullable byte[] newOrigin) {
            byte[] b = Arrays.copyOf(bytes, length);
            b[CMD_BYTE] = newCmd;

            System.arraycopy(Ints.toByteArray(id), 0, b, ID_BYTE, 4);

            if (newOrigin != null) {
                System.arraycopy(newOrigin, 0, b, PORT_BYTE, ADDRESS_BYTES);
            } else {
                Arrays.fill(b, PORT_BYTE, ADDRESS_BYTES, (byte) 0);
            }
            return new Msg(b);
        }

        int dataLength() {
            return length() - DATA_START_BYTE;
        }

        public byte[] data() {
            return data(0, dataLength());
        }

        public byte[] data(int start, int end) {
            return Arrays.copyOfRange(bytes, DATA_START_BYTE + start, DATA_START_BYTE + end);
        }

        public int id() {
            byte[] b = bytes;
            return Ints.fromBytes(
                    b[ID_BYTE], b[ID_BYTE + 1], b[ID_BYTE + 2], b[ID_BYTE + 3]
            );
        }

        /**
         * the payload as a long
         */
        long dataLong(int offset) {
            byte[] b = this.bytes;
            offset += DATA_START_BYTE;
            if (b.length < offset + 8)
                throw new RuntimeException("missing 64-bit payload");

            return Longs.fromBytes(
                    b[offset++], b[offset++], b[offset++], b[offset++],
                    b[offset++], b[offset++], b[offset++], b[offset]
            );
        }

        int dataInt(int offset, int ifMissing) {
            byte[] b = this.bytes;
            offset += DATA_START_BYTE;
            if (b.length < offset + 4)
                return ifMissing;

            return Ints.fromBytes(
                    b[offset++], b[offset++], b[offset++], b[offset++]
            );
        }


        boolean originEquals(byte[] addrBytes) {
            int addrLen = addrBytes.length;
            return Arrays.equals(bytes, PORT_BYTE, PORT_BYTE + addrLen, addrBytes, 0, addrLen);
        }


        public @Nullable InetSocketAddress origin() {
            int port = this.port();
            try {
                InetAddress aa = InetAddress.getByAddress(Arrays.copyOfRange(bytes, ORIGIN_BYTE, ORIGIN_BYTE + 16));
                return new InetSocketAddress(aa, port);
            } catch (UnknownHostException e) {
                return null;
            }

        }

        public int port() {
            int firstByte = (0x000000FF & (bytes[PORT_BYTE]));
            int secondByte = (0x000000FF & (bytes[PORT_BYTE + 1]));
            return (char) (firstByte << 8 | secondByte);

        }


        @Override
        public void pri(float p) {
            this.pri = Util.unitize(p);
        }

        @Override
        public boolean delete() {
            if (pri == pri) {
                this.pri = Float.NaN;
                return true;
            }
            return false;
        }

    }

    /**
     * profile of another peer
     */
    public static class UDProfile {
        public final InetSocketAddress addr;

        public final int id;
        /**
         * ping time, in ms
         * TODO find a lock-free sort of statistics class
         */
        final Histogram pingTime = new ConcurrentHistogram(1, 16 * 1024, 0);
        /**
         * caches the value of the mean pingtime
         */
        final AtomicLong latency = new AtomicLong(Long.MAX_VALUE);
        final byte[] addrBytes;
        long lastMessage = Long.MIN_VALUE;
        HashMapTagSet
                can = HashMapTagSet.EMPTY;
        HashMapTagSet need = HashMapTagSet.EMPTY;

        public UDProfile(int id, InetSocketAddress addr, long initialPingTime) {
            this.id = id;
            this.addr = addr;
            this.addrBytes = bytes(addr);
            onPing(initialPingTime);
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            return id == ((UDProfile) obj).id;

        }

        void onPing(long timeMS) {
            pingTime.recordValue(Math.max(1, timeMS));
            latency.set(Math.round(pingTime.getMean()));
        }

        /**
         * average ping time in ms
         */
        public long latency() {
            return latency.get();
        }

        @Override
        public String toString() {
            return name() + '{' +
                    "addr=" + addr +
                    ", ping=" + latency() +
                    ", can=" + can +
                    ", need=" + need +
                    '}';
        }


        public String name() {
            return BinTxt.toString(id);
        }
    }


}