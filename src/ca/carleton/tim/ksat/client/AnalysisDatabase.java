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

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;

//EclipseLink imports
import org.eclipse.persistence.internal.sessions.factories.model.log.DefaultSessionLogConfig;
import org.eclipse.persistence.internal.sessions.factories.model.login.DatabaseLoginConfig;
import org.eclipse.persistence.internal.sessions.factories.model.session.DatabaseSessionConfig;
import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.sequencing.TableSequence;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;

//KSAT domain imports
import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.persist.AnalysisProject;

public class AnalysisDatabase {

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

    public DatabaseSession buildDatabaseSession(DatabaseSessionConfig dsc) {
        AnalysisProject analysisProject = new AnalysisProject();
    	DatabaseLoginConfig loginConfig = (DatabaseLoginConfig)dsc.getLoginConfig();
        DatabaseLogin login = new DatabaseLogin();
        login.setUserName(loginConfig.getUsername());
        login.setEncryptedPassword(loginConfig.getEncryptedPassword());
        login.setConnectionString(loginConfig.getConnectionURL());
        login.setDriverClassName(loginConfig.getDriverClass());
        login.setPlatformClassName(loginConfig.getPlatformClass());
        login.setDefaultSequence(new TableSequence("", KSAT_SEQUENCE_TABLENAME));
        analysisProject.setDatasourceLogin(login);
        session = analysisProject.createDatabaseSession();
        session.setName(dsc.getName());
        // KSAT application preferences - global logging toggle
        IPreferenceStore preferenceStore = PlatformUI.getPreferenceStore();
        boolean enableLogging = preferenceStore.getBoolean(LoggingPreferencePage.LOGGING_PREFKEY);
        if (!enableLogging) {
            session.dontLogMessages();
        }
        else {
            DefaultSessionLogConfig logConfig = (DefaultSessionLogConfig)dsc.getLogConfig();
            if (logConfig != null) {
            	String levelStr = logConfig.getLogLevel();
                if ("OFF".equalsIgnoreCase(levelStr)) {
                	session.dontLogMessages();
                }
                else {
                	session.setLogLevel(AbstractSessionLog.translateStringToLoggingLevel(levelStr));
                }
            }
            else {
            	session.dontLogMessages();
            }
        }
        return session;
    }

    public DatabaseSession getSession() {
        return session;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (session != null) {
            sb.append(session.getName());
        }
        else {
            sb.append("<empty Database>");
        }
        return sb.toString();
    }

    public boolean isConnected() {
        if (session != null) {
            return session.isConnected();
        }
        return false;
    }

    public void connect() {
        if (session != null && !session.isConnected()) {
            session.login();
        }
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.logout();
        }
    }
}