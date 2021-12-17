# WSHttpServer
A very simple web server that serves static files and integrates with [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket).

## Features:
* Serving static files using multiple routes
* If-Modified-Since
* Range
* Mime types
* Directory index using index.html & index.txt
* Single thread for plain http (such as downloads), which is not handling WebSocket communication 
* Two (by default) threads for WebSockets
* Java-WebSocket (upgrade header)

## Usage:
```java
InetSocketAddress listenAddress = new InetSocketAddress("0.0.0.0", 80); 
ServerSocketChannel ssChannel = HttpServer.openServerChannel(listenAddress);

// MyWebSocketListener should implement HttpWebSocketServerListener
HttpServer server = new HttpServer(ssChannel, new File("/var/www"), new MyWebSocketListener());

// Spawn threads
server.setup();

while (!Thread.interrupted())
{
        // Accept new connections (non blocking)
        // You could run this in your main loop, or as a seperate thread
        server.loop();
         
        // ArrayBlockingQueue.poll(1, TimeUnit.MILLISECONDS); would also work,
        // for example when you have an event loop with worker threads.
        
        // Use a simple sleep in this example
        Thread.sleep(1);
}

// Stop threads
server.stop();
```

## License
This project is released under the MIT license.
