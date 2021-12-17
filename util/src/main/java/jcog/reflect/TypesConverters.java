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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Конвертор типов данных
 *
 * @author gocha
 */
public class TypesConverters
//implements xyz.cofe.common.Cloneable
{
    public final Map<Class, Function<Object, String>> toString = new HashMap<>();
    public final Map<Class, Function<String, Object>> toValues = new HashMap<>();

    public TypesConverters() {
    }


    /**
     * Возвращает подходящий конвертор из указаного типа данных
     *
     * @param c Из кагого типа данных
     * @return Конвертор или null, если нет подходящего
     */
    public Function<Object, String> toStringFrom(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("c == null");
        }

        ToStringConverters convs = (ToStringConverters) toString;
//        if (convs == null) return null;

        for (Map.Entry<Class, Function<Object, String>> classToStringConverterEntry : convs.entrySet()) {
            if (classToStringConverterEntry.getKey() != null) {
                if (classToStringConverterEntry.getKey() == c) {
                    return classToStringConverterEntry.getValue();
                }
            }
        }
        return null;

    }

    /**
     * Возвращает подходящий конвертор в объектное представление
     *
     * @param c В какое объектное представление необходимо конвертировать
     * @return Конвертор или null если такого нет
     */
    public Function<String, Object> toValueFor(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("c == null");
        }

        Map<Class, Function<String, Object>> convs = toValues;
        if (convs == null) return null;

        for (Map.Entry<Class, Function<String, Object>> classToValueConvertorEntry : convs.entrySet()) {
            if (classToValueConvertorEntry.getKey() != null) {
                if (classToValueConvertorEntry.getKey() == c) {
                    return classToValueConvertorEntry.getValue();
                }
            }
        }
        return null;

    }
}