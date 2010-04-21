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

import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.swt.widgets.Text;

import static ca.carleton.tim.ksat.client.KSATApplication.DB_DRIVER;
import static ca.carleton.tim.ksat.client.KSATApplication.DB_PASSWORD;
import static ca.carleton.tim.ksat.client.KSATApplication.DB_PLATFORM;
import static ca.carleton.tim.ksat.client.KSATApplication.DB_URL;
import static ca.carleton.tim.ksat.client.KSATApplication.DB_USERNAME;

public class DBPropertiesPage extends PropertyPage implements IWorkbenchPropertyPage {

    public static final String QUALIFIER = "ca.carleton.tim.ksat.client.dbproperty"; //$NON-NLS-1$
    public static final String SELECTED_DB = "SELECTED_DB"; //$NON-NLS-1$
    public static final String INITIAL_VALUE = "DBPropertyPage"; //$NON-NLS-1$
    private Text userNameText;
    private Text passwordText;
    private Text urlText;
    private Text driverText;
    private Text platformText;
    
    public DBPropertiesPage() {
        super();
        setTitle("Database Properties");
    }
    
    protected Control createContents(Composite parent) {
        Composite outerComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        outerComposite.setLayout(layout);

        Composite nestedComposite = new Composite(parent, SWT.NULL);
        GridLayout nestedLayout = new GridLayout();
        nestedComposite.setLayout(nestedLayout);
        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        nestedComposite.setLayoutData(data);
        GridData dbPropertiesData = new GridData(GridData.FILL_HORIZONTAL);
        dbPropertiesData.grabExcessVerticalSpace = true;
        
        Label userNameLabel = new Label(outerComposite, SWT.NONE);
        userNameLabel.setText("Database Username:");
        
        userNameText = new Text(outerComposite, SWT.BORDER);
        userNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label passwordLabel = new Label(outerComposite, SWT.NONE);
        passwordLabel.setText("Database Password:");
        passwordText = new Text(outerComposite, SWT.BORDER);
        passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label urlLabel = new Label(outerComposite, SWT.NONE);
        urlLabel.setText("Database URL:");
        urlText = new Text(outerComposite, SWT.BORDER);
        urlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        Label driverTextLabel = new Label(outerComposite, SWT.NONE);
        driverTextLabel.setText("JDBC Driver classname:");
        
        driverText = new Text(outerComposite, SWT.BORDER);
        driverText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label platformLabel = new Label(outerComposite, SWT.NONE);
        platformLabel.setText("Database Platform:");
        
        platformText = new Text(outerComposite, SWT.BORDER);
        platformText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        initPreferencesStore();
        
        setDefaults();
        
        return outerComposite;
    }
    
    protected void initPreferencesStore() {
        IScopeContext instanceScope = new InstanceScope();
        ScopedPreferenceStore store = new ScopedPreferenceStore(instanceScope, QUALIFIER);
        setPreferenceStore(store);
    }
    
    protected void setDefaults() {
        userNameText.setText(getPreferenceStore().getString(DB_USERNAME));
        passwordText.setText(getPreferenceStore().getString(DB_PASSWORD));
        urlText.setText(getPreferenceStore().getString(DB_URL));
        driverText.setText(getPreferenceStore().getString(DB_DRIVER));
        platformText.setText(getPreferenceStore().getString(DB_PLATFORM));
    }
    
    public boolean performOk() {
        return true;
    }

}
