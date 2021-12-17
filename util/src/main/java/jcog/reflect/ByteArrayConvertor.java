/*
 * The MIT License
 *
 * Copyright 2015 Kamnev Georgiy (nt.gocha@gmail.com).
 *
 * Данная лицензия разрешает, безвозмездно, лицам, получившим копию данного программного
 * обеспечения и сопутствующей документации (в дальнейшем именуемыми "Программное Обеспечение"),
 * использовать Программное Обеспечение без ограничений, включая неограниченное право на
 * использование, копирование, изменение, объединение, публикацию, распространение, сублицензирование
 * и/или продажу копий Программного Обеспечения, также как и лицам, которым предоставляется
 * данное Программное Обеспечение, при соблюдении следующих условий:
 *
 * Вышеупомянутый копирайт и данные условия должны быть включены во все копии
 * или значимые части данного Программного Обеспечения.
 *
 * ДАННОЕ ПРОГРАММНОЕ ОБЕСПЕЧЕНИЕ ПРЕДОСТАВЛЯЕТСЯ «КАК ЕСТЬ», БЕЗ ЛЮБОГО ВИДА ГАРАНТИЙ,
 * ЯВНО ВЫРАЖЕННЫХ ИЛИ ПОДРАЗУМЕВАЕМЫХ, ВКЛЮЧАЯ, НО НЕ ОГРАНИЧИВАЯСЬ ГАРАНТИЯМИ ТОВАРНОЙ ПРИГОДНОСТИ,
 * СООТВЕТСТВИЯ ПО ЕГО КОНКРЕТНОМУ НАЗНАЧЕНИЮ И НЕНАРУШЕНИЯ ПРАВ. НИ В КАКОМ СЛУЧАЕ АВТОРЫ
 * ИЛИ ПРАВООБЛАДАТЕЛИ НЕ НЕСУТ ОТВЕТСТВЕННОСТИ ПО ИСКАМ О ВОЗМЕЩЕНИИ УЩЕРБА, УБЫТКОВ
 * ИЛИ ДРУГИХ ТРЕБОВАНИЙ ПО ДЕЙСТВУЮЩИМ КОНТРАКТАМ, ДЕЛИКТАМ ИЛИ ИНОМУ, ВОЗНИКШИМ ИЗ, ИМЕЮЩИМ
 * ПРИЧИНОЙ ИЛИ СВЯЗАННЫМ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ ИЛИ ИСПОЛЬЗОВАНИЕМ ПРОГРАММНОГО ОБЕСПЕЧЕНИЯ
 * ИЛИ ИНЫМИ ДЕЙСТВИЯМИ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ.
 */

package jcog.reflect;


import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Конвертор массива байтов
 *
 * @author Kamnev Georgiy (nt.gocha@gmail.com)
 */
public class ByteArrayConvertor {
    public static final Class type = byte[].class;

    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="byte to hex text">

    /**
     * Возвращает символное-hex представление байтов
     *
     * @param bytes Набор байтов
     * @return Текстовое представление
     */
    public static String getHex(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes==null");
        }
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(getHex(aByte));
        }
        return sb.toString();
    }

    /**
     * Возвращает символное-hex представление байтов
     *
     * @param bytes Набор байтов
     * @return Текстовое представление
     */
    public static String getHex(Byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes==null");
        }
        String result = Arrays.stream(bytes).map(ByteArrayConvertor::getHex).collect(Collectors.joining());
        String sb = result;
        return sb;
    }

    /**
     * Возвращает двух символьное представление байта
     *
     * @param byteValue байт
     * @return два символа представляющих байт (00 .. FF)
     */
    public static String getHex(byte byteValue) {
        return Integer.toString((byteValue & 0xff) + 0x100, 16).substring(1).toUpperCase();
    }

    public static String encodeHex(byte[] bytes) {
        if (bytes == null) throw new IllegalArgumentException("bytes==null");
        return getHex(bytes);
    }

    public static String encodeHex(Byte[] bytes) {
        if (bytes == null) throw new IllegalArgumentException("bytes==null");
        return getHex(bytes);
    }

    public static byte[] decodeHex(CharSequence bytes) {
        if (bytes == null) throw new IllegalArgumentException("bytes==null");
        int len = bytes.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(bytes.charAt(i), 16) << 4)
                    + Character.digit(bytes.charAt(i + 1), 16));
        }
        return data;
    }
    // </editor-fold>    

    public static Byte[] decodeHexBytes(CharSequence bytes) {
        if (bytes == null) throw new IllegalArgumentException("bytes==null");
        int len = bytes.length();
        Byte[] data = new Byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(bytes.charAt(i), 16) << 4)
                    + Character.digit(bytes.charAt(i + 1), 16));
        }
        return data;
    }

    static final Function<Object, String> toString = (srcData) -> {
        if ((srcData instanceof byte[] bytes)) {
            return encodeHex(bytes);
        } else {
            return null;
        }
        // TODO use proj text
//        throw new Error("not implemented");
    };

    static final ToValueConvertor toValue = (text) -> {
        if (text != null) {
            return decodeHex(text);
        }
        return null;
        // TODO use proj text
//        throw new Error("not implemented");
    };
}