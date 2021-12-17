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
package jcog.reflect.simple;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

/**
 * Конвертор из чисел в строку и обратно
 *
 * @author gocha
 */
public class NumberConvertor implements Function<String,Object>  {
    private final NumberType type;

    public NumberConvertor(NumberType type) {
        if (type == null) {
            throw new IllegalArgumentException("type == null");
        }
        this.type = type;
    }

    /* (non-javadoc)
     * @see org.gocha.text.simpletypes.ToStringConvertor#convertToString
     */

    public static final Function<Object,String> toString = srcData -> {
        if (srcData == null) {
            throw new IllegalArgumentException("srcData == null");
        }
        return srcData.toString();
    };

    @Override
    public Object apply(String value) {
        if (value == null) {
            return null;
        }

        Object result = null;

        try {
            result = switch (type) {
                case BYTE -> Byte.parseByte(value);
                case DOUBLE -> Double.parseDouble(value);
                case FLOAT -> Float.parseFloat(value);
                case INTEGER -> Integer.parseInt(value);
                case LONG -> Long.parseLong(value);
                case SHORT -> Short.parseShort(value);
                case BIGDECIMAL -> new BigDecimal(value);
                case BIGINTEGER -> new BigInteger(value);
            };
        } catch (NumberFormatException e) {
            result = null;
        }

        return result;
    }
}
