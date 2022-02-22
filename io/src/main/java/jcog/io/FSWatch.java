//package jcog.io;
//
//import org.eclipse.collections.api.tuple.Pair;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.*;
//import java.util.List;
//import java.util.concurrent.Executor;
//import java.util.function.Consumer;
//
//import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
//import static org.eclipse.collections.impl.tuple.Tuples.pair;
//
//
//@Deprecated public class FSWatch extends Loop {
//
//    static final Logger logger = LoggerFactory.getLogger(FSWatch.class);
//
//    final WatchService watchService;
//    private final WatchKey watchKey;
//    private final Path path;
//    private final Consumer<Pair<Path,WatchEvent.Kind>> onEvent;
//
//
//    public FSWatch(String path, Consumer<Pair<Path,WatchEvent.Kind>> onEvent) throws IOException {
//        this(Paths.get(path), onEvent);
//    }
//
//    public FSWatch(Path path, Executor exe, Consumer<Pair<Path,WatchEvent.Kind>> onEvent) throws IOException {
//        this(path, (t) -> exe.execute(() -> onEvent.accept(t)));
//    }
//
//    public FSWatch(Path p, Consumer<Pair<Path,WatchEvent.Kind>> onEvent) throws IOException {
//
//        watchService = FileSystems.getDefault().newWatchService();
//
//
//        this.path = p.toAbsolutePath();
//
//        watchKey = this.path.register(watchService,
//                StandardWatchEventKinds.ENTRY_CREATE,
//                StandardWatchEventKinds.ENTRY_MODIFY,
//                StandardWatchEventKinds.ENTRY_DELETE
//        );
//
//        this.onEvent = onEvent;
//    }
//
//
//    @Override
//    protected void stopping() {
//        logger.info("stop: {}", path);
//    }
//
//    @Override
//    protected void starting() {
//        File f = path.toFile();
//        if (f.isDirectory()) {
//            for (File e : f.listFiles()) {
//                onEvent.accept(pair(e.toPath(), StandardWatchEventKinds.ENTRY_CREATE));
//            }
//        }
//    }
//
//    @Override
//    public boolean next() {
//        WatchKey key;
//
//        try {
//
//            key = watchService.take();
//        } catch (InterruptedException ex) {
//            ex.printStackTrace();
//            return true;
//        }
//
//        List<WatchEvent<?>> eventList = key.pollEvents();
//
//        for (WatchEvent<?> genericEvent : eventList) {
//
//            WatchEvent.Kind<?> eventKind = genericEvent.kind();
//
//            if (eventKind == OVERFLOW)
//                continue;
//
//            onEvent.accept(
//                pair(this.path.resolve(
//                    (Path)((WatchEvent) genericEvent).context())
//                        .toAbsolutePath(), eventKind)
//            );
//        }
//
//        boolean validKey = key.reset();
//
//
//        return validKey;
//
//    }
//}