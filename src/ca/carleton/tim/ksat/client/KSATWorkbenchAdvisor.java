/*
 * This software is licensed under the terms of the ISC License.
 * (ISCL http://www.opensource.org/licenses/isc-license.txt
 * It is functionally equivalent to the 2-clause BSD licence,
 * with language "made unnecessary by the Berne convention" removed).
 * 
 * Copyright (c) 2009, 2010, Mike Norman
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
package ca.carleton.tim.ksat.client;

//javase imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Map;

//RCP imports
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.MessageConsoleStream;

//KSAT imports
import ca.carleton.tim.ksat.client.preferences.LoggingPreferencePage;
import ca.carleton.tim.ksat.persist.DriverAdapterProject;
import ca.carleton.tim.ksat.persist.Drivers;
import static ca.carleton.tim.ksat.client.DriverAdapter.DRIVER_REGISTRY;

public class KSATWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = 
	    "ca.carleton.tim.ksat.client.perspective";
	private static final String DEFAULT_DRIVERS = 
		"ca/carleton/tim/ksat/default_drivers.xml";

    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new KSATWorkbenchWindowAdvisor(configurer);
    }

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

    @Override
    public void initialize(IWorkbenchConfigurer configurer) {
        configurer.setSaveAndRestore(true);
        LogConsole logConsole = new LogConsole("Logging Console",
            ConsolePlugin.getDefault().getImageDescriptor(IConsoleConstants.IMG_VIEW_CONSOLE));
        KSATRoot.defaultInstance().setLogConsole(logConsole);
        IPreferenceStore preferenceStore = PlatformUI.getPreferenceStore();
        boolean enableLogging = preferenceStore.getBoolean(LoggingPreferencePage.LOGGING_PREFKEY);
        if (enableLogging) {
            MessageConsoleStream messageStream = logConsole.getMessageStream();
            System.setOut(new PrintStream(messageStream));
            System.setErr(new PrintStream(messageStream));
        }
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{logConsole});
		XMLContext xc = new XMLContext(new DriverAdapterProject());
        Location instanceLocation = Platform.getInstanceLocation();
        URL fileURL = null;
		try {
			fileURL = FileLocator.toFileURL(instanceLocation.getURL());
	        File instanceFile = new File(fileURL.toURI());
	        File driversFile = new File(instanceFile, "drivers.xml");
	        if (!driversFile.exists()) {
	        	driversFile.createNewFile();
	        	try {
	        		InputStream is =
	        			this.getClass().getClassLoader().getResourceAsStream(DEFAULT_DRIVERS);
	        		if (is != null) {
	        			 byte[] buf = new byte[8192];
	        			 FileOutputStream fos = new FileOutputStream(driversFile);
	        			 int count;
	        			 while ((count = is.read(buf, 0, buf.length)) > 0) {
	        				 fos.write(buf, 0, count);
	        			 }
	        			 fos.flush();
	        		}
	        	}
	        	catch (Exception e) {
	        		e.printStackTrace();
	        	}
	        }
			Drivers drivers;
			try {
				drivers = (Drivers)xc.createUnmarshaller().unmarshal(driversFile);
				if (drivers != null) {
					Map<String, DriverAdapter> driversMap = drivers.getDrivers();
					DRIVER_REGISTRY.clear();
					DRIVER_REGISTRY.putAll(driversMap);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				// ignore
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    }
}