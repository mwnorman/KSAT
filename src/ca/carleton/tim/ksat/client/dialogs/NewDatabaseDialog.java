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
package ca.carleton.tim.ksat.client.dialogs;

//javas imports
import java.util.Map;
//Graphics (SWT/JFaces) imports
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

//RCP imports
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

//EclipseLink imports
import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;

//KSAT domain imports
import ca.carleton.tim.ksat.client.AnalysisDatabase;
import ca.carleton.tim.ksat.client.DriverAdapter;
import ca.carleton.tim.ksat.client.handlers.CreateNewDatabaseHandler;
import ca.carleton.tim.ksat.client.views.AnalysesView;
import static ca.carleton.tim.ksat.client.DriverAdapter.DRIVER_REGISTRY;

public class NewDatabaseDialog extends Dialog {

	protected Shell activeShell;
	protected CreateNewDatabaseHandler newDatabaseHandler;
	protected DbProperties dbProperties;

	public NewDatabaseDialog(Shell parent) {
        super(parent);
    }

    public NewDatabaseDialog(IShellProvider parentShell) {
        super(parentShell);
    }
    
    public NewDatabaseDialog(Shell activeShell, CreateNewDatabaseHandler newDatabaseHandler) {
        this(activeShell);
		this.activeShell = activeShell;
		this.newDatabaseHandler = newDatabaseHandler;
    }


    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.RESIZE | getShellStyle());
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite outerContainer = (Composite)super.createDialogArea(parent);
        dbProperties = buildEditDatabaseDialog(outerContainer, null);
        return outerContainer;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) { //Ok
        	if (dbProperties.databaseNameText.getText().length() == 0 ||
        		dbProperties.urlCombo.getText().length() == 0) {
        		IStatus status = new Status(IStatus.ERROR, AnalysesView.ID, "empty database property");
        		ErrorDialog.openError(activeShell, "Invalid Database Properties", 
        				"All Database Properties must be specified", status);
        		super.cancelPressed();
        		return;
        	}
        	else {
	        	newDatabaseHandler.setDatabaseName(dbProperties.databaseNameText.getText());
	    		newDatabaseHandler.setUserName(dbProperties.userNameText.getText());
	    		newDatabaseHandler.setUrl(dbProperties.urlCombo.getText());
	    		newDatabaseHandler.setDriverClass(dbProperties.driverCombo.getText());
	    		newDatabaseHandler.setPlatformClass(dbProperties.platformCombo.getText());
	    		newDatabaseHandler.setPassword(dbProperties.passwordText.getText());
	    		int index = dbProperties.logLevelCombo.getSelectionIndex();
	    		String item = dbProperties.logLevelCombo.getItem(index);
	    		newDatabaseHandler.setLogLevel(item);
        	}
        }
        super.buttonPressed(buttonId);
    }
    
    public static DbProperties buildEditDatabaseDialog(Composite outerContainer, 
    	AnalysisDatabase currentDatabase) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        outerContainer.setLayout(layout);
        GridData data = new GridData();
        data.widthHint = 500;
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        outerContainer.setLayoutData(data);
        GridData dbPropertiesData = new GridData(GridData.FILL_HORIZONTAL);
        dbPropertiesData.grabExcessVerticalSpace = true;
        Label databaseNameLabel = new Label(outerContainer, SWT.NONE);
        databaseNameLabel.setText("Database Name:");
        Text databaseNameText = new Text(outerContainer, SWT.BORDER);
        databaseNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        Label userNameLabel = new Label(outerContainer, SWT.NONE);
        userNameLabel.setText("Database Username:");
        Text userNameText = new Text(outerContainer, SWT.BORDER);
        userNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        Label passwordLabel = new Label(outerContainer, SWT.NONE);
        passwordLabel.setText("Database Password:");
        Text passwordText = new Text(outerContainer, SWT.BORDER);
        passwordText.setEchoChar('*');
        passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
           
        Label urlLabel = new Label(outerContainer, SWT.NONE);
        urlLabel.setText("Database URL:");
        Combo urlCombo = new Combo(outerContainer, SWT.DROP_DOWN);
        urlCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        boolean someExampleUrl = false;
        for (Map.Entry<String, DriverAdapter> me : DRIVER_REGISTRY.entrySet()) {
        	DriverAdapter da = me.getValue();
        	if (da.isOk()) {
        		for (String exampleUrl : da.getExampleURLs().values()) {
        			urlCombo.add(exampleUrl);
        			someExampleUrl = true;
        		}
        	}
        }
        if (someExampleUrl) {
        	urlCombo.select(0);
        }
        
        Label driverLabel = new Label(outerContainer, SWT.NONE);
        driverLabel.setText("Database Driver class:");
        Combo driverCombo = new Combo(outerContainer, SWT.DROP_DOWN);
        driverCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        boolean someDriver = false;
        for (Map.Entry<String, DriverAdapter> me : DRIVER_REGISTRY.entrySet()) {
        	DriverAdapter da = me.getValue();
        	if (da.isOk()) {
        		driverCombo.add(da.getDriverClass());
        		someDriver = true;
        	}
        }
        if (someDriver) {
        	driverCombo.select(0);
        }
        
        Label platformLabel = new Label(outerContainer, SWT.NONE);
        platformLabel.setText("Database Platform class:");
        Combo platformCombo = new Combo(outerContainer, SWT.DROP_DOWN);
        platformCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        boolean somePlatform = false;
        for (Map.Entry<String, DriverAdapter> me : DRIVER_REGISTRY.entrySet()) {
        	DriverAdapter da = me.getValue();
        	if (da.isOk()) {
        		for (String platform : da.getPlatforms()) {
        			platformCombo.add(platform);
        			somePlatform = true;
        		}
        	}
        }
        if (somePlatform) {
        	platformCombo.select(0);
        }
        
        Label logLabel = new Label(outerContainer, SWT.NONE);
        logLabel.setText("Logging Level");
        Combo logLevelCombo = new Combo(outerContainer, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (int level = AbstractSessionLog.ALL, off = AbstractSessionLog.OFF+1; level < off; level++) {
        	logLevelCombo.add(AbstractSessionLog.translateLoggingLevelToString(level));
        	logLevelCombo.select(level);
        }
        if (currentDatabase != null) {
        	DatabaseSession session = currentDatabase.getSession();
        	DatabaseLogin login = (DatabaseLogin)session.getDatasourceLogin();
        	databaseNameText.setText(session.getName());
        	userNameText.setText(login.getUserName());
        	urlCombo.removeAll();
        	urlCombo.setText(login.getDatabaseURL());
        	// find driver and set selection index
        	String[] driverItems = driverCombo.getItems();
        	String theDriverClassName = login.getDriverClassName();
        	for (int i = 0, len = driverItems.length; i < len; i++) {
    			if (driverItems[i].equals(theDriverClassName)) {
    				driverCombo.select(i);
    				break;
    			}
        	}
        	// find platform and set selection index
        	String[] platformItems = platformCombo.getItems();
        	String thePlatformName = login.getPlatformClassName();
        	for (int i = 0, len = platformItems.length; i < len; i++) {
    			if (platformItems[i].equals(thePlatformName)) {
    				platformCombo.select(i);
    				break;
    			}
        	}
        	// find logLevel and set selection index
        	String[] logItems = logLevelCombo.getItems();
        	String logLevel = AbstractSessionLog.translateLoggingLevelToString(session.getLogLevel());
        	for (int i = 0, len = logItems.length; i < len; i++) {
    			if (logItems[i].equals(logLevel)) {
    				logLevelCombo.select(i);
    				break;
    			}
        	}
        }
        outerContainer.pack();
        return new DbProperties(databaseNameText, userNameText, urlCombo, driverCombo, platformCombo, 
        	passwordText, logLevelCombo);
    }

}
