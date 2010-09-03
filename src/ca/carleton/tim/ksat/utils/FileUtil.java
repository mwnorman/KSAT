/*
 * Copyright (C) The Spice Group. All rights reserved.
 *
 * This software is published under the terms of the Spice
 * Software License version 1.1, a copy of which has been included
 * with this distribution in the LICENSE.txt file.
 *
 *
 * Copyright (C) 2001-2003 Colin Bell
 * colbell@users.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */
package ca.carleton.tim.ksat.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {

	/**
	 * Normalize a path. That means:
	 * <ul>
	 * <li>changes to unix style if under windows</li>
	 * <li>eliminates "/../" and "/./"</li>
	 * <li>if path is absolute (starts with '/') and there are too many
	 * occurences of "../" (would then have some kind of 'negative' path)
	 * returns null.</li>
	 * <li>If path is relative, the exceeding ../ are kept at the begining of
	 * the path.</li>
	 * </ul>
	 * <br>
	 * <br>
	 * 
	 * <b>Note:</b> note that this method has been tested with unix and windows
	 * only.
	 * 
	 * <p>
	 * Eg:
	 * </p>
	 * 
	 * <pre>
	 * /foo//               -->     /foo/
	 * /foo/./              -->     /foo/
	 * /foo/../bar          -->     /bar
	 * /foo/../bar/         -->     /bar/
	 * /foo/../bar/../baz   -->     /baz
	 * //foo//./bar         -->     /foo/bar
	 * /../                 -->     null
	 * </pre>
	 * 
	 * @param path
	 *            the path to be normalized.
	 * @return the normalized path or null.
	 * @throws java.lang.NullPointerException
	 *             if path is null.
	 */
	public static final String normalize(String path) {
		if (path.length() < 2) {
			return path;
		}

		StringBuffer buff = new StringBuffer(path);

		int length = path.length();

		// this whole prefix thing is for windows compatibility only.
		String prefix = null;

		if (length > 2 && buff.charAt(1) == ':') {
			prefix = path.substring(0, 2);
			buff.delete(0, 2);
			path = path.substring(2);
			length -= 2;
		}

		boolean startsWithSlash = length > 0
				&& (buff.charAt(0) == '/' || buff.charAt(0) == '\\');

		boolean expStart = true;
		int ptCount = 0;
		int lastSlash = length + 1;
		int upLevel = 0;

		for (int i = length - 1; i >= 0; i--) {
			switch (path.charAt(i)) {
			case '\\':
				buff.setCharAt(i, '/');
			case '/':
				if (lastSlash == i + 1) {
					buff.deleteCharAt(i);
				}

				switch (ptCount) {
				case 1:
					buff.delete(i, lastSlash);
					break;

				case 2:
					upLevel++;
					break;

				default:
					if (upLevel > 0 && lastSlash != i + 1) {
						buff.delete(i, lastSlash + 3);
						upLevel--;
					}
					break;
				}

				ptCount = 0;
				expStart = true;
				lastSlash = i;
				break;

			case '.':
				if (expStart) {
					ptCount++;
				}
				break;

			default:
				ptCount = 0;
				expStart = false;
				break;
			}
		}

		switch (ptCount) {
		case 1:
			buff.delete(0, lastSlash);
			break;

		case 2:
			break;

		default:
			if (upLevel > 0) {
				if (startsWithSlash) {
					return null;
				} else {
					upLevel = 1;
				}
			}

			while (upLevel > 0) {
				buff.delete(0, lastSlash + 3);
				upLevel--;
			}
			break;
		}

		length = buff.length();
		boolean isLengthNull = length == 0;
		char firstChar = isLengthNull ? (char) 0 : buff.charAt(0);

		if (!startsWithSlash && !isLengthNull && firstChar == '/') {
			buff.deleteCharAt(0);
		} else if (startsWithSlash
				&& (isLengthNull || (!isLengthNull && firstChar != '/'))) {
			buff.insert(0, '/');
		}

		if (prefix != null) {
			buff.insert(0, prefix);
		}

		return buff.toString();
	}

	public static Class<?>[] getAssignableClasses(
			URLClassLoader urlClassLoader, Class<?> type) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		URL[] urls = urlClassLoader.getURLs();
		for (int i = 0; i < urls.length; ++i) {
			URL url = urls[i];
			File file = new File(url.getFile());
			if (!file.isDirectory() && file.exists() && file.canRead()) {
				ZipFile zipFile = null;
				try {
					zipFile = new ZipFile(file);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				for (Enumeration<? extends ZipEntry> it = zipFile.entries(); it
						.hasMoreElements();) {
					Class<?> cls = null;
					String entryName = it.nextElement().getName();
					String className = changeFileNameToClassName(entryName);
					if (className != null) {
						try {
							cls = urlClassLoader.loadClass(className);
						} catch (Throwable th) {
							th.printStackTrace();
						}
						if (cls != null) {
							if (type.isAssignableFrom(cls)) {
								classes.add(cls);
							}
						}
					}
				}
			}
		}
		return (Class<?>[]) classes.toArray(new Class[classes.size()]);
	}

	public static String changeFileNameToClassName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("File Name == null");
		}
		String className = null;
		if (name.toLowerCase().endsWith(".class")) {
			className = name.replace('/', '.');
			className = className.replace('\\', '.');
			className = className.substring(0, className.length() - 6);
		}
		return className;
	}
}