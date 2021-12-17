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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

/**
 * @author Kamran Zafar
 * 
 */
public class TarEntry {
	private File file;
	private TarHeader header;

	private TarEntry() {
		this.file = null;
		header = new TarHeader();
	}

	public TarEntry(File file, String entryName) {
		this();
		this.file = file;
		header = TarHeader.createHeader(
				entryName, this.file.length(), this.file.lastModified() / 1000, this.file.isDirectory(),
				PermissionUtils.permissions(this.file));
	}

	public TarEntry(byte[] headerBuf) {
		this();
		this.parseTarHeader(headerBuf);
	}

	/**
	 * Constructor to create an entry from an existing TarHeader object.
	 * 
	 * This method is useful to add new entries programmatically (e.g. for
	 * adding files or directories that do not exist in the file system).
	 */
	public TarEntry(TarHeader header) {
		this.file = null;
		this.header = header;
	}

	@Override
	public boolean equals(Object it) {
		if (!(it instanceof TarEntry)) {
			return false;
		}
		return header.name.toString().equals(
				((TarEntry) it).header.name.toString());
	}

	@Override
	public int hashCode() {
		return header.name.hashCode();
	}

	public boolean isDescendent(TarEntry desc) {
		return desc.header.name.toString().startsWith(header.name.toString());
	}

	public TarHeader getHeader() {
		return header;
	}

	public String getName() {
		String name = header.name.toString();
		if (header.namePrefix != null && !header.namePrefix.toString().isEmpty()) {
			name = header.namePrefix + "/" + name;
		}

		return name;
	}

	public void setName(String name) {
		header.name = new StringBuilder(name);
	}

	public int getUserId() {
		return header.userId;
	}

	private void setUserId(int userId) {
		header.userId = userId;
	}

	public int getGroupId() {
		return header.groupId;
	}

	private void setGroupId(int groupId) {
		header.groupId = groupId;
	}

	public String getUserName() {
		return header.userName.toString();
	}

	public void setUserName(String userName) {
		header.userName = new StringBuilder(userName);
	}

	public String getGroupName() {
		return header.groupName.toString();
	}

	public void setGroupName(String groupName) {
		header.groupName = new StringBuilder(groupName);
	}

	public void setIds(int userId, int groupId) {
		this.setUserId(userId);
		this.setGroupId(groupId);
	}

	public void setModTime(long time) {
		header.modTime = time / 1000;
	}

	public void setModTime(Date time) {
		header.modTime = time.getTime() / 1000;
	}

	public Date getModTime() {
		return new Date(header.modTime * 1000);
	}

	public File getFile() {
		return this.file;
	}

	public long getSize() {
		return header.size;
	}

	public void setSize(long size) {
		header.size = size;
	}

	/**
	 * Checks if the org.kamrazafar.jtar entry is a directory
	 */
	public boolean isDirectory() {
		if (this.file != null)
			return this.file.isDirectory();

		if (header != null) {
			if (header.linkFlag == TarHeader.LF_DIR)
				return true;

			return header.name.toString().endsWith("/");
		}

		return false;
	}

	/**
	 * Extract header from File
	 */
	private void extractTarHeader(String entryName) {
		header = TarHeader.createHeader(
				entryName, file.length(), file.lastModified() / 1000, file.isDirectory(), PermissionUtils.permissions(file));
	}

	/**
	 * Calculate checksum
	 */
	private static long computeCheckSum(byte[] buf) {
		long sum = 0;

		for (byte aBuf : buf) {
			sum += 255 & aBuf;
		}

		return sum;
	}

	/**
	 * Writes the header to the byte buffer
	 */
	public void writeEntryHeader(byte[] outbuf) {
		int offset = 0;

		offset = TarHeader.getNameBytes(header.name, outbuf, offset, TarHeader.NAMELEN);
		offset = Octal.getOctalBytes(header.mode, outbuf, offset, TarHeader.MODELEN);
		offset = Octal.getOctalBytes(header.userId, outbuf, offset, TarHeader.UIDLEN);
		offset = Octal.getOctalBytes(header.groupId, outbuf, offset, TarHeader.GIDLEN);

		long size = header.size;

		offset = Octal.getLongOctalBytes(size, outbuf, offset, TarHeader.SIZELEN);
		offset = Octal.getLongOctalBytes(header.modTime, outbuf, offset, TarHeader.MODTIMELEN);

		int csOffset = offset;
		for (int c = 0; c < TarHeader.CHKSUMLEN; ++c)
			outbuf[offset++] = (byte) ' ';

		outbuf[offset++] = header.linkFlag;

		offset = TarHeader.getNameBytes(header.linkName, outbuf, offset, TarHeader.NAMELEN);
		offset = TarHeader.getNameBytes(header.magic, outbuf, offset, TarHeader.USTAR_MAGICLEN);
		offset = TarHeader.getNameBytes(header.userName, outbuf, offset, TarHeader.USTAR_USER_NAMELEN);
		offset = TarHeader.getNameBytes(header.groupName, outbuf, offset, TarHeader.USTAR_GROUP_NAMELEN);
		offset = Octal.getOctalBytes(header.devMajor, outbuf, offset, TarHeader.USTAR_DEVLEN);
		offset = Octal.getOctalBytes(header.devMinor, outbuf, offset, TarHeader.USTAR_DEVLEN);
		offset = TarHeader.getNameBytes(header.namePrefix, outbuf, offset, TarHeader.USTAR_FILENAME_PREFIX);

		for (; offset < outbuf.length;)
			outbuf[offset++] = 0;

		long checkSum = TarEntry.computeCheckSum(outbuf);

		Octal.getCheckSumOctalBytes(checkSum, outbuf, csOffset, TarHeader.CHKSUMLEN);
	}

	/**
	 * Parses the tar header to the byte buffer
	 */
	private void parseTarHeader(byte[] bh) {
		int offset = 0;

		header.name = TarHeader.parseName(bh, offset, TarHeader.NAMELEN);
		offset += TarHeader.NAMELEN;

		header.mode = (int) Octal.parseOctal(bh, offset, TarHeader.MODELEN);
		offset += TarHeader.MODELEN;

		header.userId = (int) Octal.parseOctal(bh, offset, TarHeader.UIDLEN);
		offset += TarHeader.UIDLEN;

		header.groupId = (int) Octal.parseOctal(bh, offset, TarHeader.GIDLEN);
		offset += TarHeader.GIDLEN;

		header.size = Octal.parseOctal(bh, offset, TarHeader.SIZELEN);
		offset += TarHeader.SIZELEN;

		header.modTime = Octal.parseOctal(bh, offset, TarHeader.MODTIMELEN);
		offset += TarHeader.MODTIMELEN;

		header.checkSum = (int) Octal.parseOctal(bh, offset, TarHeader.CHKSUMLEN);
		offset += TarHeader.CHKSUMLEN;

		header.linkFlag = bh[offset++];

		header.linkName = TarHeader.parseName(bh, offset, TarHeader.NAMELEN);
		offset += TarHeader.NAMELEN;

		header.magic = TarHeader.parseName(bh, offset, TarHeader.USTAR_MAGICLEN);
		offset += TarHeader.USTAR_MAGICLEN;

		header.userName = TarHeader.parseName(bh, offset, TarHeader.USTAR_USER_NAMELEN);
		offset += TarHeader.USTAR_USER_NAMELEN;

		header.groupName = TarHeader.parseName(bh, offset, TarHeader.USTAR_GROUP_NAMELEN);
		offset += TarHeader.USTAR_GROUP_NAMELEN;

		header.devMajor = (int) Octal.parseOctal(bh, offset, TarHeader.USTAR_DEVLEN);
		offset += TarHeader.USTAR_DEVLEN;

		header.devMinor = (int) Octal.parseOctal(bh, offset, TarHeader.USTAR_DEVLEN);
		offset += TarHeader.USTAR_DEVLEN;

		header.namePrefix = TarHeader.parseName(bh, offset, TarHeader.USTAR_FILENAME_PREFIX);
	}

	/**
	 * Helps dealing with file permissions.
	 */
	enum PermissionUtils {
		;

		/**
		 * XXX: When using standard Java permissions, we treat 'owner' and 'group' equally and give no
		 *      permissions for 'others'.
		 */
		private enum StandardFilePermission {
			EXECUTE(0110), WRITE(0220), READ(0440);

			private final int mode;

			StandardFilePermission(int mode) {
				this.mode = mode;
			}
		}

		private static final Map<PosixFilePermission, Integer> posixPermissionToInteger = new HashMap<>();

		static {
			posixPermissionToInteger.put(PosixFilePermission.OWNER_EXECUTE, 0100);
			posixPermissionToInteger.put(PosixFilePermission.OWNER_WRITE, 0200);
			posixPermissionToInteger.put(PosixFilePermission.OWNER_READ, 0400);

			posixPermissionToInteger.put(PosixFilePermission.GROUP_EXECUTE, 0010);
			posixPermissionToInteger.put(PosixFilePermission.GROUP_WRITE, 0020);
			posixPermissionToInteger.put(PosixFilePermission.GROUP_READ, 0040);

			posixPermissionToInteger.put(PosixFilePermission.OTHERS_EXECUTE, 0001);
			posixPermissionToInteger.put(PosixFilePermission.OTHERS_WRITE, 0002);
			posixPermissionToInteger.put(PosixFilePermission.OTHERS_READ, 0004);
		}

		/**
		 * Get file permissions in octal mode, e.g. 0755.
		 *
		 * Note: it uses `java.nio.file.attribute.PosixFilePermission` if OS supports this, otherwise reverts to
		 * using standard Java file operations, e.g. `java.io.File#canExecute()`. In the first case permissions will
		 * be precisely as reported by the OS, in the second case 'owner' and 'group' will have equal permissions and
		 * 'others' will have no permissions, e.g. if file on Windows OS is `read-only` permissions will be `0550`.
		 *
		 * @throws NullPointerException if file is null.
		 * @throws IllegalArgumentException if file does not exist.
		 */
		public static int permissions(File f) {
			if(f == null) {
				throw new NullPointerException("File is null.");
			}
			if(!f.exists()) {
				throw new IllegalArgumentException("File " + f + " does not exist.");
			}

			return isPosix ? posixPermissions(f) : standardPermissions(f);
		}

		private static final boolean isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

		private static int posixPermissions(File f) {
			int number;
			try {
				Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(f.toPath());
				int sum = posixPermissionToInteger.entrySet().stream().filter(entry -> permissions.contains(entry.getKey())).mapToInt(Map.Entry::getValue).sum();
				number = sum;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return number;
		}

		private static Set<StandardFilePermission> readStandardPermissions(File f) {
			Set<StandardFilePermission> permissions = EnumSet.noneOf(StandardFilePermission.class);
			if(f.canExecute()) {
				permissions.add(StandardFilePermission.EXECUTE);
			}
			if(f.canWrite()) {
				permissions.add(StandardFilePermission.WRITE);
			}
			if(f.canRead()) {
				permissions.add(StandardFilePermission.READ);
			}
			return permissions;
		}

		private static Integer standardPermissions(File f) {
			Set<StandardFilePermission> permissions = readStandardPermissions(f);
			int number = permissions.stream().mapToInt(permission -> permission.mode).sum();
			return number;
		}
	}
}