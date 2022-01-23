package jcog;

import com.google.common.io.Resources;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;

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


    /**
     * @param key
     * @param def   default value
     * @param quiet log or not
     */
    @Nullable public static String get(String key, @Nullable Object def, boolean quiet) {

        String defString = def != null ? def.toString() : null;
        //Intrinsics.checkParameterIsNotNull(configKey, "configKey");

        //Intrinsics.checkExpressionValueIsNotNull(var10000, "(this as java.lang.String).toLowerCase()");

        String javapropname = key.toLowerCase().replace('_', '.');//, false, 4, (Object)null);

        String y = System.getenv(key); //HACK

        if (y == null) {
            y = System.getProperty(javapropname);
            if (y == null) {
                y = System.getenv(javapropname);
                if (y == null)
                    y = System.getProperty(key);
            }
        }

        if (y != null) {

            y = Str.unquote(y);

            System.setProperty(javapropname, y);
            if (!quiet)
                report(javapropname, y);

            return y;

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

}