/*
 * Copyright (C) The Spice Group. All rights reserved.
 *
 * This software is published under the terms of the Spice
 * Software License version 1.1, a copy of which has been included
 * with this distribution in the LICENSE.txt file.
 *
 */
package ca.carleton.tim.ksat.utils;

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

}