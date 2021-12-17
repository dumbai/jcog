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
//import xyz.cofe.collection.iterators.*;
//import xyz.cofe.common.LazyValue;
//
//import java.util.*;
//
///**
// * Класс по работе с итераторами
// * @author gocha
// */
//public class Iterators
//{
//C
////    /**
////     * Конвертирует массив в итератор
////     * @param <T> Тип объектов
////     * @param array Массив
////     * @return Последовательность
////     */
////    public static <T> Iterable<T> array(T[] array)
////    {
////        return new ArrayIterable<T>(array);
////    }
//
//    /**
//     * Конвертирует массив в итератор
//     * @param <T> Тип объектов
//     * @param array Массив
//     * @return Последовательность
//     */
//    public static <T> Iterable<T> array(T ... array)
//    {
//        return new ArrayIterable<T>(array);
//    }
//
//    /**
//     * Проверяет находится ли объект в массиве
//     * @param v Объект
//     * @param src Массив
//     * @return true - находиться, false - не находится
//     */
//    public static boolean in(Object v,Object[] src)
//    {
//        if (src == null) {
//            throw new IllegalArgumentException("src == null");
//        }
//        return in(v,array(src));
//    }
//
//    /**
//     * Проверяет находится ли объект в последовательности
//     * @param v Объект
//     * @param src Последовательность
//     * @return true - находиться, false - не находится
//     */
//    public static boolean in(Object v, Iterable src)
//    {
//        if (src == null) {
//            throw new IllegalArgumentException("src == null");
//        }
//
//        if( v==null )
//        {
//            for( Object a : src )
//            {
//                if( a==null )return true;
//            }
//            return false;
//        }else{
//            for( Object b : src )
//            {
//                if( v.equals(b) )return true;
//            }
//            return false;
//        }
//    }
//
//    /**
//     * Подсчитывает кол-во элементов в последовательности
//     * @param <T> Тип объектов
//     * @param src Исходная последовательность
//     * @return Кол-во элементов
//     */
//    public static <T> long count(Iterable<T> src)
//    {
//        if (src == null) {
//            throw new IllegalArgumentException("src == null");
//        }
//        long co = 0;
//        for( @SuppressWarnings("unused") T t : src )
//        {
//            co++;
//        }
//        return co;
//    }
//
//    /**
//     * Добавляет объекты из последовательности в коллекцию
//     * @param <T> Тип объектов
//     * @param src Исходная последовательность
//     * @param collection Коллекция
//     */
//    public static <T> void addTo(Iterable<T> src, Collection<T> collection)
//    {
//        if (src == null) {
//            throw new IllegalArgumentException("src == null");
//        }
//        if (collection == null) {
//            throw new IllegalArgumentException("collection == null");
//        }
//
//        for(T o : src)collection.addAt(o);
//    }
//
//    /**
//     * Возвращает список из последовательности
//     * @param <T> Тип объектов
//     * @param src Исходная последовательность
//     * @return Список
//     */
//    public static <T> List<T> asList(Iterable<T> src)
//    {
//        if (src == null) {
//            throw new IllegalArgumentException("src == null");
//        }
//        List<T> list = new ArrayList<T>();
//        addTo(src, list);
//        return list;
//    }
//
//    /**
//     * Возвращает пустую последовательность объектов
//     * @param <T> Тип значений в последовательностях
//     * @return Пустая последовательность
//     */
//    public static<T> Iterable<T> empty()
//    {
//        return Collections::emptyIterator;
//    }
//
//    /**
//     * Возвращает последовательность с одним элементом
//     * @param <T> Тип значений в последовательностях
//     * @param item Элемент последовательности
//     * @return Последовательность
//     */
//    public static <T> Iterable<T> single(T item)
//    {
//        return new SingleIterable<T>(item);
//    }
//
//    /**
//     * Возвращает последовательность с одним элементом
//     * @param <T> Тип значений в последовательностях
//     * @param lazyValue Функция возвращающая значение
//     * @return Последовательность с один элементом
//     */
//    public static <T> Iterable<T> lazy(LazyValue<T> lazyValue)
//    {
//        return new SingleIterable<T>(lazyValue);
//    }
//
////    public static <T> Iterable<? extends T> add2(Iterable<? extends T> ... src)
////    {
////        return new AddIterable<T>(src);
////    }
//
//    /**
//     * Итератор объеденяющий последовательность значений заданых другими итераторами.
//     * <p>
//     * Для примера:<br/>
//     * Первая последовательность объектов: <b>{ A, B, C }</b> <br/>
//     * Вторая последовательность объектов: <b>{ D, E, C }</b> <br/>
//     * Результирующая последовательность будет: <b>{ A, B, C, D, E, C }</b>
//     * </p>
//     * @param <T> Тип данных в итераторе
//     * @param src Исходные итераторы
//     * @return Результирующий итератор
//     */
////    public static <T> Iterable<T> addAt(Iterable<T> ... src)
////    {
////        return new AddIterable<T>(src);
////    }
//
//    /**
//     * Итератор объеденяющий последовательность значений заданых другими итераторами.
//     * <p>
//     * Для примера:
//     * <p>Первая последовательность объектов: <b>{ A, B, C }</b>
//     * <p>Вторая последовательность объектов: <b>{ D, E, C }</b>
//     * <p>Результирующая последовательность будет: <b>{ A, B, C, D, E, C }</b>
//     * @param <T> Тип данных в итераторе
//     * @param src Исходные итераторы
//     * @return Результирующий итератор
//     */
//    public static <T> Iterable<T> sequence(Iterable<T> ... src)
//    {
//        return new AddIterable<T>(src);
//    }
//
//    /**
//     * Итератор объеденяющий последовательность значений заданых другими итераторами.
//     * <p>
//     * Для примера:
//     * <p>Первая последовательность объектов: <b>{ A, B, C }</b>
//     * <p>Вторая последовательность объектов: <b>{ D, E, C }</b>
//     * <p>Результирующая последовательность будет: <b>{ A, B, C, D, E, C }</b>
//     * @param <T> Тип данных в итераторе
//     * @param src Исходные итераторы
//     * @return Результирующий итератор
//     */
//    public static <T> Iterable<T> sequence(Iterable<Iterable<T>> src)
//    {
//        return new AddIterable<T>(src);
//    }
//
//    /**
//     * Вычитает из исходной последовательности объекты заданые второй последовательностью
//     * <p>
//     * Для примера:
//     * <p>Первая последовательность объектов: <b>{ A, B, C }</b>
//     * <p>Вторая последовательность объектов: <b>{ D, E, C }</b>
//     * <p>Результирующая последовательность будет: <b>{ A, B }</b>
//     * <p>
//     * В качестве сравнения объектов на равенство будет использоватся метод equals
//     * </p>
//     * @param <T> Тип значений в последовательностях
//     * @param src Исходная последовательность
//     * @param sub Вычитаемая последовательность
//     * @return Результатируемая последовательность
//     */
//    public static <T> Iterable<T> sub(Iterable<T> src,Iterable<T> sub)
//    {
//        return new SubIterable<T>(src,sub);
//    }
//
//    /**
//     * Вычитает из исходной последовательности объекты заданые второй последовательностью
//     * <p>
//     * Для примера:
//     * <p>
//     * Первая последовательность объектов: <b>{ A, B, C }</b>
//     * <p>
//     * Вторая последовательность объектов: <b>{ D, E, C }</b>
//     * <p>
//     * Результирующая последовательность будет: <b>{ A, B }</b>
//     * @param <T> Тип значений в последовательностях
//     * @param src Исходная последовательность
//     * @param sub Вычитаемая последовательность
//     * @param cmp Интерфес сравнения на равенство объектов
//     * @return Результатируемая последовательность
//     */
//    public static <T> Iterable<T> sub(Iterable<T> src,Iterable<T> sub,CompareEqu<T> cmp)
//    {
//        return new SubIterable<T>(src,sub,cmp);
//    }
//
//    /**
//     * Вычитает из исходной последовательности объекты заданые второй последовательностью
//     * <p>
//     * Для примера:
//     * <p>
//     * Первая последовательность объектов: <b>{ A, B, C }</b>
//     * <p>
//     * Вторая последовательность объектов: <b>{ D, E, C }</b>
//     * <p>
//     * Результирующая последовательность будет: <b>{ A, B }</b>
//     * @param <T> Тип значений в последовательностях
//     * @param src Исходная последовательность
//     * @param sub Вычитаемая последовательность
//     * @param cmp Интерфес сравнения на равенство объектов
//     * @return Результатируемая последовательность
//     */
//    public static <T> Iterable<T> sub(Iterable<T> src,Iterable<T> sub,Comparator<T> cmp)
//    {
//        final Comparator fcmp = cmp;
//        return sub( src, sub, new CompareEqu() {
//            @Override
//            public boolean isEqu(Object a, Object b) {
//                if( fcmp==null )
//                {
//                    return a==null ? b==null : a.equals(b);
//                }else{
//                    return fcmp.compare(a, b)==0;
//                }
//            }
//        });
//    }
//
//    /**
//     * Итератор использующий буффер объектов. Предварительно копирует объекты в буфер и уже по ним проходит.
//     * <p>
//     * В качестве буфера используется класс java.util.ArrayList
//     * </p>
//     * @param <T> Тип значений в последовательностях
//     * @param src Исходная последовательность
//     * @return Копия объектов
//     */
//    public static <T> Iterable<T> buffer(Iterable<T> src)
//    {
//        return new BufferIterable<T>(src);
//    }
//
//    /**
//     * Итератор использующий буффер объектов. Предварительно копирует объекты в буфер и уже по ним проходит.
//     * @param <T> Тип значений в последовательностях
//     * @param src Исходная последовательность
//     * @param buffer Буфер объектов
//     * @return Копия объектов
//     */
//    public static <T> Iterable<T> buffer(Iterable<T> src,List<T> buffer)
//    {
//        return new BufferIterable<T>(buffer,src);
//    }
//
//    /**
//     * Возвращает последовательность содержащую только те объекты, которые удалетворяют предикату
//     * @param <T> Тип значений в последовательностях
//     * @param src Исходная последовательность
//     * @param predicate Предикат
//     * @return Последовательность значений удалетворяющих предикату
//     */
//    public static <T> Iterable<T> predicate(Iterable<T> src,Predicate<T> predicate)
//    {
//        return new PredicateIterable<T>(predicate, src);
//    }
//
//    /**
//     * Возвращает последовательность содержащую только те объекты, которые удалетворяют предикату
//     * @param <T> Тип значений в последовательностях
//     * @param src Исходная последовательность
//     * @param predicate Предикат
//     * @return Последовательность значений удалетворяющих предикату
//     */
//    public static <T> Iterable<T> predicate(T[] src,Predicate<T> predicate)
//    {
//        return new PredicateIterable<T>(predicate, array(src));
//    }
//
//    /**
//     * Возвращает последовательность содержащую объекты сконвертированные в другой тип данных
//     * @param <From> Тип данных из которого необходимо сконвертировать
//     * @param <To> Тип данных в который необходимо сконвертировать
//     * @param src Исходная последовательность
//     * @param convertor Конвертор типов
//     * @return Последовательность сконвертированых объектов
//     */
//    public static <From,To> Iterable<To> convert(Iterable<From> src,Convertor<From,To> convertor)
//    {
//        return new ConvertIterable<From,To>(src, convertor);
//    }
//
//    /**
//     * Возвращает последовательность содержащую объекты сконвертированные в другой тип данных
//     * @param <From> Тип данных из которого необходимо сконвертировать
//     * @param <To> Тип данных в который необходимо сконвертировать
//     * @param src Исходная последовательность
//     * @param convertor Конвертор типов
//     * @return Последовательность сконвертированых объектов
//     */
//    public static <From,To> Iterable<To> convert(From[] src,Convertor<From,To> convertor)
//    {
//        return convert( array(src), convertor );
//    }
//
//    /**
//     * Итератор возвращающий минимальные значения из указанй последовательности
//     * @param <T> Тип значений в последовательностях
//     * @param src Исходная последовательность
//     * @param cmp Интерфейс сравнения объектов
//     * @return Последовательность содержащая минимальные значения
//     */
//    public static <T> Iterable<T> min(Iterable<T> src,Comparator<T> cmp)
//    {
//        return new MinMaxIterable<T>(src,cmp,false);
//    }
//
//    /**
//     * Итератор возвращающий максимальные значения из указанй последовательности
//     * @param <T> Тип значений в последовательностях
//     * @param src Исходная последовательность
//     * @param cmp Интерфейс сравнения объектов
//     * @return Последовательность содержащая максимальные значения
//     */
//    public static <T> Iterable<T> max(Iterable<T> src,Comparator<T> cmp)
//    {
//        return new MinMaxIterable<T>(src,cmp,true);
//    }
//
//    /**
//     * Создает обратную последовательность значений
//     * @param <T> Тип значений в последовательностях
//     * @param src Исходная последовательность
//     * @return Итератор содержащая обратную последовательность значения
//     */
//    public static <T> Iterable<T> reverse(Iterable<T> src)
//    {
//        return new ReverseInterable<T>(src);
//    }
//
//    /**
//     * Создает последовательность неповторяющихся объектов
//     * @param <T> Тип значений в последовательностях
//     * @param src Исходная последовательность
//     * @return Последовательнось неповторяющихся объектов
//     */
//    public static <T> Iterable<T> setAt(Iterable<T> src)
//    {
//        return new SetIterable<T>(src);
//    }
//
//    /**
//     *
//     * @param <T> Тип значений в последовательностях
//     * @param src Исходная последовательность
//     * @param comparer Интерфейс сравнения объектов
//     * @return Последовательнось неповторяющихся объектов
//     */
//    public static <T> Iterable<T> setAt(Iterable<T> src, CompareEqu<T> comparer)
//    {
//        return new SetIterable<T>(src,comparer);
//    }
//
//    /**
//     * Итератор по деверу объектов заданному через интерфес NodesExtracter
//     * @param <T> Тип значений в последовательностях
//     * @param src Корневой объект
//     * @param extracter Итерфес доступа к дочерним элементам
//     * @return Последовательность объектов
//     */
//    public static <T> Iterable<T> tree(T src, xyz.cofe.collection.NodesExtracter<T,T> extracter)
//    {
//        return new TreeIterable<T>(src,extracter);
//    }
//}
