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
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class JDBCDriversPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	static final String MANIFEST_JAR_LOCATION = "/META-INF/MANIFEST.MF";
	static final String[] COLUMN_HEADERS = {"JDBC Driver Class", "Driver Jar(s)" };

	protected int BUTTON_WIDTH = 100;
	protected TableViewer tableViewer;
	protected Table table;
	protected Button addButton;
	protected Button removeButton;

	protected ColumnLayoutData[] columnLayouts = {new ColumnWeightData(1), new ColumnWeightData(1)};

	public void init(IWorkbench workbench) {}

	public JDBCDriversPreferencePage() {
        super();
        setPreferenceStore(PlatformUI.getPreferenceStore());
	}

	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setFont(font);
		createTable(composite);
		createButtons(composite);
		return composite;
	}

	private void createTable(Composite parent) {
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 175;
		gridData.widthHint = 450;
		composite.setLayout(layout);
		composite.setLayoutData(gridData);
		composite.setFont(font);
		table = new Table(composite, SWT.CHECK | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | 
			SWT.V_SCROLL | SWT.BORDER);
		tableViewer = new TableViewer(table);
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(font);
		gridData = new GridData(GridData.FILL_BOTH);
		tableViewer.getControl().setLayoutData(gridData);
		for (int i = 0; i < COLUMN_HEADERS.length; i++) {
			tableLayout.addColumnData(columnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(columnLayouts[i].resizable);
			tc.setText(COLUMN_HEADERS[i]);
		}
        tableViewer.setLabelProvider(new ITableLabelProvider() {
            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }
            public String getColumnText(Object element, int columnIndex) {
            	if (element instanceof DriverInfo) {
					DriverInfo info = (DriverInfo) element;
					if (columnIndex == 0) { 
						return info.driverClass;
					}
					else {
						int len = info.jarFilePaths.size();
						String tmp = "";
						if (len > 1) {
							for (int i = 0; i < len; i++) {
								String s = info.jarFilePaths.get(i);
								tmp += s;
								if (i < len -1) {
									tmp += ";";
								}
							}
						}
						else {
							tmp = info.jarFilePaths.get(0);
						}
						return tmp;
					}
				}
            	return "";
            }
            public void addListener(ILabelProviderListener listener) {
            }
            public void dispose() {
            }
            public boolean isLabelProperty(Object element, String property) {
                return false;
            }
            public void removeListener(ILabelProviderListener listener) {
            }            
        });
	}

	private void createButtons(final Composite parent) {
		// Create button composite
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout glayout = new GridLayout();
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		glayout.numColumns = 1;
		GridData gdata = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		buttonComposite.setLayout(glayout);
		buttonComposite.setLayoutData(gdata);
		buttonComposite.setFont(parent.getFont());
		// Create buttons
		addButton = KSATApplication.createButton(parent, 1, "Add", false); 
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
		        Shell activeShell = parent.getShell();
		        FileDialog fileDialog = new FileDialog(activeShell, SWT.OPEN);
		        fileDialog.setFilterExtensions(new String[]{"*.jar", "*.zip", "*.*"});
		        String jarFileName = fileDialog.open();
		        File file = null;
		        String driverClass = "";
		        if (jarFileName != null && jarFileName.length() >0) {
	                try {
	                	file = new File(jarFileName);
	                	JarFile jarFile = new JarFile(file);
	                	URLClassLoader clazzLoader = URLClassLoader.newInstance(
	                		new URL[]{file.toURI().toURL()});
	                	for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
	                		JarEntry entry = entries.nextElement();
	                		String name = entry.getName();
	                		if (name.endsWith(".class")) {
	                			String cname = name.replaceFirst(".class", "").replaceAll("/", ".");
	                			Class<?> clazz = clazzLoader.loadClass(cname);
                				if (Driver.class.isAssignableFrom(clazz) && !Driver.class.equals(clazz)) {
                					driverClass = cname;
                					break;
                				}
	                		}
	                	}
	                }
	                catch (Exception e) {
	                    //e.printStackTrace();
	                    return;
	                }
		        }
		        if (!"".equals(driverClass)) {
		        	DriverInfo info = new DriverInfo();
		        	info.driverClass = driverClass;
		        	String jarFilePath = null;
		        	try {
		        		jarFilePath = file.toURI().getPath().substring(1);
					}
		        	catch (Exception e) {
		        		jarFilePath = jarFileName;
					}
					info.jarFilePaths.add(jarFilePath);
		        	tableViewer.add(info);
		            table.setTopIndex(table.getItemCount());
		        }
			}
		});
		removeButton = KSATApplication.createButton(parent, 1, "Remove", false); 
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Shell activeShell = parent.getShell();
				MessageDialog.openWarning(activeShell, "Remove", "Remove not yet implemented");
			}
		});
	}

}