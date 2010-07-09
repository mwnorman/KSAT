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
package ca.carleton.tim.ksat.client;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.persistence.internal.sessions.factories.XMLSessionConfigProject;
import org.eclipse.persistence.internal.sessions.factories.model.SessionConfigs;
import org.eclipse.persistence.internal.sessions.factories.model.log.DefaultSessionLogConfig;
import org.eclipse.persistence.internal.sessions.factories.model.login.DatabaseLoginConfig;
import org.eclipse.persistence.internal.sessions.factories.model.session.DatabaseSessionConfig;
import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.sequencing.TableSequence;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ca.carleton.tim.ksat.persist.AnalysisProject;

public class CreateNewDatabaseHandler extends AbstractHandler {

    protected String databaseName;
    protected String userName;
    protected String url;
    protected String driverClass;
    protected String platformClass;
    protected String password;
    protected String logLevel;
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell activeShell = HandlerUtil.getActiveShell(event);
        NewDatabaseDialog dialog = new NewDatabaseDialog(activeShell, this);
        int status = dialog.open();
        if (status == Window.OK) {
        	KSATRoot root = KSATRoot.defaultInstance();
            AnalysisProject analysisProject = new AnalysisProject();
        	DatabaseLoginConfig loginConfig = new DatabaseLoginConfig();
        	loginConfig.setBindAllParameters(true);
        	loginConfig.setUsername(userName);
        	loginConfig.setPassword(password);
        	loginConfig.setConnectionURL(url);
        	loginConfig.setDriverClass(driverClass);
        	loginConfig.setPlatformClass(platformClass);
            DatabaseLogin login = new DatabaseLogin();
            login.bindAllParameters();
            login.setUserName(userName);
            login.setPassword(password);
            login.setConnectionString(url);
            login.setDriverClassName(driverClass);
            login.setPlatformClassName(platformClass);
            login.setDefaultSequence(new TableSequence("", AnalysisDatabase.KSAT_SEQUENCE_TABLENAME));
            analysisProject.setDatasourceLogin(login);
            DatabaseSession session = analysisProject.createDatabaseSession();
            // TODO - check for re-used session/databaseNames
            session.setName(databaseName);
        	session.setLogLevel(AbstractSessionLog.translateStringToLoggingLevel(logLevel));
        	DatabaseSessionConfig dsc = new DatabaseSessionConfig();
        	dsc.setName(databaseName);
        	dsc.setLoginConfig(loginConfig);
        	DefaultSessionLogConfig logConfig = new DefaultSessionLogConfig();
        	dsc.setLogConfig(logConfig);
            if ("OFF".equalsIgnoreCase(logLevel)) {
            	session.dontLogMessages();
            }
            else {
            	logConfig.setLogLevel(logLevel);
            }
            AnalysisDatabase newDatabase = new AnalysisDatabase();
            newDatabase.setParent(root);
            root.addDatabase(newDatabase);
            root.setCurrentDatabase(newDatabase);
            newDatabase.session = session;
        	SessionConfigs sessionConfigs = root.getSessionConfigs();
        	sessionConfigs.addSessionConfig(dsc);
        	List<IViewPart> views = KSATApplication.getViews(AnalysesView.ID);
        	AnalysesView analysesView = (AnalysesView) views.get(0);
        	analysesView.analysesViewer.refresh(true);
            // make changes persistent
        	XMLSessionConfigProject sessionConfigProject = new XMLSessionConfigProject();
        	XMLContext xc = new XMLContext(sessionConfigProject);
        	try {
                Location instanceLocation = Platform.getInstanceLocation();
                URL fileURL = FileLocator.toFileURL(instanceLocation.getURL()); 
                File instanceFile = new File(fileURL.toURI());
                File ksatSessions = new File(instanceFile, "ksat-sessions.xml");
                xc.createMarshaller().marshal(sessionConfigs, new FileWriter(ksatSessions));
        	}
        	catch (Exception e) {
        		e.printStackTrace();
        	}
        
        }
        return null;
    }

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public void setPlatformClass(String platformClass) {
		this.platformClass = platformClass;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

}
