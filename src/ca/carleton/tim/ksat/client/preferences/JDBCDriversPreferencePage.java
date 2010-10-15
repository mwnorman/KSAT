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
package ca.carleton.tim.ksat.client.preferences;

//javase imports
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Collection;

//Graphics (SWT/JFaces) imports
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

//KSAT imports
import ca.carleton.tim.ksat.client.DriverAdapter;
import ca.carleton.tim.ksat.client.KSATApplication;
import ca.carleton.tim.ksat.client.dialogs.EditJDBCDriverDialog;
import ca.carleton.tim.ksat.persist.DriverAdapterProject;
import ca.carleton.tim.ksat.persist.Drivers;
import static ca.carleton.tim.ksat.client.DriverAdapter.DRIVER_REGISTRY;

/**
 * This class is 'influenced by' net.sourceforge.sqlexplorer.preferences.DriverPreferencePage - but
 * is basically a 100% re-write. I've re-used the icons, so I've added the LGPL licence file.
 * 
 * @author mnorman
 *
 */
public class JDBCDriversPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	protected TableViewer tableViewer;
	protected Table table;

	public void init(IWorkbench workbench) {}

	public JDBCDriversPreferencePage() {
        super();
	}

	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
        GridLayout parentLayout = new GridLayout(1, false);
        parentLayout.marginTop = parentLayout.marginBottom = 0;
        parentLayout.marginHeight = 0;
        parentLayout.verticalSpacing = 10;
        parent.setLayout(parentLayout);
        GridLayout layout;
        Composite myComposite = new Composite(parent, SWT.NONE);
        // Define layout.
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = layout.marginHeight = 0;
        layout.horizontalSpacing = 20;
        layout.verticalSpacing = 10;
        myComposite.setLayout(layout);
        myComposite.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, true));
        GridData grid = new GridData(GridData.FILL_BOTH);
        grid.grabExcessHorizontalSpace = grid.grabExcessVerticalSpace = true;
        grid.horizontalAlignment = grid.verticalAlignment = GridData.FILL;
        grid.verticalSpan = 6;
        tableViewer = new TableViewer(myComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        tableViewer.getControl().setLayoutData(grid);
        tableViewer.setContentProvider(new DriverContentProvider());
        final DriverLabelProvider dlp = new DriverLabelProvider().setActiveShell(parent.getShell());
        tableViewer.setLabelProvider(dlp);
        tableViewer.getTable().addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e) {
            	selectDriver();
            }
        });
        tableViewer.setInput(DRIVER_REGISTRY);
        tableViewer.refresh();
        table = tableViewer.getTable();
        myComposite.layout();
        parent.layout();

        // Edit Button
        grid = new GridData(GridData.FILL);
        grid.widthHint = 75;
        Button edit = new Button(myComposite, SWT.PUSH);
        edit.setText("Edit");
        edit.setLayoutData(grid);
        edit.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                selectDriver();
                tableViewer.refresh();
            }
        });
		return myComposite;
	}

    protected void selectDriver() {
        StructuredSelection sel = (StructuredSelection)tableViewer.getSelection();
        DriverAdapter dv = (DriverAdapter)sel.getFirstElement();
        if (dv != null) {
            EditJDBCDriverDialog dlg = new EditJDBCDriverDialog(getShell(), dv);
            int retCode = dlg.open();
            if (retCode == IDialogConstants.OK_ID) {
            	dv.reset();
            	dv.isOk();
            	select(dv);
            	tableViewer.refresh();
        		XMLContext xc = new XMLContext(new DriverAdapterProject());
                Location instanceLocation = Platform.getInstanceLocation();
                URL fileURL = null;
        		try {
        			fileURL = FileLocator.toFileURL(instanceLocation.getURL());
        	        File instanceFile = new File(fileURL.toURI());
        	        File driversFile = new File(instanceFile, "drivers.xml");
        	        driversFile.setWritable(true);
        	        XMLMarshaller marshaller = xc.createMarshaller();
        	        Drivers drvs = new Drivers();
        	        drvs.setDrivers(DRIVER_REGISTRY);
        	        marshaller.marshal(drvs, new FileOutputStream(driversFile));
	        	}
	        	catch (Exception e) {
	        		e.printStackTrace();
	        	}
            }
        }
    }
    
    protected void selectFirst() {
    	if (tableViewer.getTable().getItemCount() > 0) {
    		tableViewer.getTable().select(0);
    	}
    }

    protected void select(DriverAdapter driverAdapter) {
    	if (DRIVER_REGISTRY.containsValue(driverAdapter)) {
    		StructuredSelection sel = new StructuredSelection(driverAdapter);
            tableViewer.setSelection(sel);
    	}
    }
	
	class DriverContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object input) {
	    	Collection<DriverAdapter> drivers = DRIVER_REGISTRY.values();
	        return drivers.toArray();
	    }
	    public void dispose() {}
	    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}
	
	class DriverLabelProvider extends LabelProvider implements ITableLabelProvider {
		Shell activeShell;
	    DriverLabelProvider() {
	    };
	    public Image getColumnImage(Object element, int i) {
	        DriverAdapter dv = (DriverAdapter)element;
	        if (dv.isOk() == true) {
	            return KSATApplication.IMAGE_REGISTRY.get("okDriver");
	        }
	        else {
	            return KSATApplication.IMAGE_REGISTRY.get("errorDriver"); 
	        }
	    }
	    public String getColumnText(Object element, int i) {
	        DriverAdapter dv = (DriverAdapter)element;
	        return dv.getName();
	    }
	    public boolean isLabelProperty(Object element, String property) {
	        return true;
	    }
	    public void removeListener(ILabelProviderListener listener) {}
	    public void addListener(ILabelProviderListener listener) {}
	    public DriverLabelProvider setActiveShell(Shell activeShell) {
	    	this.activeShell = activeShell;
	    	return this;
	    }
	}

}