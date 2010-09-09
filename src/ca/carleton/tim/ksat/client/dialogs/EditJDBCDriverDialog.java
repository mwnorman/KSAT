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
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.swt.widgets.Text;

//EclipseLink imports
import org.eclipse.persistence.internal.helper.NonSynchronizedVector;
import org.eclipse.persistence.internal.libraries.asm.ClassAdapter;
import org.eclipse.persistence.internal.libraries.asm.ClassReader;
import org.eclipse.persistence.internal.libraries.asm.ClassVisitor;

//KSAT domain imports
import ca.carleton.tim.ksat.client.DriverAdapter;
import ca.carleton.tim.ksat.client.KSATApplication;
import ca.carleton.tim.ksat.utils.EmptyVisitor;

/**
 * This class is 'influenced by' net.sourceforge.sqlexplorer.dialogs.CreateDriverDlg - but
 * is basically a 100% re-write. I've re-used the icons, so I've added the LGPL licence file.
 * 
 * @author mnorman
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditJDBCDriverDialog extends TitleAreaDialog {

	static final String DRIVERS_META_INF = "META-INF/services/java.sql.Driver";
	static final int UP = -1;
	static final int DOWN = 1;
	static final int WIDTH_HINT = 250;
	static final int MARGIN_WIDTH = 10;

	protected Shell shell; 
    protected ListViewer pathsListViewer;
	protected Vector<String> paths = new NonSynchronizedVector(); // local collection of paths
	protected boolean changed = false;
	protected Combo driverClassCombo;
	protected Text driverClassField;
	protected Button upBtn;
	protected Button downBtn;
	protected Button deleteBtn;
	protected DriverAdapter driver;

	public EditJDBCDriverDialog(Shell parent, DriverAdapter driver) {
		super(parent);
		this.driver = driver;
		paths.addAll(driver.getJarPaths());
	}

	@Override
	protected Point getInitialSize() {
		Point shellSize = super.getInitialSize();
		shellSize.y += WIDTH_HINT/5; // stretch dialog a bit
		return shellSize;
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
        layout.marginWidth = MARGIN_WIDTH;
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
        data.widthHint = WIDTH_HINT;
        topGroup.setLayoutData(data);
        layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = MARGIN_WIDTH;
        topGroup.setLayout(layout);

        Label label = new Label(topGroup, SWT.WRAP);
        label.setText("Driver Class");
        driverClassField = new Text(topGroup, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        data.widthHint = WIDTH_HINT;
        driverClassField.setText(driver.getDriverClass());
        driverClassField.setLayoutData(data);
        Label label5 = new Label(topGroup, SWT.WRAP);
        label5.setText("Example URL");
        Text exampleUrlField = new Text(topGroup, SWT.READ_ONLY | SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = WIDTH_HINT;
        data.horizontalSpan = 2;
        exampleUrlField.setText(driver.getExampleURL());
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
        pathsListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				computeButtonsEnabled();
			}
		});

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
            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(shell, SWT.OPEN);
                dlg.setFilterExtensions(new String[] {"*.jar;*.zip"});
                String str = dlg.open();
                if (str != null) {
                	paths.add(str);
                	changed = true;
                	pathsListViewer.refresh();
                    StructuredSelection sel = new StructuredSelection(str);
                    pathsListViewer.setSelection(sel);
                    computeButtonsEnabled();
                }
            }
        });
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        newBtn.setLayoutData(data);

        upBtn = new Button(left, SWT.NULL);
        upBtn.setText("Up");
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        upBtn.setLayoutData(data);
        upBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				moveJarPath(UP);
			}
		});

        downBtn = new Button(left, SWT.NULL);
        downBtn.setText("Down");
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        downBtn.setLayoutData(data);
        downBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				moveJarPath(DOWN);
			}
		});

        deleteBtn = new Button(left, SWT.NULL);
        deleteBtn.setText("Delete");
        deleteBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	IStructuredSelection selection = (IStructuredSelection)pathsListViewer.getSelection();
            	String s = (String)selection.getFirstElement();
                if (s != null) {
                	paths.remove(s);
                	computeButtonsEnabled();
                	changed = true;
                	pathsListViewer.refresh();
                }
            }
        });
        
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        deleteBtn.setLayoutData(data);
        
        driverClassCombo = new Combo(cmp, SWT.BORDER | SWT.DROP_DOWN);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = WIDTH_HINT;
        data.horizontalSpan = 1;
        driverClassCombo.setLayoutData(data);
        driverClassCombo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent event) {
            }
            public void widgetSelected(SelectionEvent event) {
                int idx = driverClassCombo.getSelectionIndex();
                if (idx > -1) {
                	String selectedDriver = driverClassCombo.getItem(idx);
                	driver.setDriverClass(selectedDriver);
                	driverClassField.setText(selectedDriver);
                }
            };
        });
        driverClassCombo.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
			}
			public void mouseDoubleClick(MouseEvent e) {
                int idx = driverClassCombo.getSelectionIndex();
                if (idx > -1) {
                	String selectedDriver = driverClassCombo.getItem(idx);
                	driver.setDriverClass(selectedDriver);
                	driverClassField.setText(selectedDriver);
                }
			}
		});
        if (driver.isOk()) {
        	driverClassCombo.setItems(new String[]{driver.getDriverClass()});
        }
        Button testJarsButton = new Button(left, SWT.NULL);
        testJarsButton.setText("Test Jars");
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        testJarsButton.setLayoutData(data);
        testJarsButton.addSelectionListener(new SelectionAdapter() {
        	Combo driverClassCombo;
            public void widgetSelected(SelectionEvent event) {
            	List<String> drivers = new ArrayList<String>();
            	for (String jarFileName : paths) {
    		        File file = null;
    		        if (jarFileName != null && jarFileName.length() >0) {
    		            try {
    		            	file = new File(jarFileName);
    		            	JarFile jarFile = new JarFile(file);
		            		// try JDBC 3.0: look in META-INF/services/java.sql.Driver file for driverClass
    		            	JarEntry driverEntry = jarFile.getJarEntry(DRIVERS_META_INF);
							if (driverEntry != null) {
					            BufferedReader zipReader = new BufferedReader(
					                 new InputStreamReader(jarFile.getInputStream(driverEntry)));
					            if (zipReader.ready()) {
					            	drivers.add(zipReader.readLine().trim());
						            zipReader.close();
					            }
							}
							else {
								// jar is old - look for classes that implement java.sql.Driver interface
								for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				            		JarEntry entry = entries.nextElement();
				            		if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
					            		InputStream is = jarFile.getInputStream(entry);
					            		DriverClassAdapter dca = new DriverClassAdapter(null);
					            		new ClassReader(is).accept(dca, true);
					            		if (dca.getDriverClass() != null) {
					            			drivers.add(dca.getDriverClass().replace('/', '.'));
					            		}
				            		}
								}
							}
    					}
    		            catch (Exception e) {
    		            	// ignore
    		            }
    		        }
            	}
            	if (drivers.size() > 0) {
            		driverClassCombo.setItems(drivers.toArray(new String[drivers.size()]));
            		driverClassCombo.select(0);
            	}
            }
            public SelectionAdapter setCombo(Combo driverClassCombo) {
            	this.driverClassCombo = driverClassCombo;
            	return this;
            }
        }.setCombo(driverClassCombo));
        computeButtonsEnabled();
        
        nameGroup.layout(true, true);
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
	
	protected void moveJarPath(int offset) {
		org.eclipse.swt.widgets.List list = pathsListViewer.getList();
		int idx = list.getSelectionIndex();
        if (idx >= 0) {
            int target = idx + offset;
            String[] selection = list.getSelection();
            list.remove(idx);
            list.add(selection[0], target);
            list.setSelection(target);
            computeButtonsEnabled();
        }
	}
	
	protected void computeButtonsEnabled() {
		org.eclipse.swt.widgets.List list = pathsListViewer.getList();
        int index = list.getSelectionIndex();
        int size = list.getItemCount();
	    deleteBtn.setEnabled(index >= 0);
        upBtn.setEnabled(size > 1 && index > 0);
        downBtn.setEnabled(size > 1 && index >= 0 && index < size - 1);
    }

	class DriverClassAdapter extends ClassAdapter {
		String driverClass = null;
		public DriverClassAdapter(ClassVisitor cv) {
			super(new EmptyVisitor());
		}
		public String getDriverClass() {
			return driverClass;
		}
		@Override
		public void visit(int version, int access, String name,
			String superName, String[] interfaces, String sourceFile) {
			if (interfaces != null && interfaces.length > 0) {
				for (String intf : interfaces) {
					if (Driver.class.getName().replace('.', '/').equals(intf)) {
						driverClass = name;
						break;
					}
				}
			}
			super.visit(version, access, name, superName, interfaces, sourceFile);
		}
	}
}