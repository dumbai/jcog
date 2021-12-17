///*
// * The MIT License
// *
// * Copyright 2014 Kamnev Georgiy (nt.gocha@gmail.com).
// *
// * Данная лицензия разрешает, безвозмездно, лицам, получившим копию данного программного
// * обеспечения и сопутствующей документации (в дальнейшем именуемыми "Программное Обеспечение"),
// * использовать Программное Обеспечение без ограничений, включая неограниченное право на
// * использование, копирование, изменение, объединение, публикацию, распространение, сублицензирование
// * и/или продажу копий Программного Обеспечения, также как и лицам, которым предоставляется
// * данное Программное Обеспечение, при соблюдении следующих условий:
// *
// * Вышеупомянутый копирайт и данные условия должны быть включены во все копии
// * или значимые части данного Программного Обеспечения.
// *
// * ДАННОЕ ПРОГРАММНОЕ ОБЕСПЕЧЕНИЕ ПРЕДОСТАВЛЯЕТСЯ «КАК ЕСТЬ», БЕЗ ЛЮБОГО ВИДА ГАРАНТИЙ,
// * ЯВНО ВЫРАЖЕННЫХ ИЛИ ПОДРАЗУМЕВАЕМЫХ, ВКЛЮЧАЯ, НО НЕ ОГРАНИЧИВАЯСЬ ГАРАНТИЯМИ ТОВАРНОЙ ПРИГОДНОСТИ,
// * СООТВЕТСТВИЯ ПО ЕГО КОНКРЕТНОМУ НАЗНАЧЕНИЮ И НЕНАРУШЕНИЯ ПРАВ. НИ В КАКОМ СЛУЧАЕ АВТОРЫ
// * ИЛИ ПРАВООБЛАДАТЕЛИ НЕ НЕСУТ ОТВЕТСТВЕННОСТИ ПО ИСКАМ О ВОЗМЕЩЕНИИ УЩЕРБА, УБЫТКОВ
// * ИЛИ ДРУГИХ ТРЕБОВАНИЙ ПО ДЕЙСТВУЮЩИМ КОНТРАКТАМ, ДЕЛИКТАМ ИЛИ ИНОМУ, ВОЗНИКШИМ ИЗ, ИМЕЮЩИМ
// * ПРИЧИНОЙ ИЛИ СВЯЗАННЫМ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ ИЛИ ИСПОЛЬЗОВАНИЕМ ПРОГРАММНОГО ОБЕСПЕЧЕНИЯ
// * ИЛИ ИНЫМИ ДЕЙСТВИЯМИ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ.
// */
//package jcog.reflect;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.util.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import xyz.cofe.collection.Convertor;
//import xyz.cofe.collection.NodesExtracter;
//import xyz.cofe.collection.Predicate;
//import xyz.cofe.common.Reciver;
//
///**
// * Утилита по работе с типами
// * @author gocha
// */
//public class TypesUtil
//{
//    /**
//     * Предикаты по работе с типами данных JVM
//     */
//    public static class Predicates
//    {
//        /**
//         * Возвращает предикат проверки метода без параметров
//         * @return Предикат
//         */
//        public static Predicate<Method> hasEmptyParameters()
//        {
//            return hasParameters(emptyParametersArray);
//        }
//
//        /**
//         * Возвращает предикат строгой проверкти типов аргументов метода
//         * @param params Типы аргметов метода
//         * @return Предикат
//         */
//        public static Predicate<Method> hasParameters(Class ... params)
//        {
//            if (params == null) {
//                throw new IllegalArgumentException("params == null");
//            }
//            final Class[] _p = params;
//            return new Predicate<Method>() {
//                @Override
//                public boolean validate(Method value) {
//                    Class[] p = value.getParameterTypes();
//                    if( p.length!=_p.length )return false;
//                    for( int i=0; i<p.length; i++ )
//                    {
//                        if( !p[i].equals(_p[i]) )return false;
//                    }
//                    return true;
//                }
//            };
//        }
//
//        /**
//         * Предикат стравения <i>value</i> <b>instanceOf</b> <i>target</i>
//         * @param target Класс
//         * @return Предикат
//         */
//        public static Predicate<Class> classInstanceOf(Class target)
//        {
//            final Class fTarget = target;
//            return new Predicate<Class>() {
//                @Override
//                public boolean validate(Class value) {
//                    if( fTarget==null )return value==null;
//                    if( value==null )return false;
//                    return AinstanceOfB(value,fTarget);
//                }
//            };
//        }
//
//        /**
//         * Предикат сравнения <i>value</i> <b>equals</b> ( <i>target</i> )
//         * @param target Тип данных
//         * @return Предикат
//         */
//        public static Predicate<Class> classEquals(Class target)
//        {
//            final Class fTarget = target;
//            return new Predicate<Class>() {
//                @Override
//                public boolean validate(Class value) {
//                    if( fTarget==null )return value==null;
//                    return value==null ? false : fTarget.equals(value);
//                }
//            };
//        }
//
//        /**
//         * Предикат - возвращает true, если метода возвращает указанный тип
//         * @param type Возвращаемый тип
//         * @return Предикат
//         */
//        public static Predicate<Method> returns(Class type)
//        {
//            if (type == null) {
//                throw new IllegalArgumentException("type == null");
//            }
//            final Class tip = type;
//            return new Predicate<Method>()
//            {
//                @Override
//                public boolean validate(Method value)
//                {
//                    Class ret = value.getReturnType();
//    //                return ret.equals(tip);
//                    return AinstanceOfB(ret, tip);
//                }
//            };
//        }
//
//        /**
//         * Предикат - возвращает true, если название метода начинается с указанного текста
//         * @param text Текст
//         * @return Предикат
//         */
//        public static Predicate<Method> nameStart(String text)
//        {
//            if (text == null) {
//                throw new IllegalArgumentException("text == null");
//            }
//            final String txt = text;
//            return new Predicate<Method>() {
//                @Override
//                public boolean validate(Method value) {
//                    return value.getName().startsWith(txt);
//                }
//            };
//        }
//
//        /**
//         * Предикат , возвращает true если метод имеет указаную аннатацию
//         * @param annClass Аннатация
//         * @return Предикат
//         */
//        public static Predicate<Method> hasAnnotation(Class annClass)
//        {
//            if (annClass == null) {
//                throw new IllegalArgumentException("annClass == null");
//            }
//
//            final Class ann = annClass;
//
//            return new Predicate<Method>() {
//                @Override
//                public boolean validate(Method value)
//                {
//                    Object a = value.getAnnotation(ann);
//                    return a!=null;
//                }
//            };
//        }
//
//        /**
//         * Предикат: Сверяет на возможность вызова метода с указанными аргументами
//         * @param args Параметры
//         * @return Предикат
//         */
//        public static Predicate<Method> callableArguments(Object[] args)
//        {
//            if (args == null) {
//                throw new IllegalArgumentException("args == null");
//            }
//            final Object[] fa = args;
//
//            return new Predicate<Method>() {
//                @Override
//                public boolean validate(Method value)
//                {
//                    Class[] types = value.getParameterTypes();
//                    return isCallableArguments(types, fa);
//                }
//            };
//        }
//    }
//
//    /**
//     * Предикаты по работе с типами данных JVM
//     */
//    public static final Predicates predicates = new Predicates();
//
//    /**
//     * Итераторы по работе с типами данных JVM
//     */
//    public static class Iterators
//    {
//        /**
//        * Возвращает отсортированную последовательность по определенному критерию
//        * @param <T> Тип значений в последовательностях
//        * @param src Исходная последовательность
//        * @param comparer Критерий сортировки
//        * @return Отсортированная последовательность
//        */
//        public static <T> Iterable<T> sort(Iterable<T> src,Comparator<T> comparer)
//        {
//            if (src == null) {
//                throw new IllegalArgumentException("src == null");
//            }
//            if (comparer == null) {
//                throw new IllegalArgumentException("comparer == null");
//            }
//
//            List<T> list = toList(src,ArrayList.class);
//            Collections.sort(list, comparer);
//
//            return list;
//        }
//
//        /**
//        * Возвращает отсортированную последовательность
//        * @param <T> Тип значений в последовательностях
//        * @param src Исходная последовательность
//        * @return Отсортированная последовательность
//        */
//        public static <T extends Comparable<? super T>> Iterable<T> sort(Iterable<T> src)
//        {
//            if (src == null) {
//                throw new IllegalArgumentException("src == null");
//            }
//
//            List<T> list = toList(src,ArrayList.class);
//            Collections.sort(list);
//
//            return list;
//        }
//
//        /**
//        * Конвертирует последовательность в массив
//        * @param <T> Тип объектов в последовательности
//        * @param src Исходная последовательность
//        * @param array Пустой массив
//        * @return Сконвертированная последовательность
//        */
//        public static <T> T[] toArray(Iterable<? extends T> src, T[] array )
//        {
//            if (src == null) {
//                throw new IllegalArgumentException("src == null");
//            }
//            if (array == null) {
//                throw new IllegalArgumentException("array == null");
//            }
//
//            return toArrayList(src).toArray(array);
//        }
//
//        /**
//        * Конвертирует последовательность в список
//        * @param <T> Тип объектов в последовательности
//        * @param src Исходная последовательность
//        * @return Список
//        */
//        public static <T> ArrayList<T> toArrayList(Iterable<? extends T> src)
//        {
//            return (ArrayList<T>)toList(src, ArrayList.class);
//        }
//
//        /**
//        * Конвертирует последовательность в список
//        * @param <T> Тип объектов в последовательности
//        * @param src Исходная последовательность
//        * @return Список
//        */
//        public static <T> Vector<T> toVector(Iterable<? extends T> src)
//        {
//            return (Vector<T>)toList(src, Vector.class);
//        }
//
//        /**
//        * Конвертирует последовательность в список
//        * @param <T> Тип объектов в последовательности
//        * @param src Исходная последовательность
//        * @param listClass Класс реализующий список (должен иметь конструктор по умолчанию)
//        * @return Список или null если не смог создать список
//        */
//        public static <T> List<T> toList(Iterable<? extends T> src,Class<? extends List> listClass)
//        {
//            if (src == null) {
//                throw new IllegalArgumentException("src == null");
//            }
//            if (listClass == null) {
//                throw new IllegalArgumentException("listClass == null");
//            }
//
//            try
//            {
//                List result = listClass.newInstance();
//    //            addTo(src, result);
//                for(T o : src)result.addAt(o);
//                return result;
//            } catch (InstantiationException ex) {
//                Logger.getLogger(TypesUtil.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IllegalAccessException ex) {
//                Logger.getLogger(TypesUtil.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//            return null;
//        }
//
//        /**
//        * Итератор - Фильт возвращающий объекты заданого класса (сравнивае строго)
//        * @param <T> Интересующий класс
//        * @param src Исходное множество объектов
//        * @param c Интересующий класс
//        * @param includeNull Включать или нет пустые ссылки
//        * @return Последовательность объектов определенного класса
//        */
//        public static <T> Iterable<T> classFilter(Iterable src,Class<T> c,boolean includeNull)
//        {
//            if (src == null) {
//                throw new IllegalArgumentException("src == null");
//            }
//            if (c == null) {
//                throw new IllegalArgumentException("c == null");
//            }
//
//            final boolean incNull = includeNull;
//            final Class need = c;
//
//            Predicate<T> p = new Predicate<T>() {
//                public boolean validate(T value) {
//                    if( value==null && incNull )return true;
//                    Class c = value.getClass();
//                    return need.equals(c);
//                }
//            };
//
//            return xyz.cofe.collection.Iterators.predicate(src, p);
//        }
//
//        /**
//        * Итератор - Фильт возвращающий объекты заданого класса.
//        * <p>
//        * Сравнение объектов производиться функцией isAssignableFrom т.е.
//        * <b><i>объект</i> instanceof <i>Интересующий класс</i></b>
//        * </p>
//        * @param <T> Интересующий класс
//        * @param src Исходное множество объектов
//        * @param c Интересующий класс
//        * @param includeNull Включать или нет пустые ссылки
//        * @return Последовательность объектов определенного класса
//        */
//        public static <T> Iterable<T> isClassFilter(Iterable src,Class<T> c,boolean includeNull)
//        {
//            if (src == null) {
//                throw new IllegalArgumentException("src == null");
//            }
//            if (c == null) {
//                throw new IllegalArgumentException("c == null");
//            }
//
//            final boolean incNull = includeNull;
//            final Class need = c;
//
//            Predicate<T> p = new Predicate<T>() {
//                @Override
//                public boolean validate(T value) {
//                    if( value==null )
//                    {
//                        if( incNull )return true;
//                    }else{
//                        Class c = value.getClass();
//                        return need.isAssignableFrom(c);
//                    }
//                    return false;
//                }
//            };
//
//            return xyz.cofe.collection.Iterators.predicate(src, p);
//        }
//
//        /**
//         * Возвращает параметры/агруметы метода
//         * @param method Метод
//         * @return Пераметры
//         */
//        public static Iterable<Class> paramtersOf(Method method){
//            if (method == null) {
//                throw new IllegalArgumentException("method == null");
//            }
//            Class[] params = method.getParameterTypes();
//            return xyz.cofe.collection.Iterators.<Class>array(params);
//        }
//
//        /**
//         * Возвращает публичные методы объекта
//         * @param src объект
//         * @param predicate Условие отбора
//         * @return Перечисление методов
//         */
//        public static Iterable<Method> methodsOf(Object src,Predicate<Method> predicate)
//        {
//            if (src == null) {
//                throw new IllegalArgumentException("src == null");
//            }
//            if (predicate == null) {
//                throw new IllegalArgumentException("predicate == null");
//            }
//            Class c = src.getClass();
//            return xyz.cofe.collection.Iterators.<Method>predicate(
//                    xyz.cofe.collection.Iterators.<Method>array(c.getMethods()), predicate);
//        }
//
//        /**
//         * Возвращает публичные методы объекта
//         * @param obj объект
//         * @return Перечисление методов
//         */
//        public static Iterable<Method> methodsOf( Object obj )
//        {
//            if (obj == null) {
//                throw new IllegalArgumentException("obj == null");
//            }
//            return xyz.cofe.collection.Iterators.<Method>array(obj.getClass().getMethods());
//        }
//
//        /**
//         * Возвращает публичные методы класса
//         * @param cls Класс
//         * @return Перечисление методов
//         */
//        public static Iterable<Method> methodsOf( Class cls )
//        {
//            if (cls == null) {
//                throw new IllegalArgumentException("cls == null");
//            }
//            return xyz.cofe.collection.Iterators.<Method>array(cls.getMethods());
//        }
//
//        /**
//         * Возвращает публичные поля класса
//         * @param cls Класс
//         * @return Перечисление полей
//         */
//        public static Iterable<Field> fieldsOf( Class cls )
//        {
//            if (cls == null) {
//                throw new IllegalArgumentException("cls == null");
//            }
//            return xyz.cofe.collection.Iterators.<Field>array(cls.getFields());
//        }
//
//        public static Iterable<ValueController> fieldsControllersOf( Class cls, Object owner ){
//            if (cls == null) {
//                throw new IllegalArgumentException("cls == null");
//            }
//            List<ValueController> _vc = new ArrayList<ValueController>();
//            for( Field f : fieldsOf(cls) ){
//                FieldController fc = new FieldController(owner, f);
//                _vc.addAt( fc );
//            }
//            return  _vc;
//        }
//
//        /**
//         * Возвращает объевленные методы только в этом классе данного объекта
//         * @param obj Объект
//         * @return Перечисление методов
//         */
//        public static Iterable<Method> declaredMethodsOf( Object obj )
//        {
//            if (obj == null) {
//                throw new IllegalArgumentException("obj == null");
//            }
//            return xyz.cofe.collection.Iterators.<Method>array(obj.getClass().getDeclaredMethods());
//        }
//
//        /**
//         * Возвращает публичные свойства объекта
//         * @param object Объект
//         * @return Свойства
//         */
//        public static Iterable<ValueController> propertiesOf(Object object)
//        {
//            if (object == null) {
//                throw new IllegalArgumentException("object == null");
//            }
//
//            return PropertyController.buildControllers(object);
//        }
//
//        /**
//         * Возвращает публичные свойства объекта
//         * @param cls класс
//         * @return Свойства
//         */
//        public static Iterable<? extends ValueController> propertiesOfClass(Class cls)
//        {
//            if (cls == null) {
//                throw new IllegalArgumentException("object == null");
//            }
//
//            return PropertyController.buildPropertiesList(cls);
//        }
//    }
//
//    /**
//     * Итераторы по работе с типами данных JVM
//     */
//    public static final Iterators iterators = new Iterators();
//
//    /**
//     * Выполняет конструкция A <b>instanceOf</b> B
//     * @param cA Класс A
//     * @param cB Класс B
//     * @return true - удалетворяет конструкции, false - не удавлетворяет
//     */
//    public static boolean AinstanceOfB(Class cA, Class cB)
//    {
//        if (cA == null) {
//            throw new IllegalArgumentException("cA == null");
//        }
//        if (cB == null) {
//            throw new IllegalArgumentException("cB == null");
//        }
//        return cB.isAssignableFrom(cA);
//    }
//
//    /**
//     * Сверяет на возможность вызова метода с указанными аргументами
//     * @param types Типы принимаемых параметорв
//     * @param args Параметры
//     * @return true - вызвать возможно, false - не возможно вызвать
//     */
//    public static boolean isCallableArguments(Class[] types,Object[] args)
//    {
//        if (types == null) {
//            throw new IllegalArgumentException("types == null");
//        }
//        if (args == null) {
//            throw new IllegalArgumentException("args == null");
//        }
//
//        if (types.length != args.length) {
//            return false;
//        }
//
//        boolean callable = true;
//
//        for (int paramIdx = 0; paramIdx < types.length; paramIdx++) {
//            Class cMethodPrm = types[paramIdx];
//            if (args[paramIdx] == null) {
//                if( cMethodPrm.isPrimitive() ){
//                    callable = false;
//                    break;
//                }
//                continue;
//            }
//
//            Class cArg = args[paramIdx].getClass();
//
//            boolean assign = cMethodPrm.isAssignableFrom(cArg);
//            if (!assign)
//            {
//                callable = false;
//                break;
//            }
//        }
//
//        return callable;
//    }
//
//    // <editor-fold defaultstate="collapsed" desc="textMap2vc / vc2textMap">
//    /**
//     * Копирует значения из текстовой карты
//     *
//     * @param map Текстовая карта (откуда копировать)
//     * @param valueControllers Значения (куда копировать)
//     * @return Кол-во скопированных значений
//     */
//    public static int textMapToValueControllers(
//            Map map,
//            Iterable<ValueController> valueControllers) {
//        return textMapToValueControllers(map, valueControllers, null, null);
//    }
//
//    /**
//     * Копирует значения в текстовую карту
//     *
//     * @param valueControllers Значения
//     * @param map Текстовая карта
//     * @return Кол-во скопированных значений
//     */
//    public static int valueControllersToTextMap(
//            Iterable<ValueController> valueControllers,
//            Map map) {
//        return valueControllersToTextMap(valueControllers, map, null, null);
//    }
//
//    /**
//     * Копирует текстовую карту (значения) в карту значений (свойств/полей)
//     *
//     * @param map Исходная карта - (текстовая)
//     * @param valueControllers Конечная карта - (значения)
//     * @param convertors Конвертор типов (текст/значение; может быть null)
//     * @param mapKeyConvertor Конвертор ключей текстовой карты (может быть null)
//     * @param valueNameConvertor Конвертор имен свойств (может быть null)
//     * @param errorReciver Прием сообщений ошибок (может быть null)
//     * @return Кол-во скопированных значений
//     */
//    public static int textMapToValueControllers(
//            Map map,
//            Iterable<? extends ValueController> valueControllers,
//            TypesConverters convertors,
//            Convertor<String, String> mapKeyConvertor,
//            Convertor<String, String> valueNameConvertor,
//            Reciver<Throwable> errorReciver) {
//        if (valueControllers == null) {
//            throw new IllegalArgumentException("valueControllers == null");
//        }
//        if (map == null) {
//            throw new IllegalArgumentException("map == null");
//        }
//        if (convertors == null)
//            convertors = DefaultTypesConvertors.instance();
//
//        int res = 0;
//        for (ValueController vc : valueControllers) {
//            String vcName = vc.getName();
//            if (valueNameConvertor != null)
//                vcName = valueNameConvertor.convert(vcName);
//            if (vcName == null)
//                continue;
//
//            Class vcClass = vc.getType();
//
//            ToValueConvertor c2v = convertors.toValueFor(vcClass);
//
//            if (c2v == null)
//                continue;
//            try {
//                if (mapKeyConvertor != null) {
//                    for (Object oMapKey : map.keySet()) {
//                        if (oMapKey == null)
//                            continue;
//
//                        String sMapKey = oMapKey.toString();
//                        sMapKey = mapKeyConvertor.convert(sMapKey);
//                        if (sMapKey == null)
//                            continue;
//
//                        if (sMapKey.equals(vcName)) {
//                            Object oval = map.get(oMapKey);
//                            String sval = null;
//                            if (oval != null)
//                                sval = oval.toString();
//                            if (sval != null) {
//                                Object destValue = c2v.convertToValue(sval);
//                                vc.setValue(destValue);
//                                res++;
//                            }
//                        }
//                    }
//                } else {
//                    if (map.containsKey(vcName)) {
//                        Object oval = map.get(vcName);
//                        String sval = null;
//                        if (oval != null)
//                            sval = oval.toString();
//                        if (sval != null) {
//                            Object destValue = c2v.convertToValue(sval);
//                            vc.setValue(destValue);
//                            res++;
//                        }
//                    }
//                }
//            } catch (Throwable t) {
//                if (errorReciver != null) {
//                    errorReciver.recive(t);
//                } else {
//                    System.err.println(t.getMessage());
//                }
//            }
//        }
//
//        return res;
//    }
//
//    /**
//     * Копирует текстовую карту (значения) в карту значений (свойств/полей)
//     *
//     * @param map Исходная карта - (текстовая)
//     * @param valueControllers Конечная карта - (значения)
//     * @param convertors Конвертор типов (текст/значение; может быть null)
//     * @param errorReciver Прием сообщений ошибок (может быть null)
//     * @return Кол-во скопированных значений
//     */
//    public static int textMapToValueControllers(
//            Map map,
//            Iterable<? extends ValueController> valueControllers,
//            TypesConverters convertors,
//            Reciver<Throwable> errorReciver) {
//        return textMapToValueControllers(map, valueControllers, convertors, null, null, errorReciver);
//    }
//
//    /**
//     * Копирует карту значений в текстовую карту
//     *
//     * @param valueControllers Исходная карта значений
//     * @param map Конечная текстовая карта
//     * @param convertors Конвертор типов (текст/значение; может быть null)
//     * @param mapKeyConvertor Конвертор ключей текстовой карты (может быть null)
//     * @param valueNameConvertor Конвертор имен свойств (может быть null)
//     * @param errorReciver Прием сообщений ошибок (может быть null)
//     * @return Кол-во скопированных значений
//     */
//    public static int valueControllersToTextMap(
//            Iterable<ValueController> valueControllers,
//            Map map,
//            TypesConverters convertors,
//            Convertor<String, String> mapKeyConvertor,
//            Convertor<String, String> valueNameConvertor,
//            Reciver<Throwable> errorReciver) {
//        if (valueControllers == null) {
//            throw new IllegalArgumentException("valueControllers == null");
//        }
//        if (map == null) {
//            throw new IllegalArgumentException("map == null");
//        }
//        if (convertors == null)
//            convertors = DefaultTypesConvertors.instance();
//
//        int count = 0;
//
//        for (ValueController vc : valueControllers) {
//            String vcName = vc.getName();
//            if (valueNameConvertor != null)
//                vcName = valueNameConvertor.convert(vcName);
//            if (vcName == null)
//                continue;
//
//            Class vcClass = vc.getType();
//            ToStringConverter c2s = convertors.toStringFrom(vcClass);
//
//            if (c2s == null)
//                continue;
//            try {
//                Object v = vc.getValue();
//                if (v == null)
//                    continue;
//
//                String sval = c2s.convertToString(v);
//                if (sval != null) {
//                    String key = vc.getName();
//                    if (mapKeyConvertor != null)
//                        key = mapKeyConvertor.convert(key);
//                    if (key == null)
//                        continue;
//
//                    map.put(key, sval);
//                    count++;
//                }
//            } catch (Throwable t) {
//                if (errorReciver != null) {
//                    errorReciver.recive(t);
//                } else {
//                    System.err.println(t.getMessage());
//                }
//            }
//        }
//
//        return count;
//    }
//
//    /**
//     * Копирует карту значений в текстовую карту
//     *
//     * @param valueControllers Исходная карта значений
//     * @param map Конечная текстовая карта
//     * @param convertors Конвертор типов (текст/значение; может быть null)
//     * @param errorReciver Прием сообщений ошибок (может быть null)
//     * @return Кол-во скопированных значений
//     */
//    public static int valueControllersToTextMap(
//            Iterable<ValueController> valueControllers,
//            Map map,
//            TypesConverters convertors,
//            Reciver<Throwable> errorReciver) {
//        return valueControllersToTextMap(valueControllers, map, convertors, null, null, errorReciver);
//    }
//    // </editor-fold>
//
//    private static NodesExtracter classMethodsExtracter = null;
//
//    /**
//     * Возвращает интерфейс доступа к методам класса
//     * @return интерфейс доступа к методам класса
//     */
//    public static NodesExtracter classMethodsExtracter()
//    {
//        if( classMethodsExtracter!=null )return classMethodsExtracter;
//        classMethodsExtracter = new NodesExtracter() {
//            @Override
//            public Iterable extract(Object from)
//            {
//                if( from==null )return null;
//                if( !(from instanceof Class) )return null;
//                return Iterators.methodsOf((Class)from);
//            }
//        };
//        return null;
//    }
//
//    private static NodesExtracter methodParametersExtracter = null;
//
//    /**
//     * Возвращает интерфейс доступа к типам параметров метода
//     * @return интерфейс доступа к типам параметров метода
//     */
//    public static NodesExtracter methodParametersExtracter()
//    {
//        if( methodParametersExtracter!=null )return methodParametersExtracter;
//        methodParametersExtracter = new NodesExtracter() {
//            @Override
//            public Iterable extract(Object from)
//            {
//                if( from==null )return null;
//                if( !(from instanceof Method) ) {
//                    return null;
//                }
//                return Iterators.paramtersOf((Method)from);
//            }
//        };
//        return null;
//    }
//
//    /**
//     * Пустой массив: Class[]
//     */
//    public static final Class[] emptyParametersArray = new Class[]{};
//
//    /**
//     * Читает и заполняет поля/свойства объекта значениями указанными в текстовой карте<br>
//     * @param obj Объект чьи поля заполняются
//     * @param tdata Данные
//     * @param conv Конвертор текстового представления
//     */
//    public static void readTextConfig(Object obj,Map<String,String> tdata,TypesConverters conv){
//        if (obj== null) {
//            throw new IllegalArgumentException("obj==null");
//        }
//        if (tdata== null) {
//            throw new IllegalArgumentException("data==null");
//        }
//        if( conv==null )conv = DefaultTypesConvertors.instance();
//
//        for( Field field : Iterators.fieldsOf(obj.getClass()) ){
//            String fname = field.getName();
//            if( tdata.containsKey(fname) ){
//                Class fclazz = field.getType();
//                ToValueConvertor tvc = conv.toValueFor(fclazz);
//                if( tvc!=null ){
//                    try {
//                        Object odata = tvc.convertToValue(tdata.get(fname));
//                        field.setAt(obj, odata);
//                    } catch (Throwable ex) {
//                        Logger.getLogger(TypesUtil.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }
//        }
//
//        // format
//        // path ::= field { '.' field }
//        // field ::= name [ '[' index ']' ]
////        for( String key : tdata.keySet() ){
////        }
//    }
//
////    public static class CPath {
////        public List<CField> field = null;
////    }
////
////    public static class CField {
////        public String name = null;
////        public CIndex index = null;
////    }
////
////    public static class CIndex {
////    }
//}
