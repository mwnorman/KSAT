/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package ca.carleton.tim.ksat.client.dialogs;

//javase imports
import java.io.File;
import java.util.ArrayList;
import java.util.List;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

//KSAT domain imports
import ca.carleton.tim.ksat.client.DriverAdapter;
import ca.carleton.tim.ksat.client.KSATApplication;

/**
 * This class is a <b>heavily</b> re-written version of net.sourceforge.sqlexplorer.dialogs.CreateDriverDlg 
 * 
 * @author mnorman
 *
 */
public class EditJDBCDriverDialog extends TitleAreaDialog {
	
	static final int TEXT_FIELD_WIDTH = 250;

	protected Button jarPathDeleteBtn;
    protected Button jarPathUpBtn;
    protected Button jarPathDownBtn;
    protected Button jarPathNewBtn;
    protected Button listDriversBtn;
    protected Text nameField;
    protected Button jarSearch;
    protected Combo combo;
    protected Text exampleUrlField;
    protected ListViewer jarPathList;
    protected List<File> defaultModel = new ArrayList<File>();
	protected DriverAdapter driver;

    public EditJDBCDriverDialog(Shell parent, DriverAdapter driver) {
		super(parent);
		this.driver = driver;
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
    }
	
	@Override
    protected Control createContents(Composite parent) {

        Control contents = super.createContents(parent);
        setTitle("Edit Driver");
        setMessage("Update the Jar File Paths for the " + driver.getName() + " driver");
        setTitleImage(KSATApplication.IMAGE_REGISTRY.get("editDriver"));
        return contents;
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
        topGroup.setText("Driver");
        data = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        data.widthHint = TEXT_FIELD_WIDTH;
        topGroup.setLayoutData(data);
        layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = 5;
        topGroup.setLayout(layout);
        Label label = new Label(topGroup, SWT.WRAP);
        label.setText("Name");
        nameField = new Text(topGroup, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        data.widthHint = TEXT_FIELD_WIDTH;
        nameField.setLayoutData(data);
        nameField.setText(driver.getName());
        nameField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
            	validate();
            };
            public void keyReleased(KeyEvent e) {
            	validate();
            };
        });
        Label label5 = new Label(topGroup, SWT.WRAP);
        label5.setText("Example URL");
        exampleUrlField = new Text(topGroup, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = TEXT_FIELD_WIDTH;
        data.horizontalSpan = 2;
        exampleUrlField.setLayoutData(data);
        exampleUrlField.setText(driver.getExampleURL());
        exampleUrlField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                validate();
            };
            public void keyReleased(KeyEvent e) {
                validate();
            };
        });
        Composite centralComposite = new Composite(nameGroup, SWT.NONE);
        data = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        data.verticalSpan = 4;
        centralComposite.setLayoutData(data);
        centralComposite.setLayout(new FillLayout());
        TabFolder tabFolder = new TabFolder(centralComposite, SWT.NULL);
        TabItem item1 = new TabItem(tabFolder, SWT.NULL);
        item1.setText("Jar File Paths");
        createJarFilePathsPanel(tabFolder, item1);
        Label label4 = new Label(nameGroup, SWT.WRAP);
        label4.setText("Driver Class Name");
        combo = new Combo(nameGroup, SWT.BORDER | SWT.DROP_DOWN);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = TEXT_FIELD_WIDTH;
        data.horizontalSpan = 2;
        combo.setLayoutData(data);
        combo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent e) {
                validate();
            };
        });
        combo.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                validate();
            };
            public void keyReleased(KeyEvent e) {
                validate();
            };
        });
        nameGroup.layout();
        
        return parentComposite;
    }

	protected void createJarFilePathsPanel(final TabFolder tabFolder, TabItem tabItem) {
		
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(new FillLayout());
        tabItem.setControl(parent);
        Composite cmp = new Composite(parent, SWT.NULL);
        GridLayout grid = new GridLayout();
        grid.numColumns = 2;
        cmp.setLayout(grid);
        jarPathList = new ListViewer(cmp, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData data = new GridData();
        data.grabExcessVerticalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        jarPathList.getControl().setLayoutData(data);
        jarPathList.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
			@SuppressWarnings("unchecked")
			public Object[] getElements(Object inputElement) {
				return ((List<File>)inputElement).toArray();
			}
        });
        jarPathList.setLabelProvider(new LabelProvider(){
			@Override
			public String getText(Object element) {
				return ((File)element).toString();
			}
        });
        jarPathList.setInput(defaultModel);
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
        jarPathNewBtn = new Button(left, SWT.NULL);
        jarPathNewBtn.setText("New Jar Path");
        jarPathNewBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(tabFolder.getShell(), SWT.OPEN);
                dlg.setFilterExtensions(new String[] {"*.jar;*.zip"});
                String str = dlg.open();
                if (str != null) {
                    File obj = new File(str);
                    defaultModel.add(obj);
                    jarPathList.refresh();
                    StructuredSelection sel = new StructuredSelection(obj);
                    jarPathList.setSelection(sel);
                }
            }
        });
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        jarPathNewBtn.setLayoutData(data);
    }

    protected void validate() {
        if ((nameField.getText().trim().length() > 0) && 
        	(exampleUrlField.getText().trim().length() > 0) && 
        	(combo.getText().trim().length() > 0)) {
            setDialogComplete(true);
        }
        else {
            setDialogComplete(false);
        }
    }

    protected void setDialogComplete(boolean value) {
        Button okBtn = getButton(IDialogConstants.OK_ID);
        if (okBtn != null)
            okBtn.setEnabled(value);
    }
}