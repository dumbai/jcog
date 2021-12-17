//package jcog.net.http;
//
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.net.Socket;
//import java.util.*;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.function.Supplier;
//
///**
// * @author GroG
// * https:
// * <p>
// * TODO not yet fully integrated into this package
// *
// * <p>
// * mjpeg server - allows multiple jpeg streams to be sent to multiple
// * clients extends the most excellent NanoHTTPD server - multi-part mime
// * was done with little parts borg'd in from -
// * http:
// * http:
// */
//public class MjpegServer /*extends NanoHTTPD*/ {
//
//
//    private final Map<String, BlockingQueue<Supplier<byte[]>>> videoFeeds = new ConcurrentHashMap<>();
//    private final Map<String, VideoWebClient> clients = new ConcurrentHashMap<>();
//
//    private MjpegServer() {
//        super();
//    }
//
//    public static void main(String[] args) {
//
//        MjpegServer server = new MjpegServer();
//
//
//
//    }
//
//    public HttpResponse serve(String uri, String method, Properties header, Properties parms, Socket socket) {
//
//
//        Enumeration e = header.propertyNames();
//        while (e.hasMoreElements()) {
//            String value = (String) e.nextElement();
//
//        }
//        e = parms.propertyNames();
//        while (e.hasMoreElements()) {
//            String value = (String) e.nextElement();
//
//        }
//
//        String feed = null;
//
//
//
//
//
//
//
//        int pos0 = uri.lastIndexOf('/');
//        if (pos0 != -1) {
//            feed = uri.substring(pos0 + 1);
//        }
//
//        if (!videoFeeds.containsKey(feed)) {
//            StringBuilder response = new StringBuilder(String.format("<html><body align=center>video feeds<br/>", feed));
//            for (Map.Entry<String, BlockingQueue<Supplier<byte[]>>> o : videoFeeds.entrySet()) {
//
//
//
//                response.append(String.format("<img src=\"%s\" /><br/>%s<br/>", o.getKey(), o.getKey()));
//
//            }
//            if (videoFeeds.isEmpty()) {
//                response.append("no video feed exist - try attaching a VideoSource to the VideoStreamer");
//            }
//            response.append("</body></html>");
//            Map<String, String> headers = new HashMap();
//            return new HttpResponse(HttpUtil.METHOD.GET, 200, response.toString(), false, null);
//        } else {
//            try {
//                VideoWebClient client = new VideoWebClient(videoFeeds.get(feed), feed, socket);
//                client.start();
//                clients.put(feed, client);
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//        }
//
//
//
//
//        return null;
//    }
//
//    static class Connection {
//        boolean initialized = false;
//        Socket socket;
//        OutputStream os;
//
//        Connection(Socket socket) throws IOException {
//            this.socket = socket;
//            os = socket.getOutputStream();
//        }
//
//        void close() {
//            try {
//                socket.close();
//                socket = null;
//            } catch (IOException e) {
//            }
//        }
//    }
//
//    static class VideoWebClient extends Thread {
//        final ArrayList<Connection> connections = new ArrayList<>();
//        final String feed;
//        final BlockingQueue<Supplier<byte[]>> videoFeed;
//
//        VideoWebClient(BlockingQueue<Supplier<byte[]>> videoFeed, String feed, Socket socket) throws IOException {
//
//
//            super(String.format("stream_%s", feed));
//            this.videoFeed = videoFeed;
//            this.feed = feed;
//            connections.add(new Connection(socket));
//        }
//
//
//        @Override
//        public void run() {
//            try {
//                while (true) {
//                    Supplier<byte[]> frame = videoFeed.take();
//
//
//
//                    for (Iterator<Connection> iterator = connections.iterator(); iterator.hasNext(); ) {
//                        Connection c = iterator.next();
//
//                        try {
//
//                            if (!c.initialized) {
//                                c.os.write(
//                                        ("HTTP/1.0 200 OK\r\n" + "Server: YourServerName\r\n" + "Connection: close\r\n" + "Max-Age: 0\r\n" + "Expires: 0\r\n" + "Cache-Control: no-cache, private\r\n"
//                                                + "Pragma: no-cache\r\n" + "Content-Type: multipart/x-mixed-replace; " + "boundary=--BoundaryString\r\n\r\n").getBytes());
//                                c.initialized = true;
//                            }
//
//                            byte[] bytes = frame.get();
//
//
//                            c.os.write(("--BoundaryString\r\n" + "Content-type: image/jpg\r\n" + "Content-Length: " + bytes.length + "\r\n\r\n").getBytes());
//
//                            c.os.write(bytes);
//
//
//                            c.os.write("\r\n\r\n".getBytes());
//
//
//                            c.os.flush();
//
//                        } catch (Exception e) {
//
//
//                            iterator.remove();
//                            c.close();
//                        }
//
//                    }
//                }
//            } catch (Exception e) {
//
//                e.printStackTrace();
//
//            }
//
//        }
//    }
//
//}
