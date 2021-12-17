package jcog.net.http;

import jcog.Util;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpWebSocketServerTest {

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        HttpServer server = new HttpServer("localhost", 8080, new HttpModel() {

            @Override
            public void wssOpen(WebSocket ws, ClientHandshake handshake) {
                ws.send("hi");
            }

            @Override
            public void response(HttpConnection h) {
                h.respond("");
            }
        });


        server.fps(20f);

        //test http client connect
        URL u = new URL("http://localhost:8080/");
        URLConnection urlConnection = u.openConnection();
        InputStream content = (InputStream) urlConnection.getContent();

        String x = new String(content.readAllBytes());
        assertEquals("", x);
        

        
        WebSocketTest.Client c = new WebSocketTest.Client(8080);

        c.connectBlocking();

        Util.sleepMS(500);

        assertTrue(c.isOpen());

        c.send("abc");

        Util.sleepMS(500);

        c.closeBlocking();
        


        server.stop();


    }



//    static class JSTest {
//
//        static class MyInterface {
//
//            private final Map<String,Object> map = new HashMap();
//
//            public int put(String x, Object y) {
//                map.put(x, y);
//                return map.size();
//            }
//
//            public Object get(String x) {
//                return map.get(x);
//            }
//        }
//
//        public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
//            HttpServer server = new HttpServer("localhost", 8080, new JSSocket<>(MyInterface::new));
//
//            server.setFPS(20f);
//
//            //test http client connect
//            URL u = new URL("http://localhost:8080/");
//            URLConnection urlConnection = u.openConnection();
//            InputStream content = (InputStream) urlConnection.getContent();
//
//            String x = new String(content.readAllBytes());
//            System.out.println(x);
//
//
//
//            WebSocketTest.Client c = new WebSocketTest.Client();
//
//            c.connectBlocking();
//
//            Util.sleepMS(500);
//
//            assertTrue(c.isOpen());
//
//            c.send("put(\"x\", 1234)");
//            c.send("get(\"x\")");
//
//            Util.sleepMS(500);
//
//            c.closeBlocking();
//
//
//
//            server.stop();
//
//
//        }
//    }
}