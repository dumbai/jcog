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


import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * @author Kamnev Georgiy (nt.gocha@gmail.com)
 */
public enum BaseTypes {
	;
	public static final Class bool1 = boolean.class;
    public static final Class bool2 = Boolean.class;
    public static final Class byte1 = byte.class;
    public static final Class byte2 = Byte.class;
    public static final Class short1 = short.class;
    public static final Class short2 = Short.class;
    public static final Class int1 = int.class;
    //</editor-fold>
    public static final Class int2 = Integer.class;
    public static final Class long1 = long.class;
    public static final Class long2 = Long.class;
    public static final Class float1 = float.class;
    public static final Class float2 = Float.class;
    public static final Class double1 = double.class;
    public static final Class double2 = Double.class;
    public static final Class string = String.class;
    public static final Class chr1 = char.class;
    public static final Class chr2 = Character.class;
    public static final Class byteArr1 = byte[].class;
    public static final Class byteArr2 = Byte[].class;
    public static final Class bigInt = BigInteger.class;
    public static final Class bigDecimal = BigDecimal.class;
    public static final Class date = java.util.Date.class;
    public static final Class sqlTime = Time.class;
    public static final Class sqlDate = Date.class;
    public static final Class sqlTimestamp = Timestamp.class;

}
