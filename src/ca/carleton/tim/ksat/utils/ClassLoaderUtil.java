/*
 * This software is licensed under the terms of the ISC License.
 * (ISCL http://www.opensource.org/licenses/isc-license.txt
 * It is functionally equivalent to the 2-clause BSD licence,
 * with language "made unnecessary by the Berne convention" removed).
 * 
 * Copyright (c) 2010, Mike Norman
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE
 * USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package ca.carleton.tim.ksat.utils;

//javase imports
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//KSAT imports
import ca.carleton.tim.ksat.client.DriverAdapter;
import static ca.carleton.tim.ksat.client.DriverAdapter.DRIVER_REGISTRY;

public class ClassLoaderUtil {

	public static ClassLoader buildDriverClassLoader(String driverClass) {
		ClassLoader cl = null;
	    for (Map.Entry<String, DriverAdapter> me : DRIVER_REGISTRY.entrySet()) {
	    	DriverAdapter da = me.getValue();
	    	if (da.isOk()) {
	    		if (da.getDriverClass().equals(driverClass)) {
	    			List<String> paths = da.getJarPaths();
	    			List<URL> urls = new ArrayList<URL>();
	    			for (String path : paths) {
	    				File f = new File(path);
	    				try {
							urls.add(f.toURI().toURL());
						}
	    				catch (MalformedURLException e) {
							// ignore
						}
	    			}
	    			cl = new URLClassLoader(urls.toArray(new URL[urls.size()]),
	    				ClassLoaderUtil.class.getClassLoader());
	                break;
	    		}
	    	}
	    }
	    return cl;
	}
}