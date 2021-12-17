package jcog.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by me on 5/22/16.
 */
public class Shell {

    static final Executor exe = Executors.newSingleThreadExecutor();
    private static final Logger logger = LoggerFactory.getLogger(Shell.class);
    public final Process proc;
    public final PrintWriter writer;
    private final StreamGobbler reader;
    private final Thread rThread;
    private long lastOutput;

    public Shell(String... cmd) throws IOException {
        this.proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();

        this.writer = new PrintWriter(new OutputStreamWriter(this.proc.getOutputStream(), StandardCharsets.UTF_8), true);


        this.reader = new StreamGobbler(proc.getInputStream(), s -> {
            if (!s.isEmpty())
                exe.execute(() -> readln(s));
        });
        rThread = new Thread(reader, "read");
        rThread.start();

        logger.info("started"/*, proc.getPid()*/);

    }

    protected void readln(String line) {
        logger.info("OUT: {}", line);
        lastOutput = System.currentTimeMillis();
    }

    public void println(String line) {

        logger.info(" IN: {}", line);

        writer.println(line);
        writer.flush();

    }

    public void stop() {

        if (writer != null) {
            writer.close();

            if (proc != null) {
                proc.destroy();
            }

            rThread.stop();
        }

    }


    static class StreamGobbler implements Runnable {
        private final Consumer<String> eachLine;
        InputStream is;

        private StreamGobbler(InputStream is, Consumer<String> eachLine) {
            this.is = is;
            this.eachLine = eachLine;
        }

        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = br.readLine()) != null) {
                    eachLine.accept(line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}