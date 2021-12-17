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

import java.util.*;
//import org.gocha.collection.setAt.BasicEventSet;

/**
 * Набор классов с поддержкой конструкции instanceof
 *
 * @author gocha
 */
public class ClassSet extends HashSet<Class> {
    /**
     * Конструктор
     */
    public ClassSet() {
        super(new TreeSet<>(new ClassHierarchyComparer()));
    }

    /**
     * Конструктор
     *
     * @param inverse true - в порядке от дочернего к родит; false - от родител. к дочернему
     */
    public ClassSet(boolean inverse) {
        super(new TreeSet<>(new ClassHierarchyComparer(inverse)));
    }

    /**
     * Конструктор
     *
     * @param inverse true - в порядке от дочернего к родит; false - от родител. к дочернему
     * @param itr     Начальное наполнение
     */
    public ClassSet(boolean inverse, Iterable<Class> itr) {
        super(new TreeSet<>(new ClassHierarchyComparer(inverse)));
        if (itr != null) {
            for (Class cls : itr) {
                if (cls != null) {
                    add(cls);
                }
            }
        }
    }

    /**
     * Создает пустую коллекцию для хранения перечня классов
     *
     * @return Пустая коллекция классов
     */
    protected static Collection<Class> createAssignableCollection() {
        return new ClassSet();
    }

    /**
     * Возвращает классы удовлетворяющие заданным конструкции
     *
     * @param cls             Класс
     * @param parentClasses   Результат(Класс) - является этим же классом либо родительским.<br>
     *                        Результат принимает (=) значения Параметра <pre>(cls instanceof Результат)</pre>
     * @param childrenClasses Результат - является дочерним либо этим же классом. <br>
     *                        Параметр принимает (=) результат(Класс) <pre>(Результат instanceof cls)</pre>
     * @return Классы
     */
    public Collection<Class> getAssignableFrom(Class cls, boolean parentClasses, boolean childrenClasses) {
        if (cls == null) {
            throw new IllegalArgumentException("cls == null");
        }

        Collection<Class> res = createAssignableCollection();

        for (Class c : this) {
            boolean cAssignable = c.isAssignableFrom(cls);
            boolean _resAssignCls = parentClasses && cAssignable;

            boolean clsAssignable = cls.isAssignableFrom(c);
            boolean _clsAssignRes = childrenClasses && clsAssignable;

            if (_resAssignCls || _clsAssignRes) res.add(c);
        }

        return res;
    }

    /**
     * Возвращает родительские классы (включая этот клас)
     *
     * @param cls Класс относительного которого производится отчет родительских классов
     * @return Родительские классы
     */
    public Collection<Class> getParentClassesFrom(Class cls) {
        return getAssignableFrom(cls, true, false);
    }

    /**
     * Возвращает первый родитеский класс (возможно указанный) и выборки getParentClassesFrom()
     *
     * @param cls Класс относительного которого производится отчет родительских классов
     * @return Родительский класс или null
     * @see #getParentClassesFrom(Class)
     */
    public Class getFirstParentClassesFrom(Class cls) {
        Collection<Class> col = getParentClassesFrom(cls);
        if (col.isEmpty()) return null;
        Iterator<Class> i = col.iterator();
        if (i.hasNext()) {
            return i.next();
        }
        return null;
    }

    /**
     * Возвращает последний родитеский класс (возможно указанный) и выборки getParentClassesFrom()
     *
     * @param cls Класс относительного которого производится отчет родительских классов
     * @return Родительский класс или null
     * @see #getParentClassesFrom(Class)
     */
    public Class getLastParentClassesFrom(Class cls) {
        Collection<Class> col = getParentClassesFrom(cls);
        if (col.isEmpty()) return null;
        Iterator<Class> i = col.iterator();
        Class c = null;
        while (i.hasNext()) {
            c = i.next();
        }
        return c;
    }

    /**
     * Возвращает дочерние классы (включая этот клас)
     *
     * @param cls Класс относительного которого производится отчет дочерних классов
     * @return Дочерние классы
     */
    public Collection<Class> getChildClassesFrom(Class cls) {
        return getAssignableFrom(cls, false, true);
    }

    /**
     * Возвращает первый дочерний класс (возможно указанный) и выборки getChildClassesFrom()
     *
     * @param cls Класс относительного которого производится отчет дочерних классов
     * @return Дочерний класс или null
     * @see #getChildClassesFrom(Class)
     */
    public Class getFirstChildClassesFrom(Class cls) {
        Collection<Class> col = getChildClassesFrom(cls);
        if (col.isEmpty()) return null;
        Iterator<Class> i = col.iterator();
        if (i.hasNext()) {
            return i.next();
        }
        return null;
    }

    /**
     * Возвращает последний дочерний класс (возможно указанный) и выборки getChildClassesFrom()
     *
     * @param cls Класс относительного которого производится отчет дочерних классов
     * @return Дочерний класс или null
     * @see #getChildClassesFrom(Class)
     */
    public Class getLastChildClassesFrom(Class cls) {
        Collection<Class> col = getChildClassesFrom(cls);
        if (col.isEmpty()) return null;
        Iterator<Class> i = col.iterator();
        Class c = null;
        while (i.hasNext()) {
            c = i.next();
        }
        return c;
    }

    /**
     * Стравивает два класса на предмет иерархии.
     */
    public static class ClassHierarchyComparer implements Comparator<Class> {
        /**
         * true - в порядке от дочернего к родит; false - от родител. к дочернему
         */
        public final boolean inverse;

        /**
         * Конструктор
         *
         * @param inverse true - в порядке от дочернего к родит; false - от родител. к дочернему
         */
        public ClassHierarchyComparer(boolean inverse) {
            this.inverse = inverse;
        }

        /**
         * Конструктор, стравнивает классы в порядке от
         */
        public ClassHierarchyComparer() {
            this.inverse = false;
        }

        @Override
        public int compare(Class o1, Class o2) {
            if (o1 == null && o2 == null) return 0;
            if (o1 != null && o2 == null) return inverse ? 1 : -1;
            if (o1 == null && o2 != null) return inverse ? -1 : 1;
            if (o1 == o2) return 0;

            boolean assignO1O2 = o1.isAssignableFrom(o2);
            boolean assignO2O1 = o2.isAssignableFrom(o1);

            if (assignO1O2 == assignO2O1) {
                int r = o1.getName().compareTo(o2.getName());
                if (r == 0) {
                    return inverse ? 1 : -1;
                }
                return inverse ? -r : r;
            }

            boolean itf1 = o1.isInterface();
            boolean itf2 = o2.isInterface();

            if (itf1 != itf2) {
                if (inverse) {
                    return itf1 ? -1 : 1;
                } else {
                    return itf1 ? 1 : -1;
                }
            }

            if (assignO1O2) return inverse ? 1 : -1;
            if (assignO2O1) return inverse ? -1 : 1;

            return 0;
        }
    }

//    /**
//     * Возвращает первый элемент коллекции
//     * @return Первый элемент или null, если коллеция пуста
//     */
//    public Class firstItem(){
//        if( isEmpty() )return null;
//        Set<Class> set = this.getWrappedSet();
//        if( set instanceof TreeSet ){
//            TreeSet<Class> tset = (TreeSet)setAt;
//            return tset.first();
//        }
//        Class res = null;
//        Iterable<Class> itr = this;
//        for( Class it : itr ){
//            if( it!=null ){
//                res = it;
//                break;
//            }
//        }
//        return res;
//    }
//
//    /**
//     * Возвращает последний элемент коллекции
//     * @return Последний элемент или null, если коллеция пуста
//     */
//    public Class lastItem(){
//        if( isEmpty() )return null;
//        Set<Class> set = this.getWrappedSet();
//        if( set instanceof TreeSet ){
//            TreeSet<Class> tset = (TreeSet)setAt;
//            return tset.last();
//        }
//        Class res = null;
//        Iterable<Class> itr = this;
//        for( Class it : itr ){
//            if( it!=null ){
//                res = it;
//            }
//        }
//        return res;
//    }
}
