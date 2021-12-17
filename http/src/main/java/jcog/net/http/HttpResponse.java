package jcog.net.http;

import jcog.net.http.HttpUtil.METHOD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import java.util.regex.Matcher;

/**
 * @author Joris
 */
class HttpResponse {
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);
    final boolean close;
    private final METHOD requestMethod;
    private int status;
    private String statusMessage;
    private ByteBuffer headers;

    private boolean sendStatusAsContent = true;
    private File file;
    private RandomAccessFile raf;
    private long fileBytesSent = 0;
    private ByteBuffer fileBuffer;
    private boolean range = false;
    private long rangeStart = 0;
    private long rangeEnd = 0;
    private long rangeLength = 0;

    

    HttpResponse(METHOD requestMethod, int status, String statusMessage, boolean close, File file) {
        this.requestMethod = requestMethod;
        this.status = status;
        this.statusMessage = statusMessage;
        this.close = close;
        this.file = file;
    }

    public void prepare(HttpConnection con) {
        assert this.headers == null;

        StringBuilder headerString = new StringBuilder();

        boolean sendFile = false;

        if (file != null) {
            if (file.isDirectory()) {
                file = HttpUtil.findDirectoryIndex(file);
                if (file != null) {
                    sendFile = true;
                }
            } else if (file.isFile()) {
                sendFile = true;
            } else {
                if (status == 200) {
                    status = 404;
                    statusMessage = "File Not Found";
                }
            }
        }

        Date lastModified = null;

        if (sendFile) {
            lastModified = new Date(file.lastModified());

            String ifModifiedSince = con.request.get("if-modified-since");
            if (ifModifiedSince != null) {
                try {
                    Date ifModifiedSinceDate = HttpUtil.HttpDateUtils.parseDate(ifModifiedSince);

                    if (lastModified.after(ifModifiedSinceDate)) {
                        sendFile = false;

                        status = 304;
                        statusMessage = "Not Modified";
                        sendStatusAsContent = false;
                    }
                } catch (HttpUtil.HttpDateUtils.DateParseException ex) {
                    logger.error("{}", ex);
                }
            }
        }

        long fileLength = 0;
        if (sendFile) {
            sendFile = false;

            try {
                raf = new RandomAccessFile(file, "r");
                fileLength = raf.length();
                sendFile = true;
            } catch (FileNotFoundException ex) {
                try {
                    raf.close();
                } catch (IOException ex2) {
                    logger.error("raf close", ex);
                }

                raf = null;
                if (status == 200) {
                    status = 404;
                    statusMessage = "File Not Found";
                }
                logger.info("File not found", ex);
            } catch (IOException ex) {
                try {
                    raf.close();
                } catch (IOException ex2) {
                    logger.error("raf close", ex);
                }

                raf = null;
                if (status == 200) {
                    status = 404;
                    statusMessage = "Error reading file";
                }
                logger.warn("Error reading file: {}", ex);
            }

            String rangeValue = con.request.get("range");


            if (sendFile && rangeValue != null) {
                
                Matcher rangeMatcher = HttpUtil.simpleRange.matcher(rangeValue);
                if (rangeMatcher.matches()) {
                    range = true;
                    try {
                        String start = rangeMatcher.group(1);
                        String end = rangeMatcher.group(2);

                        if (start != null && start.isEmpty()) {
                            start = null;
                        }
                        if (end != null && end.isEmpty()) {
                            end = null;
                        }


                        if (start == null && end == null) {
                            throw new NumberFormatException(); 
                        }

                        
                        if (start == null) {
                            rangeStart = fileLength - Long.parseLong(end, 10);
                            rangeEnd = fileLength - 1;
                        }
                        
                        else if (end == null) {
                            rangeStart = Long.parseLong(start, 10);
                            rangeEnd = fileLength - 1;
                        } else {
                            rangeStart = Long.parseLong(start, 10);
                            rangeEnd = Long.parseLong(end, 10);
                        }

                        if (rangeEnd > fileLength) {
                            rangeEnd = fileLength - 1;
                        }

                        if (rangeEnd < rangeStart) {
                            range = false;
                        }

                        rangeLength = rangeEnd - rangeStart + 1;

                        raf.seek(rangeStart);
                    } catch (NumberFormatException | IOException ex) {
                        logger.warn("{}", ex);
                        range = false;
                    }
                }
            }

            if (range) {
                status = 206;
            }
        }

        
        headerString.append("HTTP/1.1 ");
        headerString.append(status);
        headerString.append(' ');
        if (!sendStatusAsContent) {
            headerString.append(statusMessage);
        }
        headerString.append("\r\n");

        if (close) {
            headerString.append("Connection: close\r\n");
        }

        if (status == 405) {
            headerString.append("Allow: GET, HEAD\r\n");
        }

        headerString.append("Server: Client\r\n");
        headerString.append("X-Frame-Options: SAMEORIGIN\r\n");
        headerString.append("Date: ");
        headerString.append(HttpUtil.HttpDateUtils.formatDate(new Date()));
        headerString.append("\r\n");

        if (!sendFile) {
            headerString.append("Content-Type: ");
            String contentType = statusMessage.startsWith("<html") ? "text/html" : "text/plain"; 
            headerString.append(contentType);
            headerString.append(" charset=UTF-8\r\n");

            if (sendStatusAsContent) {
                headerString.append("Content-Length: ");
                headerString.append(HttpUtil.binarySizeUTF8(statusMessage));
                headerString.append("\r\n");
            }

            headerString.append("\r\n"); 

            if (requestMethod != METHOD.HEAD) {
                if (sendStatusAsContent) {
                    headerString.append(statusMessage);
                }
            }
        } else {


            if (range) {
                headerString.append("Content-Range: bytes ");
                headerString.append(rangeStart);
                headerString.append('-');
                headerString.append(rangeEnd);
                headerString.append('/');
                headerString.append(fileLength);
                headerString.append("\r\n");
                headerString.append("Content-Length: ");
                headerString.append(rangeEnd - rangeStart + 1);
                headerString.append("\r\n");
            } else {
                headerString.append("Content-Length: ");
                headerString.append(fileLength);
                headerString.append("\r\n");
            }

            headerString.append("Last-Modified: ");
            headerString.append(HttpUtil.HttpDateUtils.formatDate(lastModified));
            headerString.append("\r\n");

            headerString.append("Content-Type: ");
            headerString.append(HttpMime.getMime(file));
            headerString.append("\r\n");

            headerString.append("Accept-Ranges: bytes\r\n");
            headerString.append("\r\n");
        }

        
        this.headers = ByteBuffer.wrap(headerString.toString().getBytes(HttpUtil.UTF8));
    }

    /**
     * Attempt to write some http resonse stuff on a socket channel.
     *
     * @return true if there is nothing more to write
     */
    public boolean write(WritableByteChannel channel) throws IOException {
        if (headers != null) {
            if (headers.hasRemaining() && channel.write(headers) < 0) {
                throw new IOException("closed");
            }

            if (headers.hasRemaining()) 
            {
                return false;
            } else {
                headers = null;
            }
        }

        if (raf != null) {
            if (fileBuffer == null) {
                fileBuffer = ByteBuffer.wrap(new byte[1024]);
                fileBuffer.position(fileBuffer.limit()); 
            }

            while (true) {
                if (!fileBuffer.hasRemaining()) {
                    int read = raf.read(fileBuffer.array());
                    if (read == -1) {
                        raf.close();
                        return true;
                    }
                    fileBuffer.position(0);
                    fileBuffer.limit(read);
                }

                if (fileBuffer.hasRemaining()) {
                    if (range) {
                        long limit = rangeLength - fileBytesSent;
                        if (fileBuffer.limit() > limit) {
                            fileBuffer.limit((int) limit);
                        }
                    }

                    fileBytesSent += channel.write(fileBuffer);

                    if (range && fileBytesSent >= rangeLength) {
                        raf.close();
                        return true; 
                    }

                    if (fileBuffer.hasRemaining()) 
                    {
                        return false;
                    }
                }
            }
        }

        return true; 
    }
}
