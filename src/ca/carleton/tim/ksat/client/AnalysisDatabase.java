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

//javase imports
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

//RCP imports
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

//EclipseLink imports
import org.eclipse.persistence.sequencing.TableSequence;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;

//KSAT domain imports
import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.persist.AnalysisProject;
import static ca.carleton.tim.ksat.client.KSATApplication.DB_DRIVER;
import static ca.carleton.tim.ksat.client.KSATApplication.DB_PASSWORD;
import static ca.carleton.tim.ksat.client.KSATApplication.DB_PLATFORM;
import static ca.carleton.tim.ksat.client.KSATApplication.DB_URL;
import static ca.carleton.tim.ksat.client.KSATApplication.DB_USERNAME;
import static ca.carleton.tim.ksat.client.KSATApplication.DEFAULT_DB_DRIVER;
import static ca.carleton.tim.ksat.client.KSATApplication.DEFAULT_DB_PASSWORD;
import static ca.carleton.tim.ksat.client.KSATApplication.DEFAULT_DB_PLATFORM;
import static ca.carleton.tim.ksat.client.KSATApplication.DEFAULT_DB_URL;
import static ca.carleton.tim.ksat.client.KSATApplication.DEFAULT_DB_USERNAME;

public class AnalysisDatabase {

    //public static final String CUSTOMIZER_KEY = "customizerClassName";
    public static final String KSAT_SEQUENCE_TABLENAME = "KSAT_SEQUENCE_TABLE";
    
    protected KSATRoot parent;
    protected DatabaseSession session;
    protected List<AnalysisAdapter> analyses = new ArrayList<AnalysisAdapter>();
    
    public AnalysisDatabase() {
        super();
    }

    public KSATRoot getParent() {
        return parent;
    }
    public void setParent(KSATRoot parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    public List<AnalysisAdapter> getAnalyses() {
        if (analyses.isEmpty()) {
            if (session.isConnected()) {
                Vector<Analysis> analysisV = session.readAllObjects(Analysis.class);
                for (Analysis analysis : analysisV) {
                    AnalysisAdapter analysisAdapter = new AnalysisAdapter(this);
                    analysisAdapter.setAnalysis(analysis);
                    analyses.add(analysisAdapter);
                }
            }
        }
        return analyses;
    }
    public void setAnalyses(List<AnalysisAdapter> analyses) {
        this.analyses = analyses;
    }

    public DatabaseSession buildDatabaseSession() {
        IPreferencesService service = Platform.getPreferencesService();
        InstanceScope iScope = new InstanceScope();
        DefaultScope dScope = new DefaultScope();
        IScopeContext[] scopes = new IScopeContext[2];
        scopes[0] = dScope;
        scopes[1] = iScope;
        String username = 
            service.getString(KSATApplication.PLUGIN_ID, DB_USERNAME, DEFAULT_DB_USERNAME, scopes);
        String password = 
            service.getString(KSATApplication.PLUGIN_ID, DB_PASSWORD, DEFAULT_DB_PASSWORD, scopes);
        String url = 
            service.getString(KSATApplication.PLUGIN_ID, DB_URL, DEFAULT_DB_URL, scopes);
        String driver = 
            service.getString(KSATApplication.PLUGIN_ID, DB_DRIVER, DEFAULT_DB_DRIVER, scopes);
        String platformClassname = 
            service.getString(KSATApplication.PLUGIN_ID, DB_PLATFORM, DEFAULT_DB_PLATFORM, scopes);
        AnalysisProject analysisProject = new AnalysisProject();
        DatabaseLogin login = new DatabaseLogin();
        login.setUserName(username);
        login.setPassword(password);
        login.setConnectionString(url);
        login.setDriverClassName(driver);
        login.setPlatformClassName(platformClassname);
        login.setDefaultSequence(new TableSequence("", KSAT_SEQUENCE_TABLENAME));
        analysisProject.setDatasourceLogin(login);
        session = analysisProject.createDatabaseSession();
        session.dontLogMessages();
        /*
        String customizerClassName = getCustomizerClassName();
        if (customizerClassName != null) {
            SessionCustomizer sessionCustomizer = null;
            try {
                Class<SessionCustomizer> customizerClass = null;
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    customizerClass = (Class<SessionCustomizer>)AccessController.doPrivileged(
                        new PrivilegedClassForName(customizerClassName));
                }
                else {
                    customizerClass =
                        PrivilegedAccessHelper.getClassForName(customizerClassName);
                }
                sessionCustomizer = (SessionCustomizer)Helper.getInstanceFromClass(customizerClass);
            }
            catch (Exception e) {
                System.err.println("could not instantiate SessionCustomizer " + customizerClassName +
                    " Exception:" +e);
            }
            try {
                sessionCustomizer.customize(session);
            }
            catch (Exception e) {
                System.err.println("error during session customization: " + e);
            }
        }
        */
        session.login();
        return session;
    }

    public DatabaseSession getSession() {
        return session;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Database ");
        if (session != null) {
            sb.append('(');
            sb.append(String.format("%1$#x",System.identityHashCode(session)));
            sb.append(')');
        }
        return sb.toString();
    }

    public boolean isConnected() {
        if (session != null) {
            return session.isConnected();
        }
        return false;
    }

}