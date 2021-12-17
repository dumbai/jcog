package jcog.exe;

import jcog.data.map.CustomConcurrentHashMap;

import java.util.concurrent.atomic.AtomicBoolean;

import static jcog.data.map.CustomConcurrentHashMap.*;

public class RunUnique {
    private static final CustomConcurrentHashMap<Object, AtomicBoolean> uniqueRuns =
            new CustomConcurrentHashMap<>(WEAK, IDENTITY, STRONG, IDENTITY, 128);

    public static boolean runUnique(Object key, Runnable r) {
        AtomicBoolean b = uniqueRuns.computeIfAbsent(key, z -> new AtomicBoolean());
        if (b.weakCompareAndSetAcquire(false, true)) {
            Exe.run(() -> {
                try {
                    r.run();
                } finally {
                    b.setRelease(false);
                }
            });
            return true;
        }
        return false;
    }
}