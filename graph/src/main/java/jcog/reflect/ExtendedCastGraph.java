/*
 * The MIT License
 *
 * Copyright 2015 Kamnev Georgiy (nt.gocha@gmail.com).
 *
 */

package jcog.reflect;

import jcog.Util;
import jcog.math.v2;
import jcog.math.v3;
import jcog.reflect.spi.GetTypeConvertor;
import jcog.signal.Tensor;
import jcog.signal.tensor.ArrayTensor;
import jcog.util.ArrayUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Kamnev Georgiy (nt.gocha@gmail.com)
 * @see xyz.cofe.typeconv.spi.GetTypeConvertor
 * TODO https://github.com/orika-mapper/orika/tree/master/core/src/main/java/ma/glasnost/orika/converter/builtin
 */

/**
 * Граф конвертирования типов с базовым набором типов. <br>
 * Базовые числа между собой: <br>
 * Byte, byte &lt;=&gt; byte, Byte <br>
 * Short, short &lt;=&gt; short, Short <br>
 * Integer, int &lt;=&gt; int, Integer <br>
 * Long, long &lt;=&gt; long, Long <br>
 * Float, float &lt;=&gt; float, Float <br>
 * Double, double &lt;=&gt; double, Double <br>
 * <br>
 * <p>
 * Числа между числами: <br>
 * Number, Byte &lt;=&gt; Byte, Number <br>
 * Number, Short &lt;=&gt; Short, Number <br>
 * Number, Integer &lt;=&gt; Integer, Number <br>
 * Number, Long &lt;=&gt; Long, Number <br>
 * Number, Float &lt;=&gt; Float, Number <br>
 * Number, Double &lt;=&gt; Double, Number <br>
 * <br>
 * <p>
 * BigInteger/BigDecimal между числами: <br>
 * Number, BigDecimal &lt;=&gt; BigDecimal, Number <br>
 * Number, BigInteger &lt;=&gt; BigInteger, Number <br>
 * <br>
 * <p>
 * boolean типы: <br>
 * Boolean, boolean &lt;=&gt; boolean, Boolean <br>
 * <br>
 * <p>
 * Символные типы: <br>
 * Character, char &lt;=&gt; char, Character <br>
 * <p>
 * char, Character &lt;=&gt; int, Integer <br>
 * char, Character &lt;=&gt; String <br>
 * <br>
 * <p>
 * boolean - числа - string типы: <br>
 * int, Integer &lt;=&gt; boolean, Boolean <br>
 * <p>
 * BigDecimal, BigInteger &lt;=&gt; BigInteger, BigDecimal <br>
 * <p>
 * Number &#x2192; String <br>
 * String &#x2192; Integer <br>
 * String &#x2192; int <br>
 * String &#x2192; Long <br>
 * String &#x2192; long <br>
 * String &#x2192; Double <br>
 * String &#x2192; double <br>
 * String &#x2192; BigDecimal <br>
 * <br>
 * <p>
 * Даты: <br>
 * java.util.Date &lt;=&gt; String <br>
 * java.util.Date &lt;=&gt; java.sql.Date <br>
 * java.util.Date &lt;=&gt; java.sql.Time <br>
 * java.util.Date &lt;=&gt; java.sql.Timestamp <br>
 * <p>
 * String &#x2192; java.sql.Date <br>
 * String &#x2192; java.sql.Time <br>
 * String &#x2192; java.sql.Timestamp <br>
 * <br>
 * <p>
 * Бинарные данные: <br>
 * byte[] &lt;=&gt; String <br>
 * Byte[] &lt;=&gt; String <br>
 * char[] &lt;=&gt; String <br>
 * Character[] &lt;=&gt; String <br>
 * <p>
 * java.sql.Clob &#x2192; String <br>
 * java.sql.NClob &#x2192; String <br>
 * <br>
 * <p>
 * Файлы (путь): <br>
 * java.net.URL &lt;=&gt; String <br>
 * java.net.URI &lt;=&gt; String <br>
 * java.io.File &lt;=&gt; String <br>
 * java.io.File &#x2192; URL <br>
 * java.io.File &#x2192; URI <br>
 * xyz.cofe.fs.File &lt;=&gt; String <br>
 * xyz.cofe.io.File &lt;=&gt; String <br>
 * xyz.cofe.io.File &lt;=&gt; java.nio.file.Path <br>
 * xyz.cofe.io.File &lt;=&gt; java.io.File <br>
 * java.nio.charset.Charset &lt;=&gt; String <br>
 *
 * @author Kamnev Georgiy (nt.gocha@gmail.com)
 */
public class ExtendedCastGraph extends CastGraph {


    //<ed_itor-fold defaultstate="collapsed" desc="Базовые типы">
    //<ed_itor-fold defaultstate="collapsed" desc="Числовые типы">
    //<ed_itor-fold defaultstate="collapsed" desc="числовые примитивы integer, byte, ... Integer, Byte, ...">
    //<ed_itor-fold defaultstate="collapsed" desc="integer - int">
    public static final Function int2Integer = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "int2Integer";
        }
    };
    public static final Function integer2Int = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "integer2Int";
        }
    };
    //<ed_itor-fold defaultstate="collapsed" desc="Byte - byte">
    public static final Function byte2Byte = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "byte2Byte";
        }
    };
    public static final Function Byte2byte = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Byte2byte";
        }
    };
    //<ed_itor-fold defaultstate="collapsed" desc="Short - short">
    public static final Function short2Short = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "short2Short";
        }
    };
    public static final Function Short2short = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Short2short";
        }
    };
    //<ed_itor-fold defaultstate="collapsed" desc="Long - long">
    public static final Function long2Long = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "long2Long";
        }
    };
    //</ed_itor-fold>
    public static final Function Long2long = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Long2long";
        }
    };
    //<ed_itor-fold defaultstate="collapsed" desc="Float - float">
    public static final Function float2Float = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Long2long";
        }
    };
    public static final Function Float2float = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Float2float";
        }
    };
    //<ed_itor-fold defaultstate="collapsed" desc="Double - double">
    public static final Function double2Double = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "double2Double";
        }
    };
    public static final Function Double2double = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Double2double";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="byte, short,int,... - Number">
    //<ed_itor-fold defaultstate="collapsed" desc="Number - Byte">
    public static final Function Byte2Number = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Byte2Number";
        }
    };
    public static final Function Number2Byte = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return ((Number) from).byteValue();
        }

        @Override
        public String toString() {
            return "Number2Byte";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="Number - Short">
    public static final Function Short2Number = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Short2Number";
        }
    };
    public static final Function Number2Short = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return ((Number) from).shortValue();
        }

        @Override
        public String toString() {
            return "Number2Short";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="Number - Integer">
    public static final Function Integer2Number = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Integer2Number";
        }
    };
    public static final Function Number2Integer = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return ((Number) from).intValue();
        }

        @Override
        public String toString() {
            return "Number2Integer";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="Number - Long">
    public static final Function Long2Number = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Long2Number";
        }
    };
    public static final Function Number2Long = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return ((Number) from).longValue();
        }

        @Override
        public String toString() {
            return "Number2Long";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="Number - Float">
    public static final Function Float2Number = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Float2Number";
        }
    };
    public static final Function Number2Float = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return ((Number) from).floatValue();
        }

        @Override
        public String toString() {
            return "Number2Float";
        }
    };
    //</ed_itor-fold>
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="Number - Double">
    public static final Function Double2Number = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Double2Number";
        }
    };
    public static final Function Number2Double = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return ((Number) from).doubleValue();
        }

        @Override
        public String toString() {
            return "Number2Double";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="Number - BigDecimal">
    public static final Function BigDecimal2Number = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return (((Number) from)).doubleValue();
        }

        @Override
        public String toString() {
            return "BigDecimal2Number";
        }
    };
    public static final Function Number2BigDecimal = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            if (from instanceof Byte b) {
                return new BigDecimal(b.intValue());
            }
            if (from instanceof Short s) {
                return new BigDecimal(s.intValue());
            }
            if (from instanceof Integer i) {
                return new BigDecimal(i);
            }
            if (from instanceof Long l) {
                return new BigDecimal(l);
            }
            if (from instanceof Float f) {
                return new BigDecimal(f);
            }
            if (from instanceof Double d) {
                return new BigDecimal(d);
            }
            if (from instanceof Number) {
                double d = ((Number) from).doubleValue();
                return new BigDecimal(d);
            }
            throw new Error("can't " + from + " cast to BigDecimal");
        }

        @Override
        public String toString() {
            return "Number2BigDecimal";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="Number - BigInteger">
    public static final Function BigInteger2Number = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "BigInteger2Number";
        }
    };
    public static final Function Number2BigInteger = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            if (from instanceof Byte b) {
                return BigInteger.valueOf(b);
            }
            if (from instanceof Short s) {
                return BigInteger.valueOf(s);
            }
            if (from instanceof Integer i) {
                return BigInteger.valueOf(i);
            }
            if (from instanceof Long l) {
                return BigInteger.valueOf(l);
            }
            if (from instanceof Float f) {
                return BigInteger.valueOf(f.longValue());
            }
            if (from instanceof Double d) {
                return BigInteger.valueOf(d.longValue());
            }
            if (from instanceof Number) {
                return BigInteger.valueOf(((Number) from).longValue());
            }
            throw new Error("can't " + from + " cast to BigInteger");
        }

        @Override
        public String toString() {
            return "Number2BigInteger";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="BigDecimal - BigInteger">
    public static final Function BigInteger2BigDecimal = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            BigInteger v = (BigInteger) from;
            return new BigDecimal(v);
        }

        @Override
        public String toString() {
            return "BigInteger2BigDecimal";
        }
    };
    public static final Function BigDecimal2BigInteger = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            BigDecimal v = (BigDecimal) from;
            return v.toBigInteger();
        }

        @Override
        public String toString() {
            return "BigDecimal2BigInteger";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="Boolean - boolean">
    public static final Function boolean2Boolean = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "boolean2Boolean";
        }
    };
    public static final Function Boolean2boolean = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Boolean2boolean";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="Boolean - String">
    public static final Function Boolean2String = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return ((Boolean) from).toString();
        }

        @Override
        public String toString() {
            return "Boolean2String";
        }
    };
    public static final Function String2Boolean = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            String str = from.toString().trim();
            if ("true".equalsIgnoreCase(str)) return true;
            if ("false".equalsIgnoreCase(str)) return false;
            throw new Error("can't cast string(" + str + ") to boolean");
        }

        @Override
        public String toString() {
            return "String2Boolean";
        }
    };
    //</ed_itor-fold>
//</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="Character - char">
    public static final Function char2Character = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "char2Character";
        }
    };
    public static final Function Character2char = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "Character2char";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="char - int">
    public static final Function char2int = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return (int) (char) (Character) from;
        }

        @Override
        public String toString() {
            return "char2int";
        }
    };
    public static final Function int2char = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            int i = ((Integer) from);
            return (char) i;
        }

        @Override
        public String toString() {
            return "int2char";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="char - string">
    public static final Function char2String = new MutableWeightedCaster(2) {
        @Override
        public Object apply(Object from) {
            char c = (Character) from;
            return String.valueOf(c);
        }

        @Override
        public String toString() {
            return "char2String";
        }
    };
    public static final Function String2char = new MutableWeightedCaster(2) {
        @Override
        public Object apply(Object from) {
            String str = (String) from;
            return str.isEmpty() ? (char) 0 : str.charAt(0);
        }

        @Override
        public String toString() {
            return "String2char";
        }
    };
    //</ed_itor-fold>
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="Integer - boolean">
    public static final Function Integer2Boolean = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            int v = ((Integer) from);
            if (v == 0) return false;
            return v == 1;
        }

        @Override
        public String toString() {
            return "Integer2Boolean";
        }
    };
    public static final Function Boolean2Integer = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            boolean v = ((Boolean) from);
            return v ? 1 : 0;
        }

        @Override
        public String toString() {
            return "Boolean2Integer";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="Number - String">
    public static final Function Number2String = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from.toString();
        }

        @Override
        public String toString() {
            return "Number2String";
        }
    };
    //<ed_itor-fold defaultstate="collapsed" desc="parse string to number">
    //<ed_itor-fold defaultstate="collapsed" desc="String 2 Integer">
    public static final Function String2Integer = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            String str = (String) from;
            return Integer.parseInt(str);
        }

        @Override
        public String toString() {
            return "String2Integer";
        }
    };
    //</ed_itor-fold>
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="String 2 int">
    public static final Function String2int = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            String str = (String) from;
            return (int) Math.round(Double.parseDouble(str));
        }

        @Override
        public String toString() {
            return "String2int";
        }
    };
    //<ed_itor-fold defaultstate="collapsed" desc="String 2 Long">
    public static final Function String2Long = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            String str = (String) from;
            return Long.parseLong(str);
        }

        @Override
        public String toString() {
            return "String2Long";
        }
    };
    //</ed_itor-fold>
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="String 2 long">
    public static final Function String2long = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            String str = (String) from;
            return Long.parseLong(str);
        }

        @Override
        public String toString() {
            return "String2long";
        }
    };
    //<ed_itor-fold defaultstate="collapsed" desc="String 2 Double">
    public static final Function String2Double = new MutableWeightedCaster(2) {
        @Override
        public Object apply(Object from) {
            String str = (String) from;
            return Double.parseDouble(str);
        }

        @Override
        public String toString() {
            return "String2Double";
        }
    };
    //</ed_itor-fold>
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="String 2 Double">
    public static final Function String2double = new MutableWeightedCaster(2) {
        @Override
        public Object apply(Object from) {
            String str = (String) from;
            return Double.parseDouble(str);
        }

        @Override
        public String toString() {
            return "String2double";
        }
    };
    //<ed_itor-fold defaultstate="collapsed" desc="String 2 BigDecimal">
    public static final Function String2BigDecimal = new MutableWeightedCaster(2) {
        @Override
        public Object apply(Object from) {
            String str = (String) from;
            return new BigDecimal(str);
        }

        @Override
        public String toString() {
            return "String2BigDecimal";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="char[] 2 String">
    public static final Function charArr2String = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            char[] ca = (char[]) from;
            return new String(ca);
        }

        @Override
        public String toString() {
            return "charArr2String";
        }
    };
    //<ed_itor-fold defaultstate="collapsed" desc="Character[] 2 String">
    public static final Function CharArr2String = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            Character[] ca = (Character[]) from;
            return Arrays.stream(ca).map(String::valueOf).collect(Collectors.joining());
        }

        @Override
        public String toString() {
            return "CharArr2String";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="String 2 char[]">
    public static final Function String2charArr = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            String str = ((String) from);
            char[] arr = new char[str.length()];
            for (int i = 0; i < arr.length; i++) arr[i] = str.charAt(i);
            return arr;
        }

        @Override
        public String toString() {
            return "String2charArr";
        }
    };
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="String 2 Character[]">
    public static final Function String2CharArr = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            String str = ((String) from);
            int bound = str.length();
            return IntStream.range(0, bound).mapToObj(str::charAt).toArray(Character[]::new);
        }

        @Override
        public String toString() {
            return "String2CharArr";
        }
    };
    //<ed_itor-fold defaultstate="collapsed" desc="Clob 2 String">
    public static final Function Clob2String = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            Clob clob = ((Clob) from);
            return ClobToString.stringOf(clob);
        }

        @Override
        public String toString() {
            return "Clob2String";
        }
    };
    //</ed_itor-fold>
    //</ed_itor-fold>
//
//    // TODO use proj text
//    //<ed_itor-fold defaultstate="collapsed" desc="byte / char arrays">
//    //<ed_itor-fold defaultstate="collapsed" desc="String 2 byte[]">
    public static final Function String2byteArr = new Function<String, byte[]>() {
        @Override
        public byte[] apply(String from) {
            //return xyz.cofe.text.Text.decodeHex( (String)from );
            return from.getBytes();
        }

        @Override
        public String toString() {
            return "String2byteArr";
        }
    };
    /**
     * Базовый конструктор
     */
//    public ExtendedCastGraph() {
//    }

//    /**
//     * Клонирование объекта
//     *
//     * @return клон
//     */
//    @Override
//    public BaseCastGraph clone() {
//        return new BaseCastGraph(this);
//    }

//    public SimpleDateFormat[] getDateFormats() {
//        synchronized (this) {
//            if (dateFormats == null) {
//                dateFormats = new SimpleDateFormat[]{
//                        new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSSZ"),
//                        new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSSZ"),
//                        new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS"),
//                        new SimpleDateFormat("yyy-MM-dd HH:mm:ss"),
//                        new SimpleDateFormat("yyy-MM-dd HH:mm"),
//                        new SimpleDateFormat("yyy-MM-dd"),
//                };
//            }
//            return dateFormats;
//        }
//    }
    //</ed_itor-fold>
    //</ed_itor-fold>

//    public void setDateFormat(SimpleDateFormat[] df) {
//        synchronized (this) {
//            this.dateFormats = df;
//        }
//    }

    //</ed_itor-fold>
//
//    //<ed_itor-fold defaultstate="collapsed" desc="NClob 2 String">
//    public static final Convertor NClob2String = new MutableWeightedCaster() {
//        @Override
//        public Object convert(Object from) {
//            NClob clob = ((NClob)from);
//            return NClobToString.getStringOf(clob);
//        }
//        @Override public String toString(){ return "NClob2String"; }
//    };
//    //</ed_itor-fold>
//
//    //<ed_itor-fold defaultstate="collapsed" desc="URL - String">
//    //<ed_itor-fold defaultstate="collapsed" desc="URL2String">
    public static final Function<URL, String> URL2String = new Function<>() {
        @Override
        public String apply(URL from) {
            return from.toString();
        }

        @Override
        public String toString() {
            return "URL2String";
        }
    };
    public static final Function<String, URL> String2URL = new Function<>() {
        @Override
        public URL apply(String from) {
            try {
                return new URL(from);
            } catch (MalformedURLException ex) {
                throw new ClassCastException(
                        "can't cast from " + from + " to java.net.URL\n" +
                                ex.getMessage()
                );
            }
        }

        @Override
        public String toString() {
            return "String2URL";
        }
    };
    //</ed_itor-fold>
    public final Function Date2SqlDate = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            Date d = (Date) from;
            return new java.sql.Date(d.getTime());
        }

        @Override
        public String toString() {
            return "Date2SqlDate";
        }
    };
    public final Function SqlDate2Date = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "SqlDate2Date";
        }
    };
    //</ed_itor-fold>
    public final Function Date2SqlTime = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            Date d = (Date) from;
            return new Time(d.getTime());
        }

        @Override
        public String toString() {
            return "Date2SqlTime";
        }
    };
    public final Function SqlTime2Date = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            return from;
        }

        @Override
        public String toString() {
            return "SqlTime2Date";
        }
    };
//    //</ed_itor-fold>
//
//    // TODO use proj text
//    //<ed_itor-fold defaultstate="collapsed" desc="byte[] 2 String">
////    public static final Convertor byteArr2String = new MutableWeightedCaster() {
////        @Override
////        public Object convert(Object from) {
////            byte[] ba = (byte[])from;
////            return xyz.cofe.text.Text.encodeHex(ba);
////        }
////        @Override public String toString(){ return "byteArr2String"; }
////    };
//    //</ed_itor-fold>
//
//    // TODO use proj text
//    //<ed_itor-fold defaultstate="collapsed" desc="Byte[] 2 String">
////    public static final Convertor ByteArr2String = new MutableWeightedCaster() {
////        @Override
////        public Object convert(Object from) {
////            Byte[] ba = (Byte[])from;
////            return xyz.cofe.text.Text.encodeHex(ba);
////        }
////        @Override public String toString(){ return "ByteArr2String"; }
////    };
//    //</ed_itor-fold>
//
//    // TODO use proj text
//    //<ed_itor-fold defaultstate="collapsed" desc="String 2 Byte[]">
////    public static final Convertor String2ByteArr = new MutableWeightedCaster() {
////        @Override
////        public Object convert(Object from) {
////            return xyz.cofe.text.Text.decodeHexBytes((String)from);
////        }
////        @Override public String toString(){ return "String2ByteArr"; }
////    };
//    //</ed_itor-fold>
//    public final Function SqlTimestamp2Date = new MutableWeightedCaster() {
//        @Override
//        public Object apply(Object from) {
//            return (java.sql.Timestamp) from;
//        }
//
//        @Override
//        public String toString() {
//            return "SqlTimestamp2Date";
//        }
//    };
//    //</ed_itor-fold>
//    public final Function String2SqlDate = new MutableWeightedCaster() {
//        @Override
//        public Object apply(Object from) {
//            synchronized (CastGraph.this) {
//                //java.util.Date d = getDateFormat().parse((String)from);
//                Date d = (Date) String2Date.apply(from);
//                return new java.sql.Date(d.getTime());
//            }
//        }
//
//        @Override
//        public String toString() {
//            return "String2SqlDate";
//        }
//    };
//    //</ed_itor-fold>
//    public final Function String2SqlTime = new MutableWeightedCaster() {
//        @Override
//        public Object apply(Object from) {
//            synchronized (CastGraph.this) {
//                Date d = (Date) String2Date.apply(from);
//                return new java.sql.Time(d.getTime());
//            }
//        }
//
//        @Override
//        public String toString() {
//            return "String2SqlTime";
//        }
//    };
//    //</ed_itor-fold>
//    public final Function String2SqlTimestamp = new MutableWeightedCaster() {
//        @Override
//        public Object apply(Object from) {
//            synchronized (CastGraph.this) {
//                Date d = (Date) String2Date.apply(from);
//                return new java.sql.Timestamp(d.getTime());
//            }
//        }
//
//        @Override
//        public String toString() {
//            return "String2SqlTimestamp";
//        }
//    };
    //</ed_itor-fold>
//</ed_itor-fold>
    //</ed_itor-fold>
    //<ed_itor-fold defaultstate="collapsed" desc="date and time">
    //<ed_itor-fold defaultstate="collapsed" desc="date string format">
//    private SimpleDateFormat[] dateFormats = new SimpleDateFormat[]{
//            new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSSZ")
//    };
//    //<ed_itor-fold defaultstate="collapsed" desc="date time convertors">
//    public final Function Date2String = new MutableWeightedCaster() {
//        @Override
//        public Object apply(Object from) {
//            synchronized (CastGraph.this) {
//                Date d = (Date) from;
//                SimpleDateFormat[] dfs = getDateFormats();
//                SimpleDateFormat df = dfs != null && dfs.length > 0 ? dfs[0] : new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSSZ");
//                return df.format(d);
//            }
//        }
//
//        @Override
//        public String toString() {
//            return "Date2String";
//        }
//    };
//    public final Function String2Date = new MutableWeightedCaster() {
//        @Override
//        public Object apply(Object from) {
//            synchronized (CastGraph.this) {
//                SimpleDateFormat[] dfs = getDateFormats();
//                if (dfs == null) throw new IllegalStateException("date formats not setted");
//                for (SimpleDateFormat df : dfs) {
//                    try {
//                        return df.parse((String) from);
//                    } catch (ParseException ex) {
//                    }
//                }
//                throw new Error("can't cast from " + from + " to java.util.Date");
//            }
//        }
//
//        @Override
//        public String toString() {
//            return "String2Date";
//        }
//    };
    //</ed_itor-fold>

//    /**
//     * Конструктор копирования
//     *
//     * @param src Исходный объект
//     */
//    public CastGraph(CastGraph src) {
//        super(src);
//        if (src != null) {
//            this.dateFormats = src.dateFormats;
//        }
//    }
    //</ed_itor-fold>
    public final Function Date2SqlTimestamp = new MutableWeightedCaster() {
        @Override
        public Object apply(Object from) {
            Date d = (Date) from;
            return new Timestamp(d.getTime());
        }

        @Override
        public String toString() {
            return "Date2SqlTimestamp";
        }
    };
    public ExtendedCastGraph() {

        super();

        addEdge(Integer.class, integer2Int, int.class);
        addEdge(int.class, int2Integer, Integer.class);

        addEdge(Byte.class, Byte2byte, byte.class);
        addEdge(byte.class, byte2Byte, Byte.class);

        addEdge(Short.class, Short2short, short.class);
        addEdge(short.class, short2Short, Short.class);

        addEdge(Long.class, Long2long, long.class);
        addEdge(long.class, long2Long, Long.class);

        addEdge(Float.class, Float2float, float.class);
        addEdge(float.class, float2Float, Float.class);

        addEdge(Double.class, Double2double, double.class);
        addEdge(double.class, double2Double, Double.class);

        addEdge(Number.class, Number2Byte, Byte.class);
        addEdge(Byte.class, Byte2Number, Number.class);

        addEdge(Number.class, Number2Short, Short.class);
        addEdge(Short.class, Short2Number, Number.class);

        addEdge(Number.class, Number2Integer, Integer.class);
        addEdge(Integer.class, Integer2Number, Number.class);

        addEdge(Number.class, Number2Long, Long.class);
        addEdge(Long.class, Long2Number, Number.class);

        addEdge(Number.class, Number2Float, Float.class);
        addEdge(Float.class, Float2Number, Number.class);

        addEdge(Number.class, Number2Double, Double.class);
        addEdge(Double.class, Double2Number, Number.class);

        addEdge(Number.class, Number2BigDecimal, BigDecimal.class);
        addEdge(BigDecimal.class, BigDecimal2Number, Number.class);

        addEdge(Number.class, Number2BigInteger, BigInteger.class);
        addEdge(BigInteger.class, BigInteger2Number, Number.class);

        addEdge(Boolean.class, Boolean2boolean, boolean.class);
        addEdge(boolean.class, boolean2Boolean, Boolean.class);

        addEdge(Character.class, Character2char, char.class);
        addEdge(char.class, char2Character, Character.class);

        // ..........

        addEdge(Number.class, Number2String, String.class);

        addEdge(char.class, char2int, int.class);
        addEdge(Character.class, char2int, int.class);
        addEdge(char.class, char2int, Integer.class);
        addEdge(Character.class, char2int, Integer.class);

        addEdge(int.class, int2char, char.class);
        addEdge(int.class, int2char, Character.class);
        addEdge(Integer.class, int2char, char.class);
        addEdge(Integer.class, int2char, Character.class);

        addEdge(char.class, char2String, String.class);
        addEdge(Character.class, char2String, String.class);
        addEdge(String.class, String2char, char.class);
        addEdge(String.class, String2char, Character.class);

        addEdge(Integer.class, Integer2Boolean, Boolean.class);
        addEdge(int.class, Integer2Boolean, Boolean.class);
        addEdge(Integer.class, Integer2Boolean, boolean.class);
        addEdge(int.class, Integer2Boolean, boolean.class);

        addEdge(Boolean.class, Boolean2Integer, Integer.class);
        addEdge(Boolean.class, Boolean2Integer, int.class);
        addEdge(boolean.class, Boolean2Integer, Integer.class);
        addEdge(boolean.class, Boolean2Integer, int.class);

        addEdge(Boolean.class, Boolean2String, String.class);
        addEdge(boolean.class, Boolean2String, String.class);

        addEdge(String.class, String2Boolean, Boolean.class);
        addEdge(String.class, String2Boolean, boolean.class);

        addEdge(BigInteger.class, BigInteger2BigDecimal, BigDecimal.class);
        addEdge(BigDecimal.class, BigDecimal2BigInteger, BigInteger.class);

        addEdge(String.class, String2Integer, Integer.class);
        addEdge(String.class, String2int, int.class);

        addEdge(String.class, String2Long, Long.class);
        addEdge(String.class, String2long, long.class);

        addEdge(String.class, String2Double, Double.class);
        addEdge(String.class, String2double, double.class);

        addEdge(String.class, String2BigDecimal, BigDecimal.class);

        byte[] ba = ArrayUtil.EMPTY_BYTE_ARRAY;
        Byte[] Ba = ArrayUtil.EMPTY_BYTE_OBJECT_ARRAY;

        // TODO use proj text
        addEdge(String.class, String2byteArr, ba.getClass());
        // TODO use proj text
//        setAt( ba.getClass(), String.class, byteArr2String );

        // TODO use proj text
//        setAt( Ba.getClass(), String.class, ByteArr2String );
        // TODO use proj text
//        setAt( String.class, Ba.getClass(), String2ByteArr );

        char[] ca = ArrayUtil.EMPTY_CHAR_ARRAY;
        addEdge(ca.getClass(), charArr2String, String.class);
        Character[] Ca = ArrayUtil.EMPTY_CHARACTER_OBJECT_ARRAY;
        addEdge(Ca.getClass(), CharArr2String, String.class);

        addEdge(String.class, String2charArr, ca.getClass());
        addEdge(String.class, String2CharArr, Ca.getClass());

        addEdge(Date.class, Date2SqlDate, java.sql.Date.class);
        addEdge(Date.class, Date2SqlTime, Time.class);
        addEdge(Date.class, Date2SqlTimestamp, Timestamp.class);
        addEdge(java.sql.Date.class, SqlDate2Date, Date.class);
        addEdge(Time.class, SqlTime2Date, Date.class);
        //        set(java.sql.Timestamp.class, Date.class, SqlTimestamp2Date);

//        addEdge(Date.class, String.class, Date2String);
//        addEdge(String.class, Date.class, String2Date);

//        setAt( Clob.class, String.class, Clob2String );
//        setAt( NClob.class, String.class, NClob2String );
//
        addEdge(URL.class, URL2String, String.class);
        addEdge(String.class, String2URL, URL.class);
//
//        setAt( java.net.URI.class, String.class, URI2String );
//        setAt( String.class, java.net.URI.class, String2URI );
//
//        setAt( java.io.File.class, String.class, JavaIoFile2String );
//        setAt( String.class, java.io.File.class, String2JavaIoFile );
//
//        setAt( java.io.File.class, java.net.URI.class, JavaIoFile2URI );
//        setAt( java.io.File.class, java.net.URL.class, JavaIoFile2URL );

        // TODO export to spi
//        setAt( xyz.cofe.fs.File.class, String.class, XyzCofeFile2String );
//        setAt( String.class, xyz.cofe.fs.File.class, String2XyzCofeFile );

//        setAt( Charset.class, String.class, Charset2String );
//        setAt( String.class, Charset.class, String2Charset );
//
//        setAt( xyz.cofe.io.File.class, String.class, CofeIOFile2String );
//        setAt( String.class, xyz.cofe.io.File.class, String2CofeIOFile );
//
//        setAt( xyz.cofe.io.File.class, java.nio.file.Path.class, CofeIOFile2Path );
//        setAt( java.nio.file.Path.class, xyz.cofe.io.File.class, Path2CofeIOFile );
//
//        setAt( xyz.cofe.io.File.class, java.io.File.class, CofeIOFile2File );
//        setAt( java.io.File.class, xyz.cofe.io.File.class, JavaFile2CofeIOFile );

        for (GetTypeConvertor gtc : ServiceLoader.load(GetTypeConvertor.class)) {
            if (gtc == null) continue;
            Function conv = gtc.getConvertor();
            if (conv == null) continue;
            Class srcType = gtc.getSourceType();
            if (srcType == null) continue;
            Class trgType = gtc.getTargetType();
            if (trgType == null) continue;
            addEdge(srcType, conv, trgType);
        }

        //add: generic Supplier<X> -> X -- requires generic argument processing


        //single element vector/tensor
        //addEdge(Tensor.class, (Function<Tensor, Float>) (t) -> t.volume() == 1 ? t.getAt(0) : Float.NaN, Float.class);

        addEdge(Boolean.class, (Function<Boolean, Integer>) (i) -> i ? 1 : 0, Integer.class);
        addEdge(Number.class, (Function<Number, Boolean>) (i) -> i.intValue() > 0, Boolean.class);

        addEdge(Short.class, (Function<Short, Integer>) Short::intValue, Integer.class);

        //1-element
        addEdge(Float.class, (Function<Float, Integer>) (Math::round), Integer.class);
        addEdge(Integer.class, (Function<Integer, Float>) Integer::floatValue, Float.class);

        addEdge(Integer.class, (Function<Integer, Long>) Integer::longValue, Long.class);
        addEdge(Long.class, (Function<Long, Integer>) Long::intValue, Integer.class);

        addEdge(Float.class, (Function<Float, Double>) (Float::doubleValue), Double.class);
        addEdge(Double.class, (Function<Double, Float>) (Double::floatValue), Float.class);


        addEdge(float[].class, (Function<float[], double[]>) Util::toDouble, double[].class);

        //default scalar value projection
        //addEdge(v2.class, (Function<v2, Either<Double>>)(v4 -> Math.sqrt(Util.sqr(v4.x)+Util.sqr(v4.y))), Double.class);

        addEdge(v2.class, (Function<v2, float[]>) (v3 -> new float[]{v3.x, v3.y}), float[].class);
        addEdge(v3.class, (Function<v3, float[]>) (v2 -> new float[]{v2.x, v2.y, v2.z}), float[].class);
        addEdge(v2.class, (Function<v2, v3>) (v1 -> new v3(v1.x, v1.y, 0)), v3.class);

        addEdge(float[].class, (Function<float[], Tensor>) (ArrayTensor::new), Tensor.class);
        addEdge(double[].class, (Function<double[], float[]>) Util::toFloat, float[].class);
        //1-element
        addEdge(Float.class, (Function<Float, float[]>) (v -> v != null ? new float[]{v} : new float[]{Float.NaN}), float[].class);
        //        setAt(Float.class, Tensor.class, (Function<Float,Tensor>)((f) -> new ArrayTensor(new float[] { f} ))); //1-element
        //does this happen
        addEdge(Tensor.class, (Function<Tensor, Tensor>) (t -> {
            if (t instanceof Tensor)
                return t; //does this happen
            else
                return new ArrayTensor(t.floatArrayShared());
        }), ArrayTensor.class);
        addEdge(ArrayTensor.class, (Function<Tensor, float[]>) (a -> ((ArrayTensor)a).data), float[].class);

    }

////</ed_itor-fold>
//
//    //<ed_itor-fold defaultstate="collapsed" desc="URI - String">
//    //<ed_itor-fold defaultstate="collapsed" desc="URI2String">
//    public static final Convertor URI2String = new MutableWeightedCaster() {
//        @Override
//        public Object convert(Object from) {
//            java.net.URI uri = ((java.net.URI)from);
//            return uri.toString();
//        }
//        @Override public String toString(){ return "URI2String"; }
//    };
//    //</ed_itor-fold>
//
//    //<ed_itor-fold defaultstate="collapsed" desc="String2URI">
//    public static final Convertor String2URI = new MutableWeightedCaster() {
//        @Override
//        public Object convert(Object from) {
//            String url = ((String)from);
//            try {
//                return new java.net.URI(url);
//            } catch (URISyntaxException ex) {
//                throw new ClassCastException(
//                        "can't cast from "+url+" to java.net.URL\n"+
//                                ex.getMessage()
//                );
//            }
//        }
//        @Override public String toString(){ return "String2URI"; }
//    };
//    //</ed_itor-fold>
////</ed_itor-fold>
//
//    //<ed_itor-fold defaultstate="collapsed" desc="java.io.File - String">
//    //<ed_itor-fold defaultstate="collapsed" desc="JavaIoFile2String">
//    public static final Convertor JavaIoFile2String = new MutableWeightedCaster() {
//        @Override
//        public Object convert(Object from) {
//            java.io.File file = ((java.io.File)from);
//            return file.toString();
//        }
//        @Override public String toString(){ return "JavaIoFile2String"; }
//    };
//    //</ed_itor-fold>
//
//    //<ed_itor-fold defaultstate="collapsed" desc="String2JavaIoFile">
//    public static final Convertor String2JavaIoFile = new MutableWeightedCaster() {
//        @Override
//        public Object convert(Object from) {
//            String url = ((String)from);
//            return new java.io.File(url);
//        }
//        @Override public String toString(){ return "String2JavaIoFile"; }
//    };
//    //</ed_itor-fold>
//    //</ed_itor-fold>
//
//    //<ed_itor-fold defaultstate="collapsed" desc="xyz.cofe.io.File String">
//    public static final Convertor CofeIOFile2String = new MutableWeightedCaster(){
//        @Override
//        public Object convert(Object from) {
//            xyz.cofe.io.File file = (xyz.cofe.io.File)from;
//            return file.toString();
//        }
//    };
//
//    public static final Convertor CofeIOFile2Path = new MutableWeightedCaster(){
//        @Override
//        public Object convert(Object from) {
//            xyz.cofe.io.File file = (xyz.cofe.io.File)from;
//            return file.path;
//        }
//    };
//
//    public static final Convertor CofeIOFile2File = new MutableWeightedCaster(){
//        @Override
//        public Object convert(Object from) {
//            xyz.cofe.io.File file = (xyz.cofe.io.File)from;
//            return file.toFile();
//        }
//    };
//
//    public static final Convertor String2CofeIOFile = new MutableWeightedCaster(){
//        @Override
//        public Object convert(Object from) {
//            String str = (String)from;
//            xyz.cofe.io.File file = new xyz.cofe.io.File(str);
//            return file;
//        }
//    };
//
//    public static final Convertor Path2CofeIOFile = new MutableWeightedCaster(){
//        @Override
//        public Object convert(Object from) {
//            java.nio.file.Path path = (java.nio.file.Path)from;
//            xyz.cofe.io.File file = new xyz.cofe.io.File(path);
//            return file;
//        }
//    };
//
//    public static final Convertor JavaFile2CofeIOFile = new MutableWeightedCaster(){
//        @Override
//        public Object convert(Object from) {
//            java.io.File f = (java.io.File)from;
//            xyz.cofe.io.File file = new xyz.cofe.io.File(f.toPath());
//            return file;
//        }
//    };
//    //</ed_itor-fold>
//
//    //<ed_itor-fold defaultstate="collapsed" desc="JavaIoFile2URI">
//    public final Convertor JavaIoFile2URI = new MutableWeightedCaster() {
//        @Override
//        public Object convert(Object from) {
//            java.io.File file = ((java.io.File)from);
//            return file.toURI();
//        }
//        @Override public String toString(){ return "JavaIoFile2URI"; }
//    };
//    //</ed_itor-fold>
//
//    //<ed_itor-fold defaultstate="collapsed" desc="JavaIoFile2URI">
//    public final Convertor JavaIoFile2URL = new MutableWeightedCaster() {
//        @Override
//        public Object convert(Object from) {
//            java.io.File file = ((java.io.File)from);
//            try {
//                return file.toURI().toURL();
//            } catch (MalformedURLException ex) {
//                throw new ClassCastException(
//                        "can't cast from "+file+" to java.net.URL\n"+
//                                ex.getMessage()
//                );
//            }
//        }
//        @Override public String toString(){ return "JavaIoFile2URL"; }
//    };
//    //</ed_itor-fold>
//
//    //<ed_itor-fold defaultstate="collapsed" desc="Charset2String">
//    public final Convertor Charset2String = new MutableWeightedCaster() {
//        @Override
//        public Object convert(Object from) {
//            Charset str = ((Charset)from);
//            return str.name();
//        }
//        @Override public String toString(){ return "Charset2String"; }
//    };
//    //</ed_itor-fold>
//
//    //<ed_itor-fold defaultstate="collapsed" desc="String2Charset">
//    public final Convertor String2Charset = new MutableWeightedCaster() {
//        @Override
//        public Object convert(Object from) {
//            String str = ((String)from);
//            return Charset.forName(str);
//        }
//        @Override public String toString(){ return "String2Charset"; }
//    };
//    //</ed_itor-fold>

}