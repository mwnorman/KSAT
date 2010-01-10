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
package ca.carleton.tim.ksat.persist;

//javase imports
import java.io.File;
import java.io.PrintWriter;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

//EclipseLink imports
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.internal.databaseaccess.DatabasePlatform;
import org.eclipse.persistence.internal.helper.Helper;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.internal.security.PrivilegedClassForName;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLUnmarshaller;
import org.eclipse.persistence.platform.database.MySQLPlatform;
import org.eclipse.persistence.sequencing.TableSequence;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.UnitOfWork;

public class AnalysisBuilder {

    public static final String ANALYSIS_MODEL_FILE_PATH = "-analysisFile";
    public static final String DRIVER_KEY = "driver";
    public static final String USERNAME_KEY= "username";
    public static final String PASSWORD_KEY = "password";
    public static final String URL_KEY = "url";
    public static final String LOG_LEVEL_KEY = "logLevel";
    public static final String PLATFORM_CLASSNAME_KEY = "platformClassname";
    public static final String DEFAULT_PLATFORM_CLASSNAME =
        "org.eclipse.persistence.platform.database.MySQLPlatform";
    public static final String CUSTOMIZER_KEY = "customizerClassName";
    public static final String KSAT_SEQUENCE_TABLENAME = "KSAT_SEQUENCE_TABLE";
    
    public Map<String, String> properties = new LinkedHashMap<String, String>();
    public ArrayList<AnalysisOperationModel> operations = new ArrayList<AnalysisOperationModel>();
    protected DatabasePlatform databasePlatform;
    protected Logger logger;
    public boolean quiet = false;
    protected DatabaseSession session = null;

	public AnalysisBuilder() {
		super();
	}
	
    public static void main(String[] args) {
        AnalysisBuilder builder = new AnalysisBuilder();
        builder.start(args);
    } 
    
    public void start(String[] args) {
        if (args.length > 0 && ANALYSIS_MODEL_FILE_PATH.equals(args[0])) {
            String analysisModelFilename = args[1];
            File analysisModelFile = new File(analysisModelFilename);
            if (analysisModelFile.exists() && analysisModelFile.isFile()) {
                XMLContext context = new XMLContext(new AnalysisModelProject());
                XMLUnmarshaller unmarshaller = context.createUnmarshaller();
                AnalysisBuilder builderModel = (AnalysisBuilder)unmarshaller.unmarshal(analysisModelFile);
                properties = builderModel.properties;
                operations = builderModel.operations;
                if (operations.size() == 0) {
                    logMessage(SEVERE, "No operations specified");
                    return;
                }
                else {
                    start();
                }
            }
            else {
                logMessage(SEVERE, "Unable to locate Analysis model file " +
                    analysisModelFilename);
                return;
            }
        }
        else {
            //prompt> java -cp eclipselink.jar:ksat.jar:your_favourite_jdbc_driver.jar ca.carleton.tim.ksat.impl.mappings.AnalysisBuilder -analysisFile {path_to_analysis-model.xml_file}        
            StringBuilder sb = new StringBuilder(30);
            sb.append("AnalysisBuilder usage:\nprompt> java -cp eclipselink.jar:ksat.jar:your_favourite_jdbc_driver.jar \\\n\t");
            sb.append(this.getClass().getName());
            sb.append(" ");
            sb.append(ANALYSIS_MODEL_FILE_PATH);
            sb.append(" {path_to_analysis-model.xml_file} \\\n");
            logMessage(SEVERE, sb.toString());
            return;
        }
    }
    
    public void start() {
        AnalysisProject analysisProject = new AnalysisProject();
        DatabaseLogin login = new DatabaseLogin();
        login.setUserName(getUsername());
        login.setPassword(getPassword());
        login.setConnectionString(getUrl());
        login.setDriverClassName(getDriver());
        login.setPlatform(getDatabasePlatform());
        login.setDefaultSequence(new TableSequence("", KSAT_SEQUENCE_TABLENAME));
        analysisProject.setDatasourceLogin(login);
        session = analysisProject.createDatabaseSession();
        if (quiet) {
            session.dontLogMessages();
        }
        else {
            String logLevel = getLogLevel();
            if (OFF.getName().equalsIgnoreCase(logLevel)) {
                session.dontLogMessages();
            }
            else if (CONFIG.getName().equalsIgnoreCase(logLevel)) {
                session.setLogLevel(SessionLog.CONFIG);
            }
            else if (FINE.getName().equalsIgnoreCase(logLevel)) {
                session.setLogLevel(SessionLog.FINE);
            }
            else if (FINER.getName().equalsIgnoreCase(logLevel)) {
                session.setLogLevel(SessionLog.FINER);
            }
            else if (FINEST.getName().equalsIgnoreCase(logLevel)) {
                session.setLogLevel(SessionLog.FINEST);
            }
            else if (INFO.getName().equalsIgnoreCase(logLevel)) {
                session.setLogLevel(SessionLog.INFO);
            }
            else if (SEVERE.getName().equalsIgnoreCase(logLevel)) {
                session.setLogLevel(SessionLog.SEVERE);
            }
            else if (WARNING.getName().equalsIgnoreCase(logLevel)) {
                session.setLogLevel(SessionLog.WARNING);
            }
        }
        if (getCustomizerClassName() != null) {
            SessionCustomizer sessionCustomizer = getSessionCustomizer();
            try {
                sessionCustomizer.customize(session);
            }
            catch (Exception e) {
                // e.printStackTrace();
                logMessage(SEVERE, "error during session customization", e);
                return;
            }
        }
        try {
            session.login();
        }
        catch (DatabaseException e) {
            // e.printStackTrace();
            logMessage(SEVERE, "failure logging into database", e);
            return;
        }
        for (AnalysisOperationModel opModel : operations) {
            UnitOfWork uow = session.acquireUnitOfWork();
            opModel.build(this, uow);
            uow.commit();
        }
    }

    public Map<String, String> getProperties() {
        return properties;
    }
    public void setProperties(Map<String, String> properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    public ArrayList<AnalysisOperationModel> getOperations() {
        return operations;
    }
    public void setOperations(ArrayList<AnalysisOperationModel> operations) {
        this.operations = operations;
    }

    public String getDriver() {
        return properties.get(DRIVER_KEY);
    }
    public void setDriver(String driver) {
        properties.put(DRIVER_KEY, driver);
    }

    public String getUsername() {
        return properties.get(USERNAME_KEY);
    }
    public void setUsername(String username) {
        properties.put(USERNAME_KEY, username);
    }

    public String getPassword() {
        return properties.get(PASSWORD_KEY);
    }
    public void setPassword(String password) {
        properties.put(PASSWORD_KEY, password);
    }

    public String getUrl() {
        return properties.get(URL_KEY);
    }
    public void setUrl(String url) {
        properties.put(URL_KEY, url);
    }

    public String getPlatformClassname() {
        String platformClassname = properties.get(PLATFORM_CLASSNAME_KEY);
        if (platformClassname == null || platformClassname.length() == 0) {
            platformClassname = DEFAULT_PLATFORM_CLASSNAME;
            setPlatformClassname(platformClassname);
        }
        return platformClassname;

    }
    public void setPlatformClassname(String platformClassname) {
        properties.put(PLATFORM_CLASSNAME_KEY, platformClassname);

    }
    @SuppressWarnings("unchecked")
    public DatabasePlatform getDatabasePlatform() {
        String platformClassname = getPlatformClassname();
        try {
            Class platformClass = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                platformClass = (Class)AccessController.doPrivileged(
                    new PrivilegedClassForName(platformClassname));
            }
            else {
                platformClass =
                    PrivilegedAccessHelper.getClassForName(platformClassname);
            }
            databasePlatform = (DatabasePlatform)Helper.getInstanceFromClass(platformClass);
        }
        catch (Exception e) {
            databasePlatform = new MySQLPlatform();
        }
        return databasePlatform;
    }
    public void setDatabasePlatform(DatabasePlatform databasePlatform) {
        this.databasePlatform = databasePlatform;
    }

    public String getCustomizerClassName() {
        return properties.get(CUSTOMIZER_KEY);
    }

    public void setCustomizerClassName(String sessionCustomizerClassName) {
        properties.put(CUSTOMIZER_KEY, sessionCustomizerClassName);
    }

    @SuppressWarnings("unchecked")
    protected SessionCustomizer getSessionCustomizer() {
        SessionCustomizer customizer = null;
        String customizerClassName = getCustomizerClassName();
        try {
            Class customizerClass = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                customizerClass = (Class)AccessController.doPrivileged(
                    new PrivilegedClassForName(customizerClassName));
            }
            else {
                customizerClass =
                    PrivilegedAccessHelper.getClassForName(customizerClassName);
            }
            customizer = (SessionCustomizer)Helper.getInstanceFromClass(customizerClass);
        }
        catch (Exception e) {
            logMessage(SEVERE, "could not instantiate SessionCustomizer " + customizerClassName, e);
        }
        return customizer;
    }

    public DatabaseSession getSession() {
        return session;
    }

    protected void logMessage(Level level, String message) {
        if (logger != null) {
            logger.log(level, message);
        }
        else if (!quiet) {
            System.out.println(message);
        }
    }

    protected void logMessage(Level severe, String message, Exception e) {
        if (logger != null) {
            logger.log(severe, message, e);
        }
        else {
            PrintWriter pw = new PrintWriter(System.out);
            e.printStackTrace(pw);
            System.out.println(message);
        }
    }

    public String getLogLevel() {
        return properties.get(LOG_LEVEL_KEY);
    }

    public void setLogLevel(String logLevel) {
        properties.put(LOG_LEVEL_KEY, logLevel);
    }

    public Logger getLogger() {
        return logger;
    }
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public boolean isQuiet() {
        return quiet;
    }
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }
}