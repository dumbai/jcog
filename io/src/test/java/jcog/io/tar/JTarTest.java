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

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JTarTest {
	private static final int BUFFER = 2048;

	private File dir;
	static final File tartest;
    static final File tartestGZ;

	static {
		try {
			tartest = new File(Resources.getResource("tartest.tar").toURI());
			tartestGZ = new File(Resources.getResource("tartest.tar.gz").toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}


	@BeforeEach
    void setup() throws IOException {
		dir = Files.createTempDirectory("tartest").toFile();
		dir.mkdirs();
	}

	/**
	 * Tar the given folder
	 * 
	 * @throws IOException
	 */
	@Test
    void tar() throws IOException {
		FileOutputStream dest = new FileOutputStream(dir.getAbsolutePath() + "/tartest.tar");
		TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));

		File tartest = new File(dir.getAbsolutePath(), "tartest");
		tartest.mkdirs();

		TARTestUtils.writeStringToFile("HPeX2kD5kSTc7pzCDX", new File(tartest, "one"));
		TARTestUtils.writeStringToFile("gTzyuQjfhrnyX9cTBSy", new File(tartest, "two"));
		TARTestUtils.writeStringToFile("KG889vdgjPHQXUEXCqrr", new File(tartest, "three"));
		TARTestUtils.writeStringToFile("CNBDGjEJNYfms7rwxfkAJ", new File(tartest, "four"));
		TARTestUtils.writeStringToFile("tT6mFKuLRjPmUDjcVTnjBL", new File(tartest, "five"));
		TARTestUtils.writeStringToFile("jrPYpzLfWB5vZTRsSKqFvVj", new File(tartest, "six"));

		tarFolder(null, dir.getAbsolutePath() + "/tartest/", out);

		out.close();

		assertEquals(TarUtils.calculateTarSize(new File(dir.getAbsolutePath() + "/tartest")), new File(dir.getAbsolutePath() + "/tartest.tar").length());
	}

	/**
	 * Untar the tar file
	 * 
	 * @throws IOException
	 */
	@Test
    void untarTarFile() throws IOException {
		File destFolder = new File(dir, "untartest");
		destFolder.mkdirs();

		TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(tartest)));
		untar(tis, destFolder.getAbsolutePath());

		tis.close();

		assertFileContents(destFolder);
	}

	/**
	 * Untar the tar file
	 * 
	 * @throws IOException
	 */
	@Test
    void untarTarFileDefaultSkip() throws IOException {
		File destFolder = new File(dir, "untartest/skip");
		destFolder.mkdirs();

		TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(tartest)));
		tis.setDefaultSkip(true);
		untar(tis, destFolder.getAbsolutePath());

		tis.close();

		assertFileContents(destFolder);

	}

	/**
	 * Untar the gzipped-tar file
	 * 
	 * @throws IOException
	 */
	@Test
    void untarTGzFile() throws IOException {
		File destFolder = new File(dir, "untargztest");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(tartestGZ))));

		untar(tis, destFolder.getAbsolutePath());

		tis.close();

		assertFileContents(destFolder);
	}


	@Test
    void testOffset() throws IOException {
		File destFolder = new File(dir, "untartest");
		destFolder.mkdirs();

		TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(tartest)));
		tis.getNextEntry();
		assertEquals(TarConstants.HEADER_BLOCK, tis.getCurrentOffset());
		tis.getNextEntry();
		TarEntry entry = tis.getNextEntry(); 
		
		assertEquals(TarConstants.HEADER_BLOCK * 3 + TarConstants.DATA_BLOCK * 2, tis.getCurrentOffset());
		tis.close();
		
		RandomAccessFile rif = new RandomAccessFile(tartest, "r");
		rif.seek(TarConstants.HEADER_BLOCK * 3 + TarConstants.DATA_BLOCK * 2);
		byte[] data = new byte[(int)entry.getSize()];
		rif.read(data);
		assertEquals("gTzyuQjfhrnyX9cTBSy", new String(data, StandardCharsets.UTF_8));
		rif.close();
	}
	
	private static void untar(TarInputStream tis, String destFolder) throws IOException {
		BufferedOutputStream dest = null;

		TarEntry entry;
		while ((entry = tis.getNextEntry()) != null) {
			System.out.println("Extracting: " + entry.getName());

			if (entry.isDirectory()) {
				new File(destFolder + '/' + entry.getName()).mkdirs();
				continue;
			} else {
				int di = entry.getName().lastIndexOf('/');
				if (di != -1) {
					new File(destFolder + '/' + entry.getName().substring(0, di)).mkdirs();
				}
			}

			FileOutputStream fos = new FileOutputStream(destFolder + '/' + entry.getName());
			dest = new BufferedOutputStream(fos);

			byte[] data = new byte[BUFFER];
			int count;
			while ((count = tis.read(data)) != -1) {
				dest.write(data, 0, count);
			}

			dest.flush();
			dest.close();
		}
	}

	private static void tarFolder(String parent, String path, TarOutputStream out) throws IOException {
		File f = new File(path);
		String[] files = f.list();

		
		if (files == null) {
			files = new String[1];
			files[0] = f.getName();
		}

		parent = ((parent == null) ? (f.isFile()) ? "" : f.getName() + '/' : parent + f.getName() + '/');

		BufferedInputStream origin = null;
		for (int i = 0; i < files.length; i++) {
			System.out.println("Adding: " + files[i]);
			File fe = f;

			if (f.isDirectory()) {
				fe = new File(f, files[i]);
			}

			if (fe.isDirectory()) {
				String[] fl = fe.list();
				if (fl != null && fl.length != 0) {
					tarFolder(parent, fe.getPath(), out);
				} else {
					TarEntry entry = new TarEntry(fe, parent + files[i] + '/');
					out.putNextEntry(entry);
				}
				continue;
			}

			FileInputStream fi = new FileInputStream(fe);
			origin = new BufferedInputStream(fi);
			TarEntry entry = new TarEntry(fe, parent + files[i]);
			out.putNextEntry(entry);

			int count;

			byte[] data = new byte[BUFFER];
			while ((count = origin.read(data)) != -1) {
				out.write(data, 0, count);
			}

			out.flush();

			origin.close();
		}
	}

	@Test
    void fileEntry() {
		String fileName = "file.txt";
		long fileSize = 14523;
		long modTime = System.currentTimeMillis() / 1000;
		int permissions = 0755;

		
		TarHeader fileHeader = TarHeader.createHeader(fileName, fileSize, modTime, false, permissions);
		assertEquals(fileName, fileHeader.name.toString());
		assertEquals(TarHeader.LF_NORMAL, fileHeader.linkFlag);
		assertEquals(fileSize, fileHeader.size);
		assertEquals(modTime, fileHeader.modTime);
		assertEquals(permissions, fileHeader.mode);

		
		TarEntry fileEntry = new TarEntry(fileHeader);
		assertEquals(fileName, fileEntry.getName());

		
		byte[] headerBuf = new byte[TarConstants.HEADER_BLOCK];
		fileEntry.writeEntryHeader(headerBuf);
		TarEntry createdEntry = new TarEntry(headerBuf);
        assertEquals(fileEntry, createdEntry);
	}

	private static void assertFileContents(File destFolder) throws IOException {
		assertEquals("HPeX2kD5kSTc7pzCDX", TARTestUtils.readFile(new File(destFolder, "tartest/one")));
		assertEquals("gTzyuQjfhrnyX9cTBSy", TARTestUtils.readFile(new File(destFolder, "tartest/two")));
		assertEquals("KG889vdgjPHQXUEXCqrr", TARTestUtils.readFile(new File(destFolder, "tartest/three")));
		assertEquals("CNBDGjEJNYfms7rwxfkAJ", TARTestUtils.readFile(new File(destFolder, "tartest/four")));
		assertEquals("tT6mFKuLRjPmUDjcVTnjBL", TARTestUtils.readFile(new File(destFolder, "tartest/five")));
		assertEquals("jrPYpzLfWB5vZTRsSKqFvVj", TARTestUtils.readFile(new File(destFolder, "tartest/six")));
	}
}