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
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

//RCP imports
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;

//EclipseLink imports
import org.eclipse.persistence.internal.sessions.factories.XMLSessionConfigProject;
import org.eclipse.persistence.internal.sessions.factories.model.SessionConfigs;
import org.eclipse.persistence.internal.sessions.factories.model.session.DatabaseSessionConfig;
import org.eclipse.persistence.internal.sessions.factories.model.session.SessionConfig;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.sessions.DatabaseSession;

public class KSATRoot {
    
    // avoid double-locked init of singleton
    static class KSATRootHelper {
        static KSATRoot singleton = new KSATRoot();
    }
    public static KSATRoot defaultInstance() {
        return KSATRootHelper.singleton;
    }

    protected Object parent = null;
    protected List<AnalysisDatabase> databases = new ArrayList<AnalysisDatabase>();
    protected AnalysisDatabase currentDatabase = null;
    protected SessionConfigs sessionConfigs = null;
    protected LogConsole logConsole = null;
    
    public KSATRoot() {
        super();
    }
    
    public Object getParent() {
        return parent;
    }
    public void setParent(Object parent) {
        this.parent = parent;
    }

    public LogConsole getLogConsole() {
        return logConsole;
    }
    public void setLogConsole(LogConsole logConsole) {
        this.logConsole = logConsole;
    }

    @SuppressWarnings("unchecked")
    public List<AnalysisDatabase> getDatabases() {
        if (databases.isEmpty()) {
    	    XMLSessionConfigProject sessionConfigProject = new XMLSessionConfigProject();
    	    XMLContext xc = new XMLContext(sessionConfigProject);
    	    try {
                Location instanceLocation = Platform.getInstanceLocation();
                URL fileURL = FileLocator.toFileURL(instanceLocation.getURL()); 
                File instanceFile = new File(fileURL.toURI());
                File ksatSessions = new File(instanceFile, "ksat-sessions.xml");
                if (!ksatSessions.exists()) {
                	ksatSessions.createNewFile();
                	sessionConfigs = new SessionConfigs();
                	xc.createMarshaller().marshal(sessionConfigs, new FileWriter(ksatSessions));
                }
                else {
                    sessionConfigs = (SessionConfigs)xc.createUnmarshaller().unmarshal(ksatSessions);
                }
                for (SessionConfig sc : (Vector<SessionConfig>)sessionConfigs.getSessionConfigs()) {
                	if (sc instanceof DatabaseSessionConfig) {
                        AnalysisDatabase database = new AnalysisDatabase();
                        database.setParent(this);
                        addDatabase(database);
                        database.buildDatabaseSession((DatabaseSessionConfig)sc);
                    }
                }
            }
            catch (Exception e) {
                // TODO figure out database session failure, logging and treeview stuff
                e.printStackTrace();
            }
            if (!databases.isEmpty()) {
                currentDatabase = databases.get(0);
            }
        }
        return databases;
    }
    public void addDatabase(AnalysisDatabase database) {
        if (database != null) {
            databases.add(database);
            database.setParent(this);
        }
    }
    public void removeDatabase(AnalysisDatabase database) {
        databases.remove(database);
        database.setParent(null);
    }

    public AnalysisDatabase getCurrentDatabase() {
		return currentDatabase;
	}
	public void setCurrentDatabase(AnalysisDatabase currentDatabase) {
		this.currentDatabase = currentDatabase;
	}

	public DatabaseSession getCurrentSession() {
		if (currentDatabase != null) {
			return currentDatabase.getSession();
		}
		return null;
    }

    protected SessionConfigs getSessionConfigs() {
		return sessionConfigs;
	}
	protected void setSessionConfigs(SessionConfigs sessionConfigs) {
		this.sessionConfigs = sessionConfigs;
	}

	@Override
    public String toString() {
        return "KSAT";
    }
    
}