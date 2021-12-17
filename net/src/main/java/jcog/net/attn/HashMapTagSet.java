package jcog.net.attn;

import com.fasterxml.jackson.core.JsonProcessingException;
import jcog.Util;
import jcog.io.Serials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static jcog.pri.Prioritized.EPSILON;

/**
 * Created by me on 5/2/17.
 */
public class HashMapTagSet implements TagSet, Serializable {

    public static final HashMapTagSet EMPTY = new HashMapTagSet() {

        @Override
        public float pri(String tag) {
            return 0f;
        }

        @Override
        public boolean pri(String tag, float pri) {
            return false;
        }
    };


    public String id;
    public ConcurrentHashMap<String, Float> data;

    protected HashMapTagSet() {
        this.id = "";
        this.data = new ConcurrentHashMap<>();
    }

    public HashMapTagSet(@NotNull String id) {
        this.id = id;
        this.data = new ConcurrentHashMap<>();
    }

    public void add(float priDividedAmong, String... tags) {
        float each = priDividedAmong / tags.length;
        for (String t : tags)
            add(t, each);
    }

    @Override
    public String toString() {
        return id + '=' + data;
    }

    @Override
    public @NotNull String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashMapTagSet h)) return false;
        return id.equals(h.id) && data.equals(h.data);
    }

    @Override
    public int hashCode() {
        return Util.hashCombine(super.hashCode(), id.hashCode());
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public boolean pri(String tag, float pri) {
        pri = Util.unitize(pri);
        if (Util.equals(pri, 0f, EPSILON))
            return data.remove(tag) != null;
        else {
            Float existing = data.put(tag, pri);
            return existing == null || !Util.equals(existing, EPSILON);
        }
    }

    public boolean add(String tag, float pri) {
        if (pri <= EPSILON)
            return false;

        boolean[] mod = {true};
        data.merge(tag, pri, (existing, added) -> {
            float next = Util.unitize(existing + added );
            mod[0] = !Util.equals(existing,next, EPSILON);
            return next;
        });

        return mod[0];
    }

    /**
     * mix another tagset in
     */
    public boolean add(HashMapTagSet tag, float pri) {
        float p = Util.unitize(pri);

        boolean[] modified = {false};
        for (Map.Entry<String, Float> entry : tag.data.entrySet()) {
            String k = entry.getKey();
            Float v = entry.getValue();
            modified[0] |= add(k, v * p);
        }

        return modified[0];
    }

    @Override
    public float pri(String tag) {
        Float f = data.get(tag);
        if (f == null)
            return 0;
        return f;
    }

    public byte[] toBytes() {
        try {
            return Serials.toBytes(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nullable HashMapTagSet fromBytes(byte[] b) {
        try {
            return Serials.fromBytes(b, HashMapTagSet.class);
        } catch (IOException e) {
            return null;
        }
    }
}