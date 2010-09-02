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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class JDBCDriversPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	protected TableViewer tableViewer;
	protected Table table;
	protected List<JDBCDriver> drivers = new ArrayList<JDBCDriver>();

	public void init(IWorkbench workbench) {}

	public JDBCDriversPreferencePage() {
        super();
        setPreferenceStore(PlatformUI.getPreferenceStore());
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
        GridData gid = new GridData(GridData.FILL_BOTH);
        gid.grabExcessHorizontalSpace = gid.grabExcessVerticalSpace = true;
        gid.horizontalAlignment = gid.verticalAlignment = GridData.FILL;
        gid.verticalSpan = 6;
        tableViewer = new TableViewer(myComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        tableViewer.getControl().setLayoutData(gid);
        tableViewer.setContentProvider(new DriverContentProvider());
        final DriverLabelProvider dlp = new DriverLabelProvider().setActiveShell(parent.getShell());
        tableViewer.setLabelProvider(dlp);
        tableViewer.getTable().addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e) {
                changeDriver();
            }
        });
        tableViewer.setInput(drivers);
        table = tableViewer.getTable();
        myComposite.layout();
        parent.layout();

        // Add Buttons
        gid = new GridData(GridData.FILL);
        gid.widthHint = 75;
        Button add = new Button(myComposite, SWT.PUSH);
        add.setText("Add");
        add.setLayoutData(gid);
        add.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                JDBCDriverDialog dlg = new JDBCDriverDialog(getShell(), JDBCDriverDialog.Mode.CREATE, null);
                int retCode = dlg.open();
                if (retCode == IDialogConstants.OK_ID) {
                	tableViewer.refresh();
                    select(dlg.getDriver()); 
                }
            }
        });

        gid = new GridData(GridData.FILL);
        gid.widthHint = 75;
        Button edit = new Button(myComposite, SWT.PUSH);
        edit.setText("Edit");
        edit.setLayoutData(gid);
        edit.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                changeDriver();
                tableViewer.refresh();
            }
        });

        gid = new GridData(GridData.FILL);
        gid.widthHint = 75;
        Button copy = new Button(myComposite, SWT.PUSH);
        copy.setText("Copy");
        copy.setLayoutData(gid);
        copy.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                StructuredSelection sel = (StructuredSelection)tableViewer.getSelection();
                JDBCDriver dv = (JDBCDriver) sel.getFirstElement();
                if (dv != null) {
                    JDBCDriverDialog dlg = new JDBCDriverDialog(getShell(), JDBCDriverDialog.Mode.COPY, dv);
                    int retCode = dlg.open();
                    if (retCode == IDialogConstants.OK_ID) {
                    	tableViewer.refresh();
                    	// select the new driver
                        select(dlg.getDriver()); 
                    }
                }
            }
        });

        gid = new GridData(GridData.FILL);
        gid.widthHint = 75;
        Button remove = new Button(myComposite, SWT.PUSH);
        remove.setText("Remove");
        remove.setLayoutData(gid);
        remove.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	TableItem[] selection = tableViewer.getTable().getSelection();
            	if (selection != null && selection.length > 0) {
	                boolean okToDelete = MessageDialog.openConfirm(getShell(),
	                	"Delete Driver", "Are you sure you want to delete driver <" + 
	                		selection[0].getText() + ">?");
	                if (okToDelete) {
	                    StructuredSelection sel = (StructuredSelection)tableViewer.getSelection();
	                    JDBCDriver dv = (JDBCDriver)sel.getFirstElement();
	                    if (dv != null) {
	                        drivers.remove(dv);
	                        tableViewer.refresh();
	                        selectFirst();
	                    }
	                }
            	}
            }
        });
		return myComposite;
	}

    protected void changeDriver() {
        StructuredSelection sel = (StructuredSelection)tableViewer.getSelection();
        JDBCDriver dv = (JDBCDriver)sel.getFirstElement();
        if (dv != null) {
            JDBCDriverDialog dlg = new JDBCDriverDialog(getShell(), JDBCDriverDialog.Mode.MODIFY, dv);
            int retCode = dlg.open();
            if (retCode == IDialogConstants.OK_ID) {
            	tableViewer.refresh();
            	select(dv);
            }
        }
    }
    
    protected void selectFirst() {
    	if (tableViewer.getTable().getItemCount() > 0) {
    		tableViewer.getTable().select(0);
    	}
    }

    protected void select(JDBCDriver managedDriver) {
    	if (drivers.contains(managedDriver)) {
    		StructuredSelection sel = new StructuredSelection(managedDriver);
            tableViewer.setSelection(sel);
    	}
    }
	
	class DriverContentProvider implements IStructuredContentProvider {
	    @SuppressWarnings("unchecked")
		public Object[] getElements(Object input) {
	    	ArrayList<JDBCDriver> drivers = new ArrayList<JDBCDriver>();
	    	drivers.addAll((Collection<? extends JDBCDriver>)input);
	    	Collections.sort(drivers, new Comparator<JDBCDriver>() {
				public int compare(JDBCDriver left, JDBCDriver right) {
					return left.getName().compareTo(right.getName());
				}
	    	});
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
	        JDBCDriver dv = (JDBCDriver)element;
	        if (dv.isOk() == true) {
	            return KSATApplication.IMAGE_REGISTRY.get("okDriver");
	        }
	        else {
	            return KSATApplication.IMAGE_REGISTRY.get("errorDriver"); 
	        }
	    }
	    public String getColumnText(Object element, int i) {
	        JDBCDriver dv = (JDBCDriver)element;
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