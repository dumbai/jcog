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
//package jcog;
//
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import xyz.cofe.collection.Iterators;
//import xyz.cofe.collection.NodesExtracter;
//import xyz.cofe.collection.map.BasicEventMap;
//import xyz.cofe.collection.map.EventMap;
//import xyz.cofe.collection.map.EventMapAdapter;
//import xyz.cofe.collection.setAt.ClassSet;
//
///**
// * Карта выполняющая доступ к дочерним узлам дерева, на основании типа узлов.
// * @author gocha
// */
//public class ClassNodesExtracterMap implements NodesExtracter
//{
//    // <editor-fold defaultstate="collapsed" desc="extractersMap">
//    /**
//     * Карта доступа к дочерним объектам класса
//     */
//    protected EventMap<Class, NodesExtracter> extractersMap = null;
//
//    protected boolean needResetClassSet = true;
//
//    /**
//     * Указывает карту доступа к классам
//     * @return Карта доступа к дочерним объектам класса
//     */
//    public Map<Class, NodesExtracter> getExtractersMap()
//    {
//        if (extractersMap == null) {
//            extractersMap = new BasicEventMap<Class, NodesExtracter>();
//            extractersMap.addEventMapListener(new EventMapAdapter<Class, NodesExtracter>(){
//                @Override
//                protected void deleted(EventMap<Class, NodesExtracter> map, Class key, NodesExtracter value) {
//                    needResetClassSet = true;
//                }
//
//                @Override
//                protected void inserted(EventMap<Class, NodesExtracter> map, Class key, NodesExtracter value) {
//                    needResetClassSet = true;
//                }
//
//                @Override
//                protected void updated(EventMap<Class, NodesExtracter> map, NodesExtracter old, Class key, NodesExtracter value) {
//                    needResetClassSet = true;
//                }
//            });
//        }
//        return extractersMap;
//    }
//
////    /**
////     * Указывает карту доступа к классам
////     * @param extractersMap Карта доступа к дочерним объектам класса
////     */
////    public void setExtractersMap(Map<Class, NodesExtracter> extractersMap)
////    {
////        this.extractersMap = extractersMap;
////    }
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="nextExtracter">
//    /**
//     * Объект достпука к дочерним элементам или null
//     */
//    protected NodesExtracter nextExtracter = null;
//
//    /**
//     * Указывает след. объект доступа, если значение null, либо нет подходящего
//     * @return Объект достпука к дочерним элементам или null
//     */
//    public NodesExtracter getNextExtracter()
//    {
//        return nextExtracter;
//    }
//
//    /**
//     * Указывает след. объект доступа, если значение null, либо нет подходящего
//     * @param nextExtracter Объект достпука к дочерним элементам или null
//     */
//    public void setNextExtracter(NodesExtracter nextExtracter)
//    {
//        this.nextExtracter = nextExtracter;
//    }// </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="defaultIterable">
//    /**
//     * Значение по умолчанию, используется если нет подходящего значение в карте и не указан след. объект доступа
//     * @see #getExtractersMap()
//     * @see #getNextExtracter()
//     */
//    protected Iterable defaultIterable = emptyIterable();
//
//    /**
//     * Указывает значение по умолчанию
//     * @return Значение по умолчанию
//     */
//    public Iterable getDefaultIterable()
//    {
//        return defaultIterable;
//    }
//
//    /**
//     * Указывает значение по умолчанию
//     * @param defaultIterable Значение по умолчанию
//     */
//    public void setDefaultIterable(Iterable defaultIterable)
//    {
//        this.defaultIterable = defaultIterable;
//    }// </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="emptyIterable">
//    private static Iterable emptyIterable = Iterators.empty();
//
//    /**
//     * Указывает пустой набор объектов
//     * @return Пустой набор объектов
//     */
//    public static Iterable emptyIterable()
//    {
//        return emptyIterable;
//    }// </editor-fold>
//
//    protected ClassSet cset = null;
//    protected ClassSet getClassSet(){
//        if( cset!=null ){
//            if( needResetClassSet )resetClassSet();
//            return cset;
//        }
//        cset = new ClassSet();
//        resetClassSet();
//        return cset;
//    }
//    protected void resetClassSet(){
//        needResetClassSet = false;
//        cset.clear();
//        Map<Class, NodesExtracter> map = getExtractersMap();
//        for( Map.Entry<Class,NodesExtracter> e : map.entrySet() ){
//            Class ce = e.getKey();
//            if( ce!=null )cset.addAt(ce);
//        }
//    }
//
//    protected boolean tryTypeCasting = true;
//
//    /**
//     * Указывает делать попытку подобрать подходящий NodesExtracter из ходя и указаного типа объекта или родительского типа.
//     * @return true - делать попытку (по умолчанию)
//     */
//    public boolean isTryTypeCasting() {
//        return tryTypeCasting;
//    }
//
//    /**
//     * Указывает делать попытку подобрать подходящий NodesExtracter из ходя и указаного типа объекта или родительского типа.
//     * @param tryTypeCasting true - делать попытку (по умолчанию)
//     */
//    public void setTryTypeCasting(boolean tryTypeCasting) {
//        this.tryTypeCasting = tryTypeCasting;
//    }
//
//    /**
//     * Извлекает дочерние объекты, для класса переданного объекта, в соответствии с картой доступа
//     * @param from Объект для которого в карте доступа осуществляется поиск
//     * @return Дочерние объекты
//     */
//    @Override
//    public Iterable extract(Object from)
//    {
//        if( from==null )
//        {
//            NodesExtracter next = getNextExtracter();
//            if( next!=null )return nextExtracter.extract(from);
//            return getDefaultIterable();
//        }
//
//        Class cls = from.getClass();
//
//        Map<Class, NodesExtracter> map = getExtractersMap();
//        if( map.containsKey(cls) )
//        {
//            NodesExtracter ext = map.get(cls);
//            if( ext==null )
//            {
//                NodesExtracter next = getNextExtracter();
//                if( next!=null )return nextExtracter.extract(from);
//                return getDefaultIterable();
//            }
//            return ext.extract(from);
//        }
//
//        if( tryTypeCasting ){
//            ClassSet cs = getClassSet();
//            Class c = cs.getLastParentClassesFrom(cls);
//            if( c!=null && map.containsKey(c) ){
//                NodesExtracter ext = map.get(c);
//                if( ext==null )
//                {
//                    NodesExtracter next = getNextExtracter();
//                    if( next!=null )return nextExtracter.extract(from);
//                    return getDefaultIterable();
//                }
//                return ext.extract(from);
//            }
//        }
//
//        NodesExtracter next = getNextExtracter();
//        if( next!=null )return nextExtracter.extract(from);
//        return getDefaultIterable();
//    }
//}
