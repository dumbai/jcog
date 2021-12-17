package jcog.data.graph.path;///*
// * The MIT License
// *
// * Copyright 2018 user.
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
//
//package jcog.reflect.graph;
//
//import java.util.LinkedHashMap;
//import java.util.LinkedHashSet;
//import java.util.Map;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import jcog.reflect.graph.Path;
//
///**
// * Собирает "полные" пути в графе
// * @param <N> Тип вершины
// * @param <E> Тип ребра
// * @author Kamnev Georgiy
// */
//public class FullPathCollector<N,E> implements GraphIteratorListener<N, E>
//{
//    //<editor-fold defaultstate="collapsed" desc="log Функции">
//    private static final Logger logger = Logger.getLogger(FullPathCollector.class.getName());
//
//    private static Level logLevel(){ return logger.getLevel(); }
//
//    private static boolean isLogSevere(){
//        Level logLevel = logger.getLevel();
//        return logLevel==null ? true : logLevel.intValue() <= Level.SEVERE.intValue();
//    }
//
//    private static boolean isLogWarning(){
//        Level logLevel = logger.getLevel();
//        return logLevel==null  ? true : logLevel.intValue() <= Level.WARNING.intValue();
//    }
//
//    private static boolean isLogInfo(){
//        Level logLevel = logger.getLevel();
//        return logLevel==null  ? true : logLevel.intValue() <= Level.INFO.intValue();
//    }
//
//    private static boolean isLogFine(){
//        Level logLevel = logger.getLevel();
//        return logLevel==null  ? true : logLevel.intValue() <= Level.FINE.intValue();
//    }
//
//    private static boolean isLogFiner(){
//        Level logLevel = logger.getLevel();
//        return logLevel==null  ? true : logLevel.intValue() <= Level.FINER.intValue();
//    }
//
//    private static boolean isLogFinest(){
//        Level logLevel = logger.getLevel();
//        return logLevel==null  ? true : logLevel.intValue() <= Level.FINEST.intValue();
//    }
//
//    private static void logFine(String message,Object ... args){
//        logger.log(Level.FINE, message, args);
//    }
//
//    private static void logFiner(String message,Object ... args){
//        logger.log(Level.FINER, message, args);
//    }
//
//    private static void logFinest(String message,Object ... args){
//        logger.log(Level.FINEST, message, args);
//    }
//
//    private static void logInfo(String message,Object ... args){
//        logger.log(Level.INFO, message, args);
//    }
//
//    private static void logWarning(String message,Object ... args){
//        logger.log(Level.WARNING, message, args);
//    }
//
//    private static void logSevere(String message,Object ... args){
//        logger.log(Level.SEVERE, message, args);
//    }
//
//    private static void logException(Throwable ex){
//        logger.log(Level.SEVERE, null, ex);
//    }
//
//    private static void logEntering(String method,Object ... params){
//        logger.entering(FullPathCollector.class.getName(), method, params);
//    }
//
//    private static void logExiting(String method){
//        logger.exiting(FullPathCollector.class.getName(), method);
//    }
//
//    private static void logExiting(String method, Object result){
//        logger.exiting(FullPathCollector.class.getName(), method, result);
//    }
//    //</editor-fold>
//
//    public FullPathCollector() {
//        this.paths = new LinkedHashMap<>();
//    }
//
//    protected final Map<N,Set<Path<N,E>>> paths;
//
//    public Map<N,Set<Path<N,E>>> getPathsMap(){
//        return paths;
//    }
//
//    public synchronized Set<Path<N,E>> getPathsSet(){
//        LinkedHashSet set = new LinkedHashSet();
//        for( Set<Path<N,E>> s : paths.values() ){
//            if( s!=null ){
//                setAt.addAll(s);
//            }
//        }
//        return setAt;
//    }
//
//    @Override
//    public synchronized void graphIteratorEvent(GraphIteratorEvent<N, E> ev) {
//        if( ev==null )return;
//        if( ev instanceof GraphIteratorEvent.PathFetched ){
//            GraphIteratorEvent.PathFetched<N,E> pfev = (GraphIteratorEvent.PathFetched<N,E>)ev;
//            if( pfev.isTerminal() ){
//
//                Path<N,E> path = pfev.getPath();
//                if( path==null )return;
//                if( path.isEmpty() )return;
//
//                N lastNode = path.node(0);
//                if( lastNode==null )return;
//
//                Set<Path<N,E>> roots = paths.get(lastNode);
//                if( roots==null ){
//                    roots = new LinkedHashSet<>();
//                    paths.put(lastNode, roots);
//                }
//                roots.addAt(path);
//
//                int nc = path.nodeCount();
//                if( nc>1 ){
//                    N secondNode = path.node(1);
//                    if( lastNode!=null && secondNode!=null &&
//                        !lastNode.equals(secondNode) &&
//                        paths.containsKey(secondNode)
//                    ){
//                        paths.remove(secondNode);
//                        //rootNodes.remove(nb);
//                    }
//                }
//            }
//        }
//    }
//}
