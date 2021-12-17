/**
 * Copyright 2012 Kamran Zafar 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http:
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */

package jcog.io.tar;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;


class JTarAppendTest {
	private static final int BUFFER = 2048;

	private File dir;
	private File outDir;
	private File inDir;

	@BeforeEach
    void setup() throws IOException {
		dir = Files.createTempDirectory("apnd").toFile();
		dir.mkdirs();
		outDir = new File(dir, "out");
		outDir.mkdirs();
		inDir = new File(dir, "in");
		inDir.mkdirs();
	}

	@Test
    void testSingleOperation() throws IOException {
		TarOutputStream tar = new TarOutputStream(new FileOutputStream(new File(dir, "tar.tar")));
		tar.putNextEntry(new TarEntry(TARTestUtils.writeStringToFile("a", new File(inDir, "afile")), "afile"));
		copyFileToStream(new File(inDir, "afile"), tar);
		tar.putNextEntry(new TarEntry(TARTestUtils.writeStringToFile("b", new File(inDir, "bfile")), "bfile"));
		copyFileToStream(new File(inDir, "bfile"), tar);
		tar.putNextEntry(new TarEntry(TARTestUtils.writeStringToFile("c", new File(inDir, "cfile")), "cfile"));
		copyFileToStream(new File(inDir, "cfile"), tar);
		tar.close();

		untar();

		assertInEqualsOut();
	}

	@Test
    void testAppend() throws IOException {
		TarOutputStream tar = new TarOutputStream(new FileOutputStream(new File(dir, "tar.tar")));
		tar.putNextEntry(new TarEntry(TARTestUtils.writeStringToFile("a", new File(inDir, "afile")), "afile"));
		copyFileToStream(new File(inDir, "afile"), tar);
		tar.close();

		tar = new TarOutputStream(new File(dir, "tar.tar"), true);
		tar.putNextEntry(new TarEntry(TARTestUtils.writeStringToFile("b", new File(inDir, "bfile")), "bfile"));
		copyFileToStream(new File(inDir, "bfile"), tar);
		tar.putNextEntry(new TarEntry(TARTestUtils.writeStringToFile("c", new File(inDir, "cfile")), "cfile"));
		copyFileToStream(new File(inDir, "cfile"), tar);
		tar.close();

		untar();

		assertInEqualsOut();
	}

	private static void copyFileToStream(File file, OutputStream out) throws IOException {

        try (FileInputStream in = new FileInputStream(file)) {
            int length = 0;
            byte[] buffer = new byte[BUFFER];
            while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
		}
	}

	/**
	 * Make sure that the contents of the input & output dirs are identical.
	 */
	private void assertInEqualsOut() throws IOException {
		assertEquals(inDir.list().length, outDir.list().length);
		for (File in : inDir.listFiles()) {
			assertEquals(TARTestUtils.readFile(in), TARTestUtils.readFile(new File(outDir, in.getName())));
		}
	}

	private void untar() throws IOException {
		try (TarInputStream in = new TarInputStream(new FileInputStream(new File(dir, "tar.tar")))) {
			TarEntry entry;

			while ((entry = in.getNextEntry()) != null) {
                try (BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(outDir + "/" + entry.getName()))) {
                    byte[] data = new byte[2048];
                    int count;
                    while ((count = in.read(data)) != -1) {
						dest.write(data, 0, count);
					}
				}
			}
		}
	}

}