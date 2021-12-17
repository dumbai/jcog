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
//import java.io.Closeable;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import xyz.cofe.collection.Func2;
//import xyz.cofe.collection.NodesExtracter;
//import xyz.cofe.collection.Pair;
//import xyz.cofe.collection.setAt.SyncEventSet;
//import xyz.cofe.common.ListenersHelper;
//import xyz.cofe.common.Reciver;
//
///**
// * Обход графа
// * @author Kamnev Georgiy
// * @param <N> Тип вершины
// * @param <E> Тип ребра
// */
//public class GraphIterator<N,E> implements Iterator<Path<N,E>> {
//    //<editor-fold defaultstate="collapsed" desc="log Функции">
//    private static final Logger logger = Logger.getLogger(GraphIterator.class.getName());
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
//        logger.entering(GraphIterator.class.getName(), method, params);
//    }
//
//    private static void logExiting(String method){
//        logger.exiting(GraphIterator.class.getName(), method);
//    }
//
//    private static void logExiting(String method, Object result){
//        logger.exiting(GraphIterator.class.getName(), method, result);
//    }
//    //</editor-fold>
//
//    /**
//     * Объект для синхронизации
//     */
//    protected final Object sync;
//
//    /**
//     * Функция извлечения исходящих/следующих верших из указанной вершины
//     */
//    protected final NodesExtracter<N,Pair<N,E>> follow;
//
//    /**
//     * Текущий список рабочих путей
//     */
//    protected final List<Path<N,E>> paths;
//
//    /**
//     * Функция помещаюшая пути в список рабочих
//     */
//    protected final GraphIteratorPusher<N,E> pusher;
//
//    /**
//     * Функция вытаскивания (выбор и удаление) пути из списка рабочих путей
//     */
//    protected final GraphIteratorPoller<N,E> poller;
//
//    /**
//     * Набор посещенных вершин
//     */
//    protected final Set<N> visited;
//
//    /**
//     * Итератор верщин в графе
//     */
//    protected final Iterator<N> startIterator;
//
//    /**
//     * Интерфейс для восстановления рание сохраненного состояния
//     * @param <N> Тип вершины
//     * @param <E> Тип ребра
//     */
//    public interface StoredState<N,E> {
//        /**
//         * Итератор верщин в графе
//         * @return итератор
//         */
//        Iterator<N> getStartIterator();
//
//        /**
//         * Функция извлечения исходящих/следующих верших из указанной вершины
//         * @return функция
//         */
//        NodesExtracter<N,Pair<N,E>> getFollow();
//
//        /**
//         * Текущий список рабочих путей
//         * @return список путей (может быть null)
//         */
//        List<Path<N,E>> getWorkPaths();
//
//        /**
//         * Набор посещенных вершин
//         * @return посещенные вершины (может быть null)
//         */
//        Set<N> getVisited();
//
//        /**
//         * Функция помещаюшая пути в список рабочих
//         * @return функция вставки (может быть null)
//         */
//        GraphIteratorPusher<N,E> getPusher();
//
//        /**
//         * Функция вытаскивания (выбор и удаление) пути из списка рабочих путей
//         * @return функция извлечения (может быть null)
//         */
//        GraphIteratorPoller<N,E> getPoller();
//    }
//
//    //<editor-fold defaultstate="collapsed" desc="Event listeners">
//    /**
//     * Подписки
//     */
//    protected final ListenersHelper<GraphIteratorListener,GraphIteratorEvent<N,E>> listeners;
//
//    //<editor-fold defaultstate="collapsed" desc="hasListener()">
//    /**
//     * Проверяет наличие подписчика на события
//     * @param listener подписчик
//     * @return true - есть подписка
//     */
//    public boolean hasListener(GraphIteratorListener listener) {
//        return listeners.hasListener(listener);
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="getListeners()">
//    /**
//     * Возвращает подписчиков
//     * @return подписчики
//     */
//    public Set<GraphIteratorListener> getListeners() {
//        return listeners.getListeners();
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="addListener()">
//    /**
//     * Добавление подписчика на события
//     * @param listener подписчик
//     * @return отписаться от событий
//     */
//    public Closeable addListener(GraphIteratorListener listener) {
//        return listeners.addListener(listener);
//    }
//
//    /**
//     * Добавление подписчика на события
//     * @param listener подписчик
//     * @param weakLink true - добавить подписчика на weak ссылку
//     * @return отписаться от событий
//     */
//    public Closeable addListener(GraphIteratorListener listener, boolean weakLink) {
//        return listeners.addListener(listener, weakLink);
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="removeListener()">
//    /**
//     * Отписка от событий
//     * @param listener подписчик
//     */
//    public void removeListener(GraphIteratorListener listener) {
//        listeners.removeListener(listener);
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="fireEvent()">
//    /**
//     * Уведомление о событии подписчиков
//     * @param event событие
//     */
//    protected void fireEvent(GraphIteratorEvent<N, E> event) {
//        listeners.fireEvent(event);
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="listen()">
//    /**
//     * Добавляет подписчика на событие определенного типа
//     * @param <EventType> Тип события
//     * @param evType Тип события
//     * @param listener Подписчик
//     * @return Отписка
//     */
//    public <EventType extends GraphIteratorEvent> Closeable listen(
//        final Class<EventType> evType,
//        final Reciver<EventType> listener
//    ){
//        if( evType==null )throw new IllegalArgumentException("evType == null");
//        if( listener==null )throw new IllegalArgumentException("listener == null");
//        return addListener(new GraphIteratorListener() {
//            @Override
//            public void graphIteratorEvent(GraphIteratorEvent event) {
//                if( event==null )return;
//                if( event.getClass().equals(evType) ){
//                    listener.recive((EventType)event);
//                }
//            }
//        });
//    }
//    //</editor-fold>
//
//    /**
//     * Очередь сообщений
//     */
//    protected final ConcurrentLinkedQueue<GraphIteratorEvent<N,E>> eventQueue;
//
//    //<editor-fold defaultstate="collapsed" desc="addEvent()">
//    /**
//     * Добавляет событие в очередь
//     * @param event событие
//     */
//    public void addEvent( GraphIteratorEvent<N,E> event ){
//        if( event!=null && eventQueue!=null ){
//            eventQueue.addAt(event);
//        }
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="sendEvents()">
//    /**
//     * Рассылает события из очереди подписчикам
//     */
//    public void sendEvents(){
//        if( eventQueue!=null ){
//            while( true ){
//                GraphIteratorEvent e = eventQueue.poll();
//                if( e==null )break;
//                fireEvent(e);
//            }
//        }
//    }
//    //</editor-fold>
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="GraphIterator()">
//    /**
//     * Конструктор. <br>
//     * По умолчанию:
//     * <ul>
//     * <li> poller - FirstPoller
//     * <li> pusher - PrependPusher
//     * </ul>
//     *
//     * @param starts Список вершин с которых происходит обход графа
//     * @param follow Функция извлечения исходящих/следующих верших из указанной вершины
//     */
//    public GraphIterator(
//        Iterable<N> starts,
//        NodesExtracter<N,Pair<N,E>> follow
//    ){
//        this(starts,follow,new GraphIteratorPoller.FirstPoller<N, E>(),new GraphIteratorPusher.PrependPusher<N,E>());
//    }
//    /**
//     * Конструктор. <br>
//     * По умолчанию:
//     * <ul>
//     * <li> poller - FirstPoller
//     * </ul>
//     *
//     * @param starts Список вершин с которых происходит обход графа
//     * @param follow Функция извлечения исходящих/следующих верших из указанной вершины
//     * @param pusher Функция добавления списка путей к существующему списку
//     */
//    public GraphIterator(
//        Iterable<N> starts,
//        NodesExtracter<N,Pair<N,E>> follow,
//        GraphIteratorPusher<N,E> pusher
//    ){
//        this(starts,follow,new GraphIteratorPoller.FirstPoller<N, E>(),pusher);
//    }
//
//    /**
//     * Конструктор
//     * @param starts Список вершин с которых происходит обход графа
//     * @param follow Функция извлечения исходящих/следующих верших из указанной вершины
//     * @param poller Функция выборки очередного пути из списка возможных
//     * @param pusher Функция добавления списка путей к существующему списку
//     */
//    public GraphIterator(
//        Iterable<N> starts,
//        NodesExtracter<N,Pair<N,E>> follow,
//        GraphIteratorPoller<N,E> poller,
//        GraphIteratorPusher<N,E> pusher
//    ){
//        if( starts==null )throw new IllegalArgumentException("starts == null");
//        if( follow==null )throw new IllegalArgumentException("follow == null");
//        if( pusher==null )pusher = new GraphIteratorPusher.AppendPusher<>();
//        if( poller==null )poller = new GraphIteratorPoller.FirstPoller<>();
//
//        this.listeners = new ListenersHelper(
//            new Func2<Object, GraphIteratorListener<N,E>, GraphIteratorEvent<N,E>>(){
//                @Override
//                public Object apply(GraphIteratorListener<N, E> ls, GraphIteratorEvent<N, E> ev) {
//                    if( ls!=null ){
//                        //try{
//                            ls.graphIteratorEvent(ev);
//                        //}catch( Thr
//                    }
//                    return null;
//                }
//            }
//        );
//        this.eventQueue = new ConcurrentLinkedQueue<>();
//
//        this.sync = this;
//        this.follow = follow;
//        this.paths = new ArrayList<>();
//        //this.paths.clear();
//        this.poller = poller;
//        this.pusher = pusher;
//        this.visited = new SyncEventSet<>(new LinkedHashSet(), sync);
//        this.startIterator = starts.iterator();
//    }
//
//    /**
//     * Конструктор восстановления
//     * @param sstate ранение сохраненноное состояние
//     * @param sync объект для синронизации графа
//     */
//    public GraphIterator( StoredState<N,E> sstate, Object sync ){
//        if( sstate==null )throw new IllegalArgumentException("sstate == null");
//        this.sync = sync==null ? this : sync;
//        this.listeners = new ListenersHelper(
//            new Func2<Object, GraphIteratorListener<N,E>, GraphIteratorEvent<N,E>>(){
//                @Override
//                public Object apply(GraphIteratorListener<N, E> ls, GraphIteratorEvent<N, E> ev) {
//                    if( ls!=null ){
//                        //try{
//                            ls.graphIteratorEvent(ev);
//                        //}catch( Thr
//                    }
//                    return null;
//                }
//            }
//        );
//        this.eventQueue = new ConcurrentLinkedQueue<>();
//
//        NodesExtracter ne = sstate.getFollow();
//        if( ne==null )throw new IllegalArgumentException("sstate.getFollow() return null");
//        this.follow = ne;
//
//        Iterator siterator = sstate.getStartIterator();
//        if( siterator==null )throw new IllegalArgumentException("sstate.getStartIterator() return null");
//        this.startIterator = siterator;
//
//        List wpaths = sstate.getWorkPaths();
//        if( wpaths==null )wpaths = new ArrayList();
//        this.paths = wpaths;
//
//        GraphIteratorPoller poller = sstate.getPoller();
//        if( poller==null )poller = new GraphIteratorPoller.FirstPoller();
//        this.poller = poller;
//
//        GraphIteratorPusher pusher = sstate.getPusher();
//        if( pusher==null )pusher = new GraphIteratorPusher.AppendPusher();
//        this.pusher = pusher;
//
//        Set visitd = sstate.getVisited();
//        if( visitd==null )visitd = new SyncEventSet<>(new LinkedHashSet(), sync);
//        this.visited = visitd;
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="follow : NodesExtracter<N,Pair<N,E>>">
//    /**
//     * Функция извлечения исходящих/следующих верших из указанной вершины
//     * @return функция извлечения
//     */
//    public NodesExtracter<N,Pair<N,E>> getFollow(){ return follow; }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="pusher : Pusher<N,E>">
//    /**
//     * Функция добавления списка путей к существующему списку
//     * @return функция добавления
//     */
//    public GraphIteratorPusher<N,E> getPusher(){ return pusher; }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="poller : Poller<N,E>">
//    /**
//     * Функция выборки очередного пути из списка возможных
//     * @return функция выборки
//     */
//    public GraphIteratorPoller<N,E> getPoller(){ return poller; }
//    //</editor-fold>
//
//    /**
//     * Возвращает рабочий набор путей
//     * @return Рабочий набор путей
//     */
//    public List<Path<N,E>> getWorkPaths(){ return paths; }
//
//    /**
//     * Возвращает набор посещенных узлов
//     * @return посещенные узлы
//     */
//    public Set<N> getVisited(){ return visited; }
//
//    /**
//     * Возвращает итератор по вершинам с которых начинается обход
//     * @return итератор вершин
//     */
//    public Iterator<N> getStartIterator(){ return startIterator; }
//
//    /**
//     * Выполняет код синхронно (блокируя другие операции)
//     * @param syncCode код
//     */
//    public void sync( Reciver<GraphIterator<N,E>> syncCode ){
//        if( syncCode==null )throw new IllegalArgumentException("syncCode");
//        synchronized( sync ){
//            syncCode.recive(this);
//        }
//    }
//
//    //<editor-fold defaultstate="collapsed" desc="fetch start">
//    private List<Path<N,E>> startPaths( Iterator<N> nodeIterator, Set<N> visited, boolean addVisited ){
//        logFine("start( nodeIterator, visited)");
//        if( nodeIterator==null ){
//            return null;
//        }
//        synchronized( sync ){
//            int nullNodeMax = 500;
//            int nullNodeCnt = 0;
//            while( true ){
//                if( !nodeIterator.hasNext() ){
//                    logFiner("nodeIterator finished");
//                    return null;
//                }
//
//                N startNode = nodeIterator.next();
//                if( startNode==null ){
//                    nullNodeCnt++;
//                    logFiner("nodeIterator return null node #{0} (max: {1})",nullNodeCnt,nullNodeMax);
//                    if( nullNodeCnt>nullNodeMax ){
//                        return null;
//                    }
//                    continue;
//                }
//                nullNodeCnt = 0;
//
//                if( visited!=null && visited.contains(startNode) ){
//                    logFiner("nodeIterator return already visited node");
//                    continue;
//                }
//
//                Iterable<Pair<N,E>> iterPaths = follow.extract(startNode);
//                if( addVisited && visited!=null )visited.addAt(startNode);
//
//                ArrayList<Path<N,E>> result = new ArrayList<>();
//                if( iterPaths!=null ){
//                    for( Pair<N,E> p : iterPaths ){
//                        if( p==null || p.A()==null || p.B()==null )continue;
//                        result.addAt(startPath(startNode, p.A(), p.B()));
//                    }
//                    if( result.isEmpty() ){
//                        result.addAt( startPath(startNode) );
//                    }
//                    return result;
//                }else{
//                    result.addAt( startPath(startNode) );
//                    return result;
//                }
//            }
//        }
//    }
//
//    /**
//     * Извлечение начальных путей из списка вершин
//     * @return пути
//     */
//    protected List<Path<N,E>> fetchStartPaths(){
//        logFine("fetchStart()");
//        synchronized( sync ){
//            if( startIterator==null ){
//                logFinest("startIterator is null");
//                return null;
//            }
//            if( !startIterator.hasNext() ){
//                logFinest("startIterator finished");
//                return null;
//            }
//
//            List<Path<N,E>> starts = startPaths(startIterator, visited, true);
//            if( starts!=null && !starts.isEmpty() ){
//                logFiner( "fetch {0} start paths", starts.size() );
//                //paths.addAll(starts);
//                push(starts);
//            }
//
//            return starts;
//        }
//    }
//
//    /**
//     * Создает начальный путь
//     * @param from начала пути
//     * @param to конец пути
//     * @param e ребро
//     * @return путь
//     */
//    protected Path<N,E> startPath(N from, N to, E e){
//        BasicPath<N,E> p = new BasicPath<>();
//        p = p.start(from).join(to, e);
//        return p;
//    }
//
//    /**
//     * Создает начальный путь
//     * @param from начала пути
//     * @return путь
//     */
//    protected Path<N,E> startPath(N from){
//        BasicPath<N,E> p = new BasicPath<>();
//        p = p.start(from);
//        return p;
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="followPaths()">
//    /**
//     * Создает путь (конкретенация) содержащий указанный и последующие за ним вершину + ребро
//     * @param path путь
//     * @param nextNode вершина
//     * @param nextEdge ребро
//     * @return путь содержащий в конце вершину и ребро
//     */
//    protected Path<N,E> join( Path<N,E> path, N nextNode, E nextEdge ){
//        return path.join(nextNode, nextEdge);
//    }
//
//    /**
//     * Извлечение полследующих путей из указанного
//     * @param path путь
//     * @return  следующие пути
//     */
//    protected List<Path<N,E>> followPaths( Path<N,E> path ){
//        logFine( "followPaths()" );
//        ArrayList<Path<N,E>> followsPaths = new ArrayList<>();
//
//        if( path.nodeCount()<1 ){
//            logFiner("no follows, no nodes");
//            return followsPaths;
//        }
//
//        N from = path.node(-1);
//        if( from==null ){
//            logFiner( "no follows, last node is null" );
//            return followsPaths;
//        }
//
//        Iterable<Pair<N,E>> iterPaths = follow.extract(from);
//        if( iterPaths!=null ){
//            for( Pair<N,E> p : iterPaths ){
//                if( p==null || p.A()==null || p.B()==null )continue;
//                N nextNode = p.A();
//                E nextEdge = p.B();
//
//                Path<N,E> followPath = join(path, nextNode, nextEdge);
//                followsPaths.addAt(followPath);
//            }
//        }else{
//            logFiner( "no follows, last node ({0}) is terminal", from );
//        }
//
//        logFiner( "follow {0} paths from {1}", followsPaths.size(), from );
//        return followsPaths;
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="sendFetchFinish()">
//    protected boolean fetchFinishSended = false;
//
//    /**
//     * Добавляет событие FetchFinish, если оно небыло еще добавлено
//     */
//    protected void sendFetchFinish(){
//        if( fetchFinishSended )return;
//        fetchFinishSended = true;
//        addEvent(new GraphIteratorEvent.FetchFinish<>(this));
//        logFiner( "add FetchFinish event into event queue" );
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="hasNext() : boolean">
//    /**
//     * Проверка наличия очередного пути в графе
//     * @return true - есть очередной путь, false - обход графа завершен
//     */
//    @Override
//    public boolean hasNext(){
//        synchronized(sync){
//            if( paths.isEmpty() ){
//                fetchStartPaths();
//                if( paths.isEmpty() ){
//                    sendFetchFinish();
//                    return false;
//                }
//            }
//            return true;
//        }
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="next()">
//    /**
//     * Получение очередного пути
//     * @return путь или null (конец)
//     */
//    @Override
//    public Path<N, E> next() {
//        Path<N,E> from = null;
//        try{
//            synchronized(sync){
//                // Проверка наличия
//                if( paths.isEmpty() ){
//                    fetchStartPaths();
//                    if( paths.isEmpty() ){
//                        sendFetchFinish();
//                        return null;
//                    }
//                }
//
//                logFine( "next()" );
//
//                // Получение очередного пути
//                from = poll(paths);
//
//                // Извлечение последующих путей
//                boolean isTerminal = false;
//
//                if( from!=null ){
//                    if( !from.hasCycles() ){
//                        N lastNode = from.node(-1);
//                        if( lastNode!=null && visited!=null )visited.addAt(lastNode);
//
//                        List<Path<N,E>> followPaths = followPaths(from);
//                        if( followPaths!=null && !followPaths.isEmpty() ){
//                            push(followPaths);
//                        }else{
//                            isTerminal = true;
//                        }
//                    }else{
//                        logFiner( "skip follows from current(next()) - has cycles" );
//                    }
//                }else{
//                    logFiner( "follow 0 paths from current(next()) = null" );
//                }
//
//                GraphIteratorEvent.PathFetched ev = new GraphIteratorEvent.PathFetched<>(this, from);
//                ev.setTerminal(isTerminal);
//                addEvent(ev);
//            }
//        }catch( Throwable err ){
//            throw err;
//        }finally{
//            sendEvents();
//        }
//        return from;
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="remove()">
//    /**
//     * Пустая функция
//     */
//    @Override
//    public void remove() {
//        logFine( "remove() - is dummy" );
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="poll()">
//    /**
//     * Извлечение пути из списка. Извлеченный путь удаляется из списка.
//     * @param paths список вариантов
//     * @return путь
//     */
//    protected Path<N,E> poll( List<Path<N,E>> paths ){
//        return poller.poll(paths);
//    }
//    //</editor-fold>
//
//    //<editor-fold defaultstate="collapsed" desc="push()">
//    /**
//     * Помещение путей в список
//     * @param pushPaths помещаемые пути
//     */
//    protected void push( List<Path<N,E>> pushPaths ){
//        pusher.push(this.paths, pushPaths);
//    }
//    //</editor-fold>
//}
