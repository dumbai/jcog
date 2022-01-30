package jcog;

import com.google.common.io.Resources;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public enum Config {
    ;

    private static final Logger logger = Log.log(Config.class);

    static {
        //HACK

        String defaultsFile = "defaults.ini";
        try {
            System.getProperties().load(Resources.getResource(defaultsFile).openStream());
        } catch (IllegalArgumentException | IOException e) {
            //logger.warn("{} missing", defaultsFile);
            //e.printStackTrace();
        }
    }


    @Nullable public static String get(String key, @Nullable Object def) {
        return get(key, def, false);
    }


    private static final Map<String,Object> config = new ConcurrentHashMap();

    /**
     * @param key
     * @param def   default value
     * @param quiet log or not
     */
    @Nullable public static String get(String key, @Nullable Object def, boolean quiet) {

        String defString = def != null ? def.toString() : null;
        //Intrinsics.checkParameterIsNotNull(configKey, "configKey");

        //Intrinsics.checkExpressionValueIsNotNull(var10000, "(this as java.lang.String).toLowerCase()");

        String k = key.toLowerCase().replace('_', '.');//, false, 4, (Object)null);

        String v = System.getenv(key); //HACK

        if (v == null) {
            v = System.getProperty(k);
            if (v == null) {
                v = System.getenv(k);
                if (v == null)
                    v = System.getProperty(key);
            }
        }

        if (v != null) {

            v = Str.unquote(v);

            System.setProperty(k, v);

            config.put(k,v);
            if (!quiet)
                report(k, v);

            return v;

        } else {
//            if (defString == null)
//                throw new RuntimeException("configuration unknown: " + key);

            return defString;
        }
    }

    private static String report(String property, String val) {
        logger.info("-D{}={}", property, val);
        return val;
    }

    public static int INT(String key, int def) {
        return Integer.parseInt(get(key, def));
    }

    public static float FLOAT(String key, float def) {
        return Float.parseFloat(get(key, def));
    }

    public static double DOUBLE(String key, double def) {
        return Double.parseDouble(get(key, def));
    }

    public static boolean IS(String key, boolean def) {
        return IS(key, def ? +1 : 0);
    }

    public static boolean IS(String key) {
        return IS(key, -1);
    }

    public static boolean IS(String key, int def) {
        return switch (get(key, "")) {
            case "true", "t", "yes", "y", "1" -> true;
            case "false", "f", "no", "n", "0" -> false;
            default -> {
                if (def < 0)
                    throw new UnsupportedOperationException(key + ": expected boolean value");
                yield def == 1;
            }
        };
    }

    public static void forEach(BiConsumer<String,Object> o) {
        config.forEach(o);
    }
}