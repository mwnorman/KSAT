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

//Graphics (SWT/JFaces) imports
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ca.carleton.tim.ksat.client.AnalysisDatabase;
import ca.carleton.tim.ksat.client.handlers.EditDatabaseHandler;
import ca.carleton.tim.ksat.client.views.AnalysesView;

public class EditDatabaseDialog extends Dialog {
    
    protected EditDatabaseHandler editDatabaseHandler;
	protected AnalysisDatabase currentDatabase;
	protected DbProperties dbProperties;
	protected Shell activeShell;

	public EditDatabaseDialog(Shell parent) {
        super(parent);
    }

    public EditDatabaseDialog(IShellProvider parentShell) {
        super(parentShell);
    }
    
    public EditDatabaseDialog(Shell activeShell, EditDatabaseHandler editDatabaseHandler,
    	AnalysisDatabase currentDatabase) {
        this(activeShell);
		this.activeShell = activeShell;
		this.editDatabaseHandler = editDatabaseHandler;
		this.currentDatabase = currentDatabase;
    }


    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.RESIZE | getShellStyle());
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite outerContainer = (Composite) super.createDialogArea(parent);
        dbProperties = NewDatabaseDialog.buildEditDatabaseDialog(outerContainer, currentDatabase);
        return outerContainer;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) { //Ok
        	if (dbProperties.databaseNameText.getText().length() == 0 ||
        		dbProperties.userNameText.getText().length() == 0 ||
        		dbProperties.urlText.getText().length() == 0 ||
        		dbProperties.passwordText.getText().length() == 0) {
        		IStatus status = new Status(IStatus.ERROR, AnalysesView.ID, "empty database property");
        		ErrorDialog.openError(activeShell, "Invalid Database Properties", 
        				"All Database Properties must be specified", status);
        		super.cancelPressed();
        		return;
        	}
        	else {
	        	editDatabaseHandler.setDatabaseName(dbProperties.databaseNameText.getText());
	    		editDatabaseHandler.setUserName(dbProperties.userNameText.getText());
	    		editDatabaseHandler.setUrl(dbProperties.urlText.getText());
	    		editDatabaseHandler.setDriver(dbProperties.driverText.getText());
	    		editDatabaseHandler.setPlatform(dbProperties.platformText.getText());
	    		editDatabaseHandler.setPassword(dbProperties.passwordText.getText());
	    		int index = dbProperties.logLevelCombo.getSelectionIndex();
	    		String item = dbProperties.logLevelCombo.getItem(index);
	    		editDatabaseHandler.setLogLevel(item);
        	}
        }
        super.buttonPressed(buttonId);
    }
  
}