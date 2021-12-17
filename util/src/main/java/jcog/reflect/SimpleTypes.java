/*
 * The MIT License
 *
 * Copyright 2014 Kamnev Georgiy (nt.gocha@gmail.com).
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

/**
 * Проверка на простые типы
 *
 * @author gocha
 */
public enum SimpleTypes {
	;
	private static final Class cBool1 = boolean.class;
    private static final Class cBool2 = Boolean.class;
    private static final Class cByte1 = byte.class;
    private static final Class cByte2 = Byte.class;
    private static final Class cChar1 = char.class;
    private static final Class cChar2 = Character.class;
    private static final Class cDouble1 = double.class;
    private static final Class cDouble2 = Double.class;
    private static final Class cFloat1 = float.class;
    private static final Class cFloat2 = Float.class;
    private static final Class cInteger1 = int.class;
    private static final Class cInteger2 = Integer.class;
    private static final Class cLong1 = long.class;
    private static final Class cLong2 = Long.class;
    private static final Class cShort1 = short.class;
    private static final Class cShort2 = Short.class;
    private static final Class cVoid1 = Void.class;
    private static final Class cVoid2 = void.class;

    private static final Class[] simpleTypes =
            {
                    cBool1, cBool2, cByte1, cByte2, cChar1, cChar2, cDouble1, cDouble2,
                    cFloat1, cFloat2, cInteger1, cInteger2, cLong1, cLong2, cShort1, cShort2,
                    cVoid1, cVoid2
            };
    private static final Class[] boolTypes = {cBool1, cBool2};
    private static final Class[] byteTypes = {cByte1, cByte2};
    private static final Class[] charTypes = {cChar1, cChar2};
    private static final Class[] doubleTypes = {cDouble1, cDouble2};
    private static final Class[] floatTypes = {cFloat1, cFloat2};
    private static final Class[] intTypes = {cInteger1, cInteger2};
    private static final Class[] longTypes = {cLong1, cLong2};
    private static final Class[] shortTypes = {cShort1, cShort2};

    /**
     * Возвращает простые типы
     *
     * @return Простые типы
     */
    public static Class[] simpleTypes() {
        return simpleTypes;
    }

    public static Class voidObject() {
        return cVoid1;
    }

    public static Class boolObject() {
        return cBool2;
    }

    public static Class byteObject() {
        return cByte2;
    }

    public static Class charObject() {
        return cChar2;
    }

    public static Class doubleObject() {
        return cDouble2;
    }

    public static Class floatObject() {
        return cFloat2;
    }

    public static Class intObject() {
        return cInteger2;
    }

    public static Class longObject() {
        return cLong2;
    }

    public static Class shortObject() {
        return cShort2;
    }

    public static Class _void() {
        return cVoid2;
    }

    public static Class _bool() {
        return cBool1;
    }

    public static Class _byte() {
        return cByte1;
    }

    public static Class _char() {
        return cChar1;
    }

    public static Class _double() {
        return cDouble1;
    }

    public static Class _float() {
        return cFloat1;
    }

    public static Class _int() {
        return cInteger1;
    }

    public static Class _long() {
        return cLong1;
    }

    public static Class _short() {
        return cShort1;
    }

    public static Class[] boolTypes() {
        return boolTypes;
    }

    public static Class[] byteTypes() {
        return byteTypes;
    }

    public static Class[] charTypes() {
        return charTypes;
    }

    public static Class[] doubleTypes() {
        return doubleTypes;
    }

    public static Class[] floatTypes() {
        return floatTypes;
    }

    public static Class[] intTypes() {
        return intTypes;
    }

    public static Class[] longTypes() {
        return longTypes;
    }

    public static Class[] shortTypes() {
        return shortTypes;
    }

    /**
     * Проверка что указанный класс являеться простым типом данных<br>
     * boolean, byte, char, double, float, int, long, void, short
     *
     * @param c класс
     * @return true - является, false - не является
     */
    public static boolean isSimple(Class c) {
        return isBoolean(c) ||
                isByte(c) ||
                isChar(c) ||
                isDouble(c) ||
                isFloat(c) ||
                isInt(c) ||
                isLong(c) ||
                isVoid(c) ||
                isShort(c);
    }

    public static boolean isNullable(Class c) {
        if (c == null) throw new IllegalArgumentException("c==null");
        if (c == cBool2) return true;
        if (c == cByte2) return true;
        if (c == cChar2) return true;
        if (c == cDouble2) return true;
        if (c == cFloat2) return true;
        if (c == cInteger2) return true;
        if (c == cLong2) return true;
        return c == cShort2;
    }

    public static boolean isVoid(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("c == null");
        }
        return cVoid2 == c || cVoid1 == c;
    }

    public static boolean isBoolean(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("c == null");
        }
        return cBool1 == c || cBool2 == c;
    }

    public static boolean isByte(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("c == null");
        }
        return cByte1 == c || cByte2 == c;
    }

    public static boolean isChar(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("c == null");
        }
        return cChar1 == c || cChar2 == c;
    }

    public static boolean isDouble(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("c == null");
        }
        return cDouble1 == c || cDouble2 == c;
    }

    public static boolean isFloat(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("c == null");
        }
        return cFloat1 == c || cFloat2 == c;
    }

    public static boolean isInt(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("c == null");
        }
        return cInteger1 == c || cInteger2 == c;
    }

    public static boolean isLong(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("c == null");
        }
        return cLong1 == c || cLong2 == c;
    }

    public static boolean isShort(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("c == null");
        }
        return cShort1 == c || cShort2 == c;
    }
}
