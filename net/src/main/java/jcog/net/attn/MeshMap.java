package jcog.net.attn;

import com.fasterxml.jackson.core.JsonProcessingException;
import jcog.event.ListTopic;
import jcog.event.Topic;
import jcog.io.Serials;
import jcog.net.UDPeer;
import org.eclipse.collections.api.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/** UDPeer-backed lossy hashmap with event notification */
public class MeshMap<K,V> extends UDPeer /* implements Map<K,V>*/ {

    static final float seedFPS = 4;
    static final float mapFPS = 4;

    static final ConcurrentHashMap<String,MeshMap> the = new ConcurrentHashMap<>();
    static final UDPeer seed;
    static {
        try {
            seed = new UDPeer();
            seed.fps(seedFPS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** id of this map */
    private final byte[] map;
    private final BiConsumer<K,V> model;

    public MeshMap(String id, BiConsumer<K,V> m) throws IOException {
        super();
        this.map = id.getBytes();
        this.model = m;
    }

    final Topic<MeshMap<K,V>> onStart = new ListTopic<>();
    final Topic<Pair<K,V>> onRecv = new ListTopic<>();


    @Override
    protected void starting() {
        super.starting();
        onStart.accept(this);
    }

    public void put(K k, V v) {
        try {
            tellSome(Serials.toBytes(new MapKeyValue(map, k, v)), 3);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static class MapKeyValue implements Serializable {
        public byte[] map;
        public Object key;
        public Object val;

        public MapKeyValue() {

        }

        public MapKeyValue(byte[] m, Object k, Object v) {
            set(m, k, v);
        }

        public void set(byte[] map, Object k, Object v) {
            this.map = map;
            this.key = k;
            this.val = v;
        }
    }

    @Override
    protected void receive(@Nullable UDPeer.UDProfile connected, @NotNull UDPeer.Msg m) {

        try {
            MapKeyValue mkv = Serials.fromBytes(m.data(), MapKeyValue.class);
            if (Arrays.equals(map, mkv.map)) {
                
                K k = (K)mkv.key;
                V v = (V)mkv.val;
                
                model.accept(k, v);
            }
        } catch (IOException e) {
            logger.error("{}",e);
        }

    }

    public static <K,V> MeshMap<K,V> get(String id, BiConsumer<K,V> x) {
        return the.computeIfAbsent(id, i -> {
            try {
                MeshMap<K, V> y = new MeshMap<>(id, x);
                y.fps(mapFPS);
                return y;
            } catch (IOException e) {
                
                e.printStackTrace();
                return null;
            }
        });
    }
}