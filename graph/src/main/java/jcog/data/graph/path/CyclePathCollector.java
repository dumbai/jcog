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
//import java.util.LinkedHashSet;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * Собирает "цикличные" пути в графе
// * @param <N> Тип вершины
// * @param <E> Тип ребра
// * @author Kamnev Georgiy
// */
//public class CyclePathCollector<N,E> implements GraphIteratorListener<N, E> {
//    //<editor-fold defaultstate="collapsed" desc="log Функции">
//    private static final Logger logger = Logger.getLogger(CyclePathCollector.class.getName());
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
//        logger.entering(CyclePathCollector.class.getName(), method, params);
//    }
//
//    private static void logExiting(String method){
//        logger.exiting(CyclePathCollector.class.getName(), method);
//    }
//
//    private static void logExiting(String method, Object result){
//        logger.exiting(CyclePathCollector.class.getName(), method, result);
//    }
//    //</editor-fold>
//
//    public CyclePathCollector(){
//        paths = new LinkedHashSet<>();
//    }
//
//    protected final Set<Path<N,E>> paths;
//
//    /**
//     * Возвращает пути содержащие циклы
//     * @return пути с циклами
//     */
//    public Set<Path<N,E>> getPaths(){
//        return paths;
//    }
//
//    private boolean existsNode( N n ){
//        if( paths.isEmpty() )return false;
//        for( Path<N,E> p : paths ){
//            if( p.has(n) )return true;
//        }
//        return false;
//    }
//    private boolean exists( Path<N,E> path, boolean checkAllNodes ){
//        if( path==null )throw new IllegalArgumentException("path == null");
//
//        if( paths.isEmpty() )return false;
//        if( checkAllNodes ){
//            if( path.nodeCount()<2 )return false;
//
//            for( int ni=0; ni<path.nodeCount(); ni++ ){
//                N n = path.node(ni);
//                if( !existsNode(n) ){
//                    return false;
//                }
//            }
//
//            return true;
//        }else{
//            if( path.nodeCount()<1 )return false;
//
//            N n = path.node(0);
//            if( existsNode(n) ){
//                return true;
//            }
//
//            return false;
//        }
//    }
//
//    private boolean checkExists = true;
//
//    /**
//     * Проверять что указанынй цикл добавлен
//     * @return true - указанынй цикл добавлен
//     */
//    public synchronized boolean isCheckExists() {
//        return checkExists;
//    }
//
//    /**
//     * Проверять что указанынй цикл добавлен
//     * @param checkExists true - проверять наличие уже существующего цикла
//     */
//    public synchronized void setCheckExists(boolean checkExists) {
//        this.checkExists = checkExists;
//    }
//
//    private boolean checkAllNodes = true;
//
//    /**
//     * Проверять все узлы пути на, то что цикл с указанными узлами уже добавлен
//     * @return true - Проверять все узлы пути; false - проверять первый узел
//     */
//    public synchronized boolean isCheckAllNodes() {
//        return checkAllNodes;
//    }
//
//    /**
//     * Проверять все узлы пути на, то что цикл с указанными узлами уже добавлен
//     * @param checkAllNodes true - Проверять все узлы пути; false - проверять первый узел
//     */
//    public synchronized void setCheckAllNodes(boolean checkAllNodes) {
//        this.checkAllNodes = checkAllNodes;
//    }
//
//    @Override
//    public synchronized void graphIteratorEvent(GraphIteratorEvent<N, E> ev) {
//        if( ev==null )return;
//        if( ev instanceof GraphIteratorEvent.PathFetched ){
//            GraphIteratorEvent.PathFetched<N,E> pfev = (GraphIteratorEvent.PathFetched<N,E>)ev;
//            Path<N,E> path = pfev.getPath();
//            if( path==null )return;
//            if( path.hasCycles() && !path.isEmpty() && path.nodeCount()>1 ){
//                if( checkExists ){
//                    if( !exists(path, checkAllNodes) ){
//                        paths.addAt(path);
//                    }
//                }else{
//                    paths.addAt(path);
//                }
//            }
//        }
//    }
//}
