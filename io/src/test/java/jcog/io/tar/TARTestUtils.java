package jcog.io.tar;

import java.io.*;
import java.nio.charset.StandardCharsets;

class TARTestUtils {

	private static final int BUFFER = 2048;

	public static File writeStringToFile(String string, File file) throws IOException {
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			writer.write(string);
		}

		return file;
	}

	public static String readFile(File file) throws IOException {
        StringBuilder out = new StringBuilder();
		try (Reader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            char[] buffer = new char[BUFFER];
            return readFromStream(buffer, out, in);
		}
	}

	private static String readFromStream(char[] buffer, StringBuilder out, Reader in) throws IOException {
		while (true) {
			int read = in.read(buffer, 0, BUFFER);
			if (read <= 0) {
				break;
			}
			out.append(buffer, 0, read);
		}
		return out.toString();
	}
}