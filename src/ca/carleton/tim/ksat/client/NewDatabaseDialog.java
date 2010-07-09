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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

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
        		dbProperties.urlText.getText().length() == 0) {
        		IStatus status = new Status(IStatus.ERROR, AnalysesView.ID, "empty database property");
        		ErrorDialog.openError(activeShell, "Invalid Database Properties", 
        				"All Database Properties must be specified", status);
        		super.cancelPressed();
        		return;
        	}
        	else {
	        	newDatabaseHandler.setDatabaseName(dbProperties.databaseNameText.getText());
	    		newDatabaseHandler.setUserName(dbProperties.userNameText.getText());
	    		newDatabaseHandler.setUrl(dbProperties.urlText.getText());
	    		newDatabaseHandler.setDriverClass(dbProperties.driverText.getText());
	    		newDatabaseHandler.setPlatformClass(dbProperties.platformText.getText());
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
        data.widthHint = 400;
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
        Text urlText = new Text(outerContainer, SWT.BORDER);
        urlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        Label driverLabel = new Label(outerContainer, SWT.NONE);
        driverLabel.setText("Database Driver class:");
        Text driverText = new Text(outerContainer, SWT.BORDER);
        driverText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        Label platformLabel = new Label(outerContainer, SWT.NONE);
        platformLabel.setText("Database Platform class:");
        Text platformText = new Text(outerContainer, SWT.BORDER);
        platformText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        Label logLabel = new Label(outerContainer, SWT.NONE);
        logLabel.setText("Logging Level");
        Combo logLevelCombo = new Combo(outerContainer, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (int level = AbstractSessionLog.ALL, off = AbstractSessionLog.OFF+1; level < off; level++) {
        	logLevelCombo.add(AbstractSessionLog.translateLoggingLevelToString(level));
        }
        if (currentDatabase != null) {
        	DatabaseSession session = currentDatabase.getSession();
        	DatabaseLogin login = (DatabaseLogin)session.getDatasourceLogin();
        	databaseNameText.setText(session.getName());
        	userNameText.setText(login.getUserName());
        	urlText.setText(login.getDatabaseURL());
        	driverText.setText(login.getDriverClassName());
        	platformText.setText(login.getPlatformClassName());
        	int logLevel = session.getLogLevel();
        	logLevelCombo.setText(AbstractSessionLog.translateLoggingLevelToString(logLevel));
        }
        outerContainer.pack();
        return new DbProperties(databaseNameText, userNameText, urlText, driverText, platformText, 
        	passwordText, logLevelCombo);
    }

}
