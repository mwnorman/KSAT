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
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

//EclipseLink imports
import org.eclipse.persistence.internal.libraries.asm.ClassAdapter;
import org.eclipse.persistence.internal.libraries.asm.ClassReader;
import org.eclipse.persistence.internal.libraries.asm.ClassVisitor;

//KSAT domain imports
import ca.carleton.tim.ksat.client.DriverAdapter;
import ca.carleton.tim.ksat.client.KSATApplication;
import ca.carleton.tim.ksat.utils.EmptyVisitor;
import ca.carleton.tim.ksat.utils.FileUtil;

/**
 * This class is 'influenced by' net.sourceforge.sqlexplorer.dialogs.CreateDriverDlg - but
 * is basically a 100% re-write. However, since I've re-used the icons, I need to include
 * the LGPL licence file.
 * 
 * @author mnorman
 *
 */
public class EditJDBCDriverDialog extends TitleAreaDialog {

	static final String DRIVERS_META_INF = "META-INF/services/java.sql.Driver";
	static final int UP = -1;
	static final int DOWN = 1;
	static final int WIDTH_HINT = 80;

	protected Shell shell;
	protected Table exampleURLsTable;
    protected ListViewer pathsListViewer;
    protected ArrayList<String> paths = new ArrayList<String>();
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
	public boolean isHelpAvailable() {
		return false;
	}
	
	@Override
	protected Point getInitialSize() {
		Point shellSize = super.getInitialSize();
		shellSize.x += 10; // stretch dialog a bit to accomodate table and list 
		shellSize.y += WIDTH_HINT;
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
		Composite parentComposite = (Composite)super.createDialogArea(parent);
		Composite outerContainer = new Composite(parentComposite, SWT.NONE);
		outerContainer.setLayout(new FormLayout());
		outerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		outerContainer.setFont(parentComposite.getFont());
		
		final Composite innerContainer = new Composite(outerContainer, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		innerContainer.setLayout(gridLayout);
		final FormData formData = new FormData();
		formData.left = new FormAttachment(0);
		formData.bottom = new FormAttachment(100, 0);
		formData.right = new FormAttachment(100, -8);
		formData.top = new FormAttachment(0, 4);
		innerContainer.setLayoutData(formData);
		
		Label driverClassLabel = new Label(innerContainer, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = WIDTH_HINT;
		driverClassLabel.setLayoutData(gridData);
		driverClassLabel.setText("Driver Class");
		driverClassField = new Text(innerContainer, SWT.BORDER);
		driverClassField.setText(driver.getDriverClass());
		driverClassField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        
        Group exampleUrlsGroup = new Group(innerContainer, SWT.NONE);
        exampleUrlsGroup.setText("Example URLs");
        exampleUrlsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1));
        exampleUrlsGroup.setLayout(new GridLayout(3, false));
        
        TableViewer tableViewer = new TableViewer(exampleUrlsGroup, SWT.V_SCROLL | SWT.H_SCROLL | 
            SWT.BORDER);
        exampleURLsTable = tableViewer.getTable();
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        gridData.widthHint = WIDTH_HINT * 4;
        exampleURLsTable.setLayoutData(gridData);
        exampleURLsTable.setLinesVisible(true);
        exampleURLsTable.setHeaderVisible(true);
        TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn nameColumn = tableViewerColumn.getColumn();
        nameColumn.setResizable(false);
        nameColumn.setWidth(WIDTH_HINT);
        nameColumn.setText("Name");
        EditingSupport nullEditingSupport = new EditingSupport(tableViewer) {
			protected void setValue(Object element, Object value) {
			}
			protected Object getValue(Object element) {
				return null;
			}
			protected CellEditor getCellEditor(Object element) {
				return null;
			}
			protected boolean canEdit(Object element) {
				return false;
			}
		};
		tableViewerColumn.setEditingSupport(nullEditingSupport);
        TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn urlColumn = tableViewerColumn_1.getColumn();
        urlColumn.setResizable(false);
        urlColumn.setWidth(WIDTH_HINT * 4);
        urlColumn.setText("URL");
        tableViewerColumn_1.setEditingSupport(nullEditingSupport);
        for (Map.Entry<String,String> me :driver.getExampleURLs().entrySet()) {
            TableItem tableItem = new TableItem(exampleURLsTable, SWT.NULL);
            tableItem.setText(new String[] {me.getKey(), me.getValue()});
        }
        nameColumn.pack();
        urlColumn.pack();

        Group pathsGroup = new Group(innerContainer, SWT.NONE);
        pathsGroup.setText("Paths to JDBC Driver Jar(s)");
        pathsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1));
        pathsGroup.setLayout(new GridLayout(3, false));
        
        pathsListViewer = new ListViewer(pathsGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        pathsListViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
        pathsListViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return paths.toArray();
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
				
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
                
        Button addBtn = new Button(pathsGroup, SWT.NULL);
        gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gridData.widthHint = WIDTH_HINT;
        addBtn.setLayoutData(gridData);
        addBtn.setText("Add Jars");
        addBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(shell, SWT.MULTI | SWT.OPEN);
                dlg.setFilterExtensions(new String[] {"*.jar;*.zip"});
                dlg.open();
                String[] fileNames = dlg.getFileNames();
                if (fileNames != null) {
                	String path = FileUtil.normalize(dlg.getFilterPath());
                	for (String fileName : fileNames) {
                		paths.add(path + "/" + fileName);
                	}
                	changed = true;
                    computeButtonsEnabled();
                }
            }
        });
        
        upBtn = new Button(pathsGroup, SWT.NULL);
        gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gridData.widthHint = WIDTH_HINT;
        upBtn.setLayoutData(gridData);
        upBtn.setText("Up");
        upBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				shiftJarPath(UP);
			}
		});
        
        downBtn = new Button(pathsGroup, SWT.NULL);
        gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gridData.widthHint = WIDTH_HINT;
        downBtn.setLayoutData(gridData);
        downBtn.setText("Down");
        downBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				shiftJarPath(DOWN);
			}
		});
        
        deleteBtn = new Button(pathsGroup, SWT.NULL);
        gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gridData.widthHint = WIDTH_HINT;
        deleteBtn.setLayoutData(gridData);
        deleteBtn.setText("Delete");
        deleteBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	IStructuredSelection selection = (IStructuredSelection)pathsListViewer.getSelection();
            	if (!selection.isEmpty()) {
        	String pathToRemove = (String)selection.getFirstElement();
        	paths.remove(pathToRemove);
            changed = true;
            computeButtonsEnabled();
            	}
            }
        });
        
        driverClassCombo = new Combo(pathsGroup, SWT.BORDER | SWT.DROP_DOWN);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = false;
        gridData.widthHint = WIDTH_HINT;
        gridData.horizontalSpan = 2;
        driverClassCombo.setLayoutData(gridData);
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
        
        Button testJarsButton = new Button(pathsGroup, SWT.NULL);
        testJarsButton.setText("Test Jars");
        gridData = new GridData();
        gridData.widthHint = WIDTH_HINT;
        gridData.horizontalAlignment = SWT.FILL;
        testJarsButton.setLayoutData(gridData);
        testJarsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	ArrayList<String> drivers = new ArrayList<String>();
            	for (String jarFileName : pathsListViewer.getList().getItems()) {
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
        });
        computeButtonsEnabled();
		return parentComposite;
    }

	@Override
	protected void okPressed() {
		super.okPressed();
		if (changed) {
			driver.getJarPaths().clear();
			driver.reset();
			for (String jarPath : paths) {
				driver.getJarPaths().add(jarPath);
			}
		}
	}
	
	protected void shiftJarPath(int direction) {
		List list = pathsListViewer.getList();      
		int count = list.getItemCount();
		int index = list.getSelectionIndex();
		if ((index ==0 && direction == UP) || (index == count-1 && direction == DOWN)){
		    return; //do not shift
		}
		else{
		    reSortList(index, direction);
		    list.setFocus();
		    list.select(index + direction);
		}
		computeButtonsEnabled();
	}
	
	protected void reSortList(int index, int direction) {
	    String savedPath = paths.get(index);
	    paths.remove(index);
	    paths.add(index + direction, savedPath);
	    pathsListViewer.setInput(paths.toArray());
	}
	
	protected void computeButtonsEnabled() {
		List list = pathsListViewer.getList();
		GridData gd_list = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 4);
		gd_list.widthHint = 317;
		list.setLayoutData(gd_list);
        int index = list.getSelectionIndex();
        int size = list.getItemCount();
	    deleteBtn.setEnabled(index >= 0);
        upBtn.setEnabled(size > 1 && index > 0);
        downBtn.setEnabled(size > 1 && index >= 0 && index < size - 1);
        pathsListViewer.refresh();
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