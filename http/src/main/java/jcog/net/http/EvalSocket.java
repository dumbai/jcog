package jcog.net.http;

import jcog.data.list.Lst;
import jcog.data.map.CustomConcurrentHashMap;
import jcog.exe.Exe;
import jcog.io.Serials;
import org.codehaus.janino.ExpressionEvaluator;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.jctools.queues.MpscArrayQueue;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static jcog.data.map.CustomConcurrentHashMap.*;


/**
 * exposes an interface thru java evaluation to HTTP/WebSockets
 * --addAt-opens=java.base/jdk.internal.misc=ALL-UNNAMED
 */
public class EvalSocket<X> implements HttpModel {



//    private static final Logger logger = LoggerFactory.getLogger(JSSocket.class);

    private final CustomConcurrentHashMap<WebSocket, EvalSession<X>> session =
            new CustomConcurrentHashMap<>(
                    WEAK, EQUALS, STRONG, IDENTITY,
                    1024
            );

    private final Supplier<X> target;

//    /**
//     * swagger-like API guide for the interface
//     */
//    private final String manual;

    public EvalSocket(Supplier<X> target) {
        this.target = target;
//        this.manual = manual(target.get()).toString();
    }

    private static <X> StringBuilder manual(X x) {
        List<Method> m = new Lst();
        Collections.addAll(m, x.getClass().getMethods());

        StringBuilder s = new StringBuilder(1024);
        for (Method y : m) {
            if (y.getDeclaringClass() == Object.class)
                continue;

            s.append(y.getName()).append(" (");
            Parameter[] parameters = y.getParameters();
            for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
                Parameter p = parameters[i];
                s.append(p.getType()).append(' ').append(p.getName());
                if (i < parametersLength - 1)
                    s.append(", ");
            }
            s.append(")\n");

        }

        return s;
    }

    static final String[] param = {"x"};

    private static Object eval(String code, Object context) {
        Object o;

        try {
//            if (bindings == null)
//                o = engine.eval(code);
//            else
//                o = engine.eval(code, bindings);

            ExpressionEvaluator ee = new ExpressionEvaluator();

            ee.setParameters(param, new Class[] { context.getClass() });
            ee.setExpressionType(Object.class);
            ee.cook(code);
            o = ee.evaluate(/*new Object[] {*/ context /*}*/);


//            o = ExpressionEvaluator.createFastExpressionEvaluator(
//                code,                    // expression to evaluate
//                Object.class,                  // interface that describes the expression's signature
//                params,
//                (ClassLoader) null          // Use current thread's context class loader
//            );

        } catch (Exception t) {
            o = t;
        }
        return o;
    }

    @Override
    public void response(HttpConnection h) {
        //h.respond(manual);
        h.respond(target.toString());
    }

    @Override
    public void wssOpen(WebSocket ws, ClientHandshake handshake) {
        onOpened(session.computeIfAbsent(ws, s -> new EvalSession<>(s, target.get())));
    }

    @Override
    public void wssClose(WebSocket ws, int code, String reason, boolean remote) {
        onClosed(session.remove(ws));
    }

    @Override
    public void wssMessage(WebSocket ws, String _message) {
        String message = _message.trim();
        if (message.isEmpty())
            return;

        session.get(ws).invoke(message);

    }

    private void onOpened(EvalSession<X> session) {

    }

    private void onClosed(EvalSession<X> session) {

    }

    private static class EvalSession<X> implements Runnable {

        private final X context;
        private final WebSocket socket;

        static final int MAX_QUEUE_SIZE = 64;


        final AtomicBoolean pending = new AtomicBoolean(false);
        final MpscArrayQueue<String> q = new MpscArrayQueue<>(MAX_QUEUE_SIZE);

        EvalSession(WebSocket s, X context) {
            this.socket = s;
            this.context = context;
//            this.bindings = JS.getBindings("js");
//            bindings.putMember("i", context);
        }

        void invoke(String expr) {
            if (socket.isClosed())
                return;

            if (!q.offer(expr))
                socket.close(CloseFrame.TOOBIG, "Overflow");

            if (pending.compareAndSet(false, true)) {
                Exe.runLater(this);
            }
        }

        @Override
        public void run() {

            if (socket.isClosed())
                return;

            pending.set(false);

            q.drain(message -> {
                try {

                    Object x = eval(/*"i." +*/ message, context);
                    if (x == null || socket.isClosed())
                        return;

//                    if (x instanceof ScriptException)
//                        x = ((Throwable) x).getMessage();

                    if (x instanceof String)
                        socket.send((String) x);
                    else {
                        try {
                            socket.send(Serials.jsonMapper.writeValueAsString(x));
                        } catch (Exception serialization) {
                            socket.send(x.toString());
                        }
                    }

                } catch (Exception e) {
                    socket.send(e.getMessage());
                }

            });

        }
    }

}