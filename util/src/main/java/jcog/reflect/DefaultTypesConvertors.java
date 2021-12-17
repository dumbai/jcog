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

import jcog.reflect.simple.*;
import jcog.reflect.spi.ConvertToStringService;
import jcog.reflect.spi.ConvertToValueService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ServiceLoader;
import java.util.function.Function;

//import xyz.cofe.files.ByteSize;
//import xyz.cofe.text.EndLine;

/**
 * Конверторы типов по умолчанию.
 * <p>
 * Подерживаемые типы:
 * <ul>
 * <li>boolean</li>
 * <li>byte</li>
 * <li>char</li>
 * <li>double</li>
 * <li>float</li>
 * <li>int</li>
 * <li>long</li>
 * <li>short</li>
 * <li>BigDecimal</li>
 * <li>BigInteger</li>
 * <li>String</li>
 * <li>char</li>
 * <li>short</li>
 * <li>java.util.Date</li>
 * <li>java.sql.Date</li>
 * <li>java.sql.Time</li>
 * <li>java.sql.Timestamp</li>
 * <li>java.awt.Font</li>
 * <li>javax.swing.KeyStroke</li>
 * <li>java.io.File</li>
 * <li>java.awt.geom.Dimension2D</li>
 * <li>java.awt.Dimension</li>
 * <li>java.awt.Point</li>
 * <li>java.net.URI</li>
 * <li>java.net.URL</li>
 * <li>java.awt.Color - Пример <br><b>#000</b><br> <b>#fafefc</b></li>
 * <li>java.nio.charset.Charset</li>
 * <li>org.gocha.text.EndLine</li>
 * <li>org.gocha.files.ByteSize</li>
 * </ul>
 *
 * @author gocha
 */
public class DefaultTypesConvertors extends TypesConverters {


    protected DefaultTypesConvertors() {
        NumberConvertor numConv = new NumberConvertor(NumberType.LONG);
        BooleanConvertor boolConv = new BooleanConvertor();

        toString.put(SimpleTypes._bool(), BooleanConvertor.toString);
        toString.put(SimpleTypes.boolObject(), BooleanConvertor.toString);

        toString.put(SimpleTypes._byte(), NumberConvertor.toString);
        toString.put(SimpleTypes._char(), NumberConvertor.toString);
        toString.put(SimpleTypes._double(), NumberConvertor.toString);
        toString.put(SimpleTypes._float(), NumberConvertor.toString);
        toString.put(SimpleTypes._int(), NumberConvertor.toString);
        toString.put(SimpleTypes._long(), NumberConvertor.toString);
        toString.put(SimpleTypes._short(), NumberConvertor.toString);


        toString.put(SimpleTypes.byteObject(), NumberConvertor.toString);
        toString.put(SimpleTypes.charObject(), NumberConvertor.toString);
        toString.put(SimpleTypes.doubleObject(), NumberConvertor.toString);
        toString.put(SimpleTypes.floatObject(), NumberConvertor.toString);
        toString.put(SimpleTypes.intObject(), NumberConvertor.toString);
        toString.put(SimpleTypes.longObject(), NumberConvertor.toString);
        toString.put(SimpleTypes.shortObject(), NumberConvertor.toString);

        toString.put(BigDecimal.class, NumberConvertor.toString);
        toString.put(BigInteger.class, NumberConvertor.toString);

        toValues.put(SimpleTypes._byte(), new NumberConvertor(NumberType.BYTE));
        toValues.put(SimpleTypes._double(), new NumberConvertor(NumberType.DOUBLE));
        toValues.put(SimpleTypes._float(), new NumberConvertor(NumberType.FLOAT));
        toValues.put(SimpleTypes._int(), new NumberConvertor(NumberType.INTEGER));
        toValues.put(SimpleTypes._long(), new NumberConvertor(NumberType.LONG));
        toValues.put(SimpleTypes._short(), new NumberConvertor(NumberType.SHORT));

        toValues.put(SimpleTypes.byteObject(), new NumberConvertor(NumberType.BYTE));
        toValues.put(SimpleTypes.doubleObject(), new NumberConvertor(NumberType.DOUBLE));
        toValues.put(SimpleTypes.floatObject(), new NumberConvertor(NumberType.FLOAT));
        toValues.put(SimpleTypes.intObject(), new NumberConvertor(NumberType.INTEGER));
        toValues.put(SimpleTypes.longObject(), new NumberConvertor(NumberType.LONG));
        toValues.put(SimpleTypes.shortObject(), new NumberConvertor(NumberType.SHORT));

        toValues.put(BigDecimal.class, new NumberConvertor(NumberType.BIGDECIMAL));
        toValues.put(BigInteger.class, new NumberConvertor(NumberType.BIGINTEGER));

        StringConvertor sConvertor = new StringConvertor();
        //toStringConvertors().put(String.class, x->x);
        toString.put(CharSequence.class, Object::toString);
        toValues.put(String.class, sConvertor);

        CharConvertor cConv = new CharConvertor();
        toString.put(Character.class, CharConvertor.toString);
        toString.put(char.class, CharConvertor.toString);
        toValues.put(Character.class, cConv);
        toValues.put(char.class, cConv);

        BooleanConvertor bConv = new BooleanConvertor();
        toString.put(Boolean.class, BooleanConvertor.toString);
        toString.put(boolean.class, BooleanConvertor.toString);
        toValues.put(Boolean.class, bConv);
        toValues.put(boolean.class, bConv);

//        DateConvertor dConv = new DateConvertor(DateType.Date);
//        toStringConvertors().put(java.util.Date.class, dConv);
//        toStringConvertors().put(java.sql.Date.class, dConv);
//        toStringConvertors().put(java.sql.Time.class, dConv);
//        toStringConvertors().put(java.sql.Timestamp.class, dConv);

//        toValueConvertors().put(java.util.Date.class, new DateConvertor(DateType.Date));
//        toValueConvertors().put(java.sql.Date.class, new DateConvertor(DateType.SqlDate));
//        toValueConvertors().put(java.sql.Time.class, new DateConvertor(DateType.SqlTime));
//        toValueConvertors().put(java.sql.Timestamp.class, new DateConvertor(DateType.SqlTimeStamp));

//        FontConvertor fntConvertor = new FontConvertor();
//        toStringConvertors().put(java.awt.Font.class, fntConvertor);
//        toValueConvertors().put(java.awt.Font.class, fntConvertor);

//        KeyStrokeConvertor ks = new KeyStrokeConvertor();
//        toStringConvertors().put(javax.swing.KeyStroke.class, ks);
//        toValueConvertors().put(javax.swing.KeyStroke.class, ks);

//        FileConvertor fconv = new FileConvertor();
//        toStringConvertors().put(java.io.File.class, fconv);
//        toValueConvertors().put(java.io.File.class, fconv);

//        DimensionConvertor dimConv = new DimensionConvertor();
//        toStringConvertors().put(Dimension2D.class, dimConv);
//        toValueConvertors().put(Dimension2D.class, dimConv);
//        toStringConvertors().put(Dimension.class, dimConv);
//        toValueConvertors().put(Dimension.class, dimConv);

//        PointConvertor pointConv = new PointConvertor();
//        toStringConvertors().put(Point.class, pointConv);
//        toValueConvertors().put(Point.class, pointConv);
//        toStringConvertors().put(Point2D.Float.class, pointConv);
//        toValueConvertors().put(Point2D.Float.class, pointConv);
//        toStringConvertors().put(Point2D.Double.class, pointConv);
//        toValueConvertors().put(Point2D.Double.class, pointConv);

//        URLConvertor urlConv = new URLConvertor();
//        toStringConvertors().put(URL.class, urlConv);
//        toValueConvertors().put(URL.class, urlConv);

//        URIConvertor uriConv = new URIConvertor();
//        toStringConvertors().put(URI.class, uriConv);
//        toValueConvertors().put(URI.class, uriConv);

//        ColorConvertor colorConv = new ColorConvertor();
//        toStringConvertors().put(Color.class, colorConv);
//        toValueConvertors().put(Color.class, colorConv);

//        CharsetConvertor charsetConv = new CharsetConvertor();
//        toStringConvertors().put(Charset.class, charsetConv);
//        toValueConvertors().put(Charset.class, charsetConv);

        for (ConvertToStringService toStrSrvc : ServiceLoader.load(ConvertToStringService.class)) {
            if (toStrSrvc != null) {
                Class type = toStrSrvc.getValueType();
                Function<Object, String> conv = toStrSrvc.getConvertor();
                if (type != null && conv != null) {
                    toString.put(type, conv);
                }
            }
        }

        for (ConvertToValueService toValSrvc : ServiceLoader.load(ConvertToValueService.class)) {
            if (toValSrvc != null) {
                Class type = toValSrvc.getValueType();
                Function<String, Object> conv = toValSrvc.getConvertor();
                if (type != null && conv != null) {
                    toValues.put(type, conv);
                }
            }
        }

        // TODO: use proj fs by spi
//        ByteSizeConvertor bsConv = new ByteSizeConvertor();
//        toStringConvertors().put(ByteSize.class, bsConv);
//        toValueConvertors().put(ByteSize.class, bsConv);
    }


}
