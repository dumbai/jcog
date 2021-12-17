













package jcog.grammar.synthesize.util;

import jcog.grammar.synthesize.util.OracleUtils.DiscriminativeOracle;
import jcog.grammar.synthesize.util.OracleUtils.Oracle;

import java.io.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum ShellUtils {
	;

	public static void delete(String filename) {
        new File(filename).delete();
    }

    public static void write(String query, File file) {
        file.delete();
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(query);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException("Error writing seed file!", e);
        }
    }

    public static void write(String query, String filename) {
        write(query, new File(filename));
    }

    public static String read(InputStream input) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String result = br.lines().map(line -> line + '\n').collect(Collectors.joining());
            br.close();
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Error reading program output stream!", e);
        }
    }

    private static Process executeNoWait(String command) {
        try {
            String[] shellCommand = {"/bin/sh", "-c", command};
            return Runtime.getRuntime().exec(shellCommand);
        } catch (Exception e) {
            throw new RuntimeException("Error executing command: " + command, e);
        }
    }

    public static String executeForStream(String command, boolean isError, long timeoutMillis) {
        Process process = executeNoWait(command);
        Callable<String> exec = () -> {
            String result = read(isError ? process.getErrorStream() : process.getInputStream());
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                throw new RuntimeException("Error executing command: " + command, e);
            }
            return result;
        };
        if (timeoutMillis == -1) {
            try {
                return exec.call();
            } catch (Exception e) {
                throw new RuntimeException("Error executing command: " + command, e);
            }
        } else {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(exec);
            executor.shutdown();
            String result;
            try {
                result = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                process.destroy();
                result = "Timeout!";
            }
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
            return result;
        }
    }

    @FunctionalInterface
    public interface CommandFactory {
        String getCommand(String filename, String auxFilename, String exePath);
    }

    public static class SimpleCommandFactory implements CommandFactory {
        @Override
        public String getCommand(String filename, String auxFilename, String exePath) {
            return exePath + ' ' + filename;
        }
    }

    public static class ShellOracle implements Oracle {
        private final String command;
        private final String filename;
        private final String auxFilename;
        private final boolean isError;
        private final long timeoutMillis;

        public ShellOracle(String filename, String auxFilename, String command, boolean isError, long timeoutMillis) {
            this.filename = filename;
            this.auxFilename = auxFilename;
            this.command = command;
            this.isError = isError;
            this.timeoutMillis = timeoutMillis;
        }

        @Override
        public String apply(String query) {
            write("", this.auxFilename);
            write(query, this.filename);
            String result = ShellUtils.executeForStream(this.command, this.isError, this.timeoutMillis);
            delete(this.auxFilename);
            delete(this.filename);
            return result;
        }
    }

    public static class ExecuteDiscriminativeOracle implements DiscriminativeOracle {
        private static final Pattern SPACE = Pattern.compile("\\s*");
        private final Oracle oracle;

        public ExecuteDiscriminativeOracle(Oracle oracle) {
            this.oracle = oracle;
        }

        @Override
        public boolean test(String query) {
            return SPACE.matcher(this.oracle.apply(query)).matches();
        }
    }
}
