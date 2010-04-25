/*
 * This software is licensed under the terms of the ISC License.
 * (ISCL http://www.opensource.org/licenses/isc-license.txt
 * It is functionally equivalent to the 2-clause BSD licence,
 * with language "made unnecessary by the Berne convention" removed).
 * 
 * Copyright (c) 2009, Mike Norman
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
import java.util.ArrayList;
import java.util.List;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.IEvaluationService;

//RCP imports
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

//KSAT domain imports
import ca.carleton.tim.ksat.model.KeywordExpression;
import ca.carleton.tim.ksat.model.Site;
import static ca.carleton.tim.ksat.client.KSATPreferencePage.PROXY_HOST_PREFKEY;
import static ca.carleton.tim.ksat.client.KSATPreferencePage.PROXY_PORT_PREFKEY;

/**
 * This class controls all aspects of the application's execution
 */
public class KSATApplication implements IApplication {

    public static final String PLUGIN_ID = "ca.carleton.tim.ksat";

    public static final String DB_USERNAME = "db.user";
    public static final String DEFAULT_DB_USERNAME = "user";
    public static final String DB_PASSWORD = "db.pwd";
    public static final String DEFAULT_DB_PASSWORD = "password";
    public static final String DB_URL = "db.url";
    public static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/db";
    public static final String DB_DRIVER = "db.driver";
    public static final String DEFAULT_DB_DRIVER = "com.mysql.jdbc.Driver";
    public static final String DB_PLATFORM = "db.platform";
    public static final String DEFAULT_DB_PLATFORM =
        "org.eclipse.persistence.platform.database.MySQLPlatform";
    
    public static ImageRegistry IMAGE_REGISTRY;
    static {
        Thread.currentThread().setContextClassLoader(KSATApplication.class.getClassLoader());
    }

	public Object start(IApplicationContext context) throws Exception {
		Display display = PlatformUI.createDisplay();
        IMAGE_REGISTRY = new ImageRegistry(display);
        ImageDescriptor imageDesc = 
            AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, "icons/root.gif");//$NON-NLS-1$
        IMAGE_REGISTRY.put("root", imageDesc.createImage());
		imageDesc = 
		    AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, "icons/analyses.gif");//$NON-NLS-1$
        IMAGE_REGISTRY.put("analyses", imageDesc.createImage());
        imageDesc = 
            AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, "icons/report.gif");//$NON-NLS-1$
        IMAGE_REGISTRY.put("report", imageDesc.createImage());
        imageDesc = 
            AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, "icons/connecteddb.gif");//$NON-NLS-1$
        IMAGE_REGISTRY.put("connecteddb", imageDesc.createImage());
        imageDesc = 
            AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, "icons/db.gif");//$NON-NLS-1$
        IMAGE_REGISTRY.put("disconnecteddb", imageDesc.createImage());

        // weird timing, but ...
        IPreferenceStore preferenceStore = PlatformUI.getPreferenceStore();
        String proxyHost = preferenceStore.getString(PROXY_HOST_PREFKEY);
        if (proxyHost != null && proxyHost.length() > 0) {
            String proxyPort = preferenceStore.getString(PROXY_PORT_PREFKEY);
            if (proxyPort != null && proxyHost.length() > 0) {
                System.setProperty(PROXY_HOST_PREFKEY, proxyHost);
                System.setProperty(PROXY_PORT_PREFKEY, proxyPort);
            }
        }
		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display, new KSATWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			else
				return IApplication.EXIT_OK;
		}
		finally {
		    IMAGE_REGISTRY.dispose();
			display.dispose();
		}
		
	}

	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
	
	public static void resetViewsOnDisconnectFromDatabase() {
        List<IViewPart> views = getViews(AnalysesView.ID, SitesView.ID, KeywordsView.ID, ResultsView.ID);
        AnalysesView analysesView = (AnalysesView)views.get(0);
        SitesView sitesView = (SitesView)views.get(1);
        KeywordsView keywordsView = (KeywordsView)views.get(2);
        ResultsView resultsView = (ResultsView)views.get(3);
        sitesView.setSites(new ArrayList<Site>());
        keywordsView.setKeywords(new ArrayList<KeywordExpression>());
        analysesView.analysesViewer.refresh(true);
        sitesView.listViewer.refresh(true);
        keywordsView.listViewer.refresh(true);
        resultsView.browser.setText("");
        resultsView.browser.getParent().layout(true);
        resultsView.text.setText("");
        resultsView.text.getParent().layout(true);
        reevaluateIsConnected(analysesView);
	}
	
	public static void resetViewsOnConnectToDatabase() {
        List<IViewPart> views = getViews(AnalysesView.ID);
        AnalysesView analysesView = (AnalysesView)views.get(0);
        analysesView.analysesViewer.refresh(true);
        reevaluateIsConnected(analysesView);
	}
	
	static List<IViewPart> getViews(String... viewIds) {
	    List<IViewPart> views = new ArrayList<IViewPart>();
        IWorkbenchWindow[] workbenchs = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (IWorkbenchWindow workbench : workbenchs) {
            IWorkbenchPage[] pages = workbench.getPages();
            for (IWorkbenchPage page : pages) {
                for (String viewId : viewIds) {
                    IViewPart view = page.findView(viewId);
                    if (view != null) {
                        views.add(view);
                    }
                }
            }
        }
	    return views;
	}
    static void reevaluateIsConnected(IViewPart view) {
        IEvaluationService service = 
            (IEvaluationService)view.getSite().getService(IEvaluationService.class);
        service.requestEvaluation("ca.carleton.tim.ksat.client.isConnected");
    }
}