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

/**
 * @author Kamran
 * 
 */
public enum TarUtils {
	;

	/**
	 * Determines the tar file size of the given folder/file path
	 */
	public static long calculateTarSize(File path) {
		return tarSize(path) + TarConstants.EOF_BLOCK;
	}

	private static long tarSize(File dir) {
		long size = 0;

		if (dir.isFile()) {
			return entrySize(dir.length());
		} else {
			File[] subFiles = dir.listFiles();

			if (subFiles != null && subFiles.length > 0) {
				for (File file : subFiles) {
					size += file.isFile() ? entrySize(file.length()) : tarSize(file);
				}
			} else {
				
				return TarConstants.HEADER_BLOCK;
			}
		}

		return size;
	}

	private static long entrySize(long fileSize) {
		long size = 0;
		size += TarConstants.HEADER_BLOCK; 
		size += fileSize; 

		long extra = size % TarConstants.DATA_BLOCK;

		if (extra > 0) {
			size += (TarConstants.DATA_BLOCK - extra); 
		}

		return size;
	}

	public static String trim(String s, char c) {
		StringBuilder tmp = new StringBuilder(s);
		for (int i = 0; i < tmp.length(); i++) {
			if (tmp.charAt(i) != c) {
				break;
			} else {
				tmp.deleteCharAt(i);
			}
		}

		for (int i = tmp.length() - 1; i >= 0; i--) {
			if (tmp.charAt(i) != c) {
				break;
			} else {
				tmp.deleteCharAt(i);
			}
		}

		return tmp.toString();
	}
}
