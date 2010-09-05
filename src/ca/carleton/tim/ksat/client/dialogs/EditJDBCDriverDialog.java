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

//javase imports
import java.util.Vector;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.persistence.internal.helper.NonSynchronizedVector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

//KSAT domain imports
import ca.carleton.tim.ksat.client.DriverAdapter;
import ca.carleton.tim.ksat.client.KSATApplication;

/**
 * This class is 'influenced by' net.sourceforge.sqlexplorer.dialogs.CreateDriverDlg - but
 * is basically a 100% re-write. I've re-used the icons, so I've added the LGPL licence file.
 * 
 * @author mnorman
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditJDBCDriverDialog extends TitleAreaDialog {

	protected Shell shell; 
    protected ListViewer pathsListViewer;
	protected Vector paths = new NonSynchronizedVector(); // local collection of paths
	protected boolean changed = false;
	protected DriverAdapter driver;

	public EditJDBCDriverDialog(Shell parent, DriverAdapter driver) {
		super(parent);
		this.driver = driver;
		paths.addAll(driver.getJarPaths());
	}

	public DriverAdapter getDriver() {
		return null;
	}

	@Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.RESIZE | getShellStyle());
    }

	@Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Edit Driver");
        this.shell = shell;
    }
	
	@Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle("Edit Driver Info");
        setMessage("Edit Driver Info for the " + driver.getName() + " driver");
        setTitleImage(KSATApplication.IMAGE_REGISTRY.get("editDriver"));
        return contents;
	}

	protected void updateButtons(boolean isValid) {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(isValid);
		}
	}

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite parentComposite = (Composite) super.createDialogArea(parent);
        
        Composite composite = new Composite(parentComposite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setFont(parentComposite.getFont());
        
        Composite nameGroup = new Composite(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = 10;
        nameGroup.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        nameGroup.setLayoutData(data);

        Composite topComposite = new Composite(nameGroup, SWT.NONE);
        data = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        topComposite.setLayoutData(data);
        topComposite.setLayout(new GridLayout());

        Group topGroup = new Group(topComposite, SWT.NULL);
        topGroup.setText("Driver Info");
        data = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        data.widthHint = 250;
        topGroup.setLayoutData(data);
        layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = 5;
        topGroup.setLayout(layout);

        Label label = new Label(topGroup, SWT.WRAP);
        label.setText("Driver Class");
        Text nameField = new Text(topGroup, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        data.widthHint = 250;
        nameField.setText(driver.getDriverClass());
        nameField.setLayoutData(data);
        Label label5 = new Label(topGroup, SWT.WRAP);
        label5.setText("Example URL");
        Text exampleUrlField = new Text(topGroup, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = 250;
        data.horizontalSpan = 2;
        exampleUrlField.setText(driver.getExampleURL());
        exampleUrlField.setEnabled(false);
        exampleUrlField.setLayoutData(data);

        Composite centralComposite = new Composite(nameGroup, SWT.NONE);
        data = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        data.verticalSpan = 4;
        centralComposite.setLayoutData(data);
        centralComposite.setLayout(new FillLayout());
        
        Composite cmp = new Composite(centralComposite, SWT.NULL);
        GridLayout grid = new GridLayout();
        grid.numColumns = 2;
        cmp.setLayout(grid);
        pathsListViewer = new ListViewer(cmp, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        data = new GridData();
        data.grabExcessVerticalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        pathsListViewer.getControl().setLayoutData(data);
        pathsListViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				Vector v = (Vector)inputElement;
				return v.toArray();
			}
		});
        pathsListViewer.setLabelProvider(new LabelProvider() {
        	public String getText(Object element) {
                return element.toString();
            }
        });
        pathsListViewer.setInput(paths);

        Composite left = new Composite(cmp, SWT.NULL);
        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessVerticalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        left.setLayoutData(data);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        left.setLayout(gridLayout);
        Button newBtn = new Button(left, SWT.NULL);
        newBtn.setText("New Jar Path...");
        newBtn.addSelectionListener(new SelectionAdapter() {
        	ListViewer pathsListViewer;
            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(shell, SWT.OPEN);
                dlg.setFilterExtensions(new String[] {"*.jar;*.zip"}); //$NON-NLS-1$
                String str = dlg.open();
                if (str != null) {
                	paths.add(str);
                	changed = true;
                	pathsListViewer.refresh();
                    StructuredSelection sel = new StructuredSelection(str);
                    pathsListViewer.setSelection(sel);
                }
            }
            public SelectionAdapter setViewer(ListViewer pathsListViewer) {
            	this.pathsListViewer = pathsListViewer;
            	return this;
            }
        }.setViewer(pathsListViewer));
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        newBtn.setLayoutData(data);

        //TODO - figure out what to do
        Button upBtn = new Button(left, SWT.NULL);
        upBtn.setText("Up");
        upBtn.setEnabled(false);
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        upBtn.setLayoutData(data);

        //TODO - figure out what to do
        Button downBtn = new Button(left, SWT.NULL);
        downBtn.setText("Down");
        downBtn.setEnabled(false);
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        downBtn.setLayoutData(data);

        Button deleteBtn = new Button(left, SWT.NULL);
        deleteBtn.setText("Delete");
        deleteBtn.setEnabled(false);
        deleteBtn.addSelectionListener(new SelectionAdapter() {
        	ListViewer pathsListViewer;
            public void widgetSelected(SelectionEvent event) {
            	IStructuredSelection selection = (IStructuredSelection)pathsListViewer.getSelection();
            	String s = (String)selection.getFirstElement();
                if (s != null) {
                	paths.remove(s);
                	changed = true;
                	pathsListViewer.refresh();
                }
            }
            public SelectionAdapter setViewer(ListViewer pathsListViewer) {
            	this.pathsListViewer = pathsListViewer;
            	return this;
            }
        }.setViewer(pathsListViewer));
        
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        deleteBtn.setLayoutData(data);
        pathsListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            Button deleteBtn;
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                String s = (String)selection.getFirstElement();
                if (s != null) {
                    deleteBtn.setEnabled(true);
                }
                else {
                    deleteBtn.setEnabled(false);
                }
            }
            public ISelectionChangedListener setButton(Button deleteBtn) {
            	this.deleteBtn = deleteBtn;
            	return this;
            }
        }.setButton(deleteBtn));
        
        
        nameGroup.layout();
		return parentComposite;
    }

	@Override
	protected void okPressed() {
		super.okPressed();
		if (changed) {
			driver.getJarPaths().clear();
			driver.reset();
			for (String jarPath : (Vector<String>)paths) {
				driver.getJarPaths().add(jarPath);
			}
		}
	}

}