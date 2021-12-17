package jcog.markov;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

class MarkovSentenceTest {

	public static void main(String[] args) throws IOException {
		MarkovSentence m = new MarkovSentence();
		m.learnTokenize(6, Files.toString(new File("/tmp/t.txt"), Charset.defaultCharset()));
		for (int i= 0; i < 100; i++) {
			String y = m.generateSentence(400);
			System.out.println(y);
			System.out.println();
		}
	}
}