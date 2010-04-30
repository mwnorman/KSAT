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
import java.util.List;

//Graphics (JFaces/SWT) imports
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

//RCP imports
import org.eclipse.ui.part.ViewPart;

//EclipseLink imports
import org.eclipse.persistence.sessions.DatabaseSession;

//KSAT domain imports
import ca.carleton.tim.ksat.model.Site;

public class SitesView extends ViewPart {

    public static final String ID = "ca.carleton.tim.ksat.client.views.sites";
    public static final String[] SITE_COLUMN_HEADINGS = {"Url", "Description"};
    
    protected TableViewer tableViewer;
    protected Table table;
    
    public SitesView() {
    }

    public TableViewer getTableViewer() {
        return tableViewer;
    }

    @Override
    public void createPartControl(Composite parent) {
        DatabaseSession currentSession = KSATRoot.defaultInstance().getCurrentSession();
        TableAndViewer tAndv = buildTable(parent, 
            new TableViewer(parent, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI |
                SWT.FULL_SELECTION), false, currentSession);
        table = tAndv.table;
        tableViewer = tAndv.tableViewer;
        hookContextMenu(); 
    }
    /**
     * Setup Context Menu.
     */
    private void hookContextMenu() {
        MenuManager menuManager = new MenuManager("#PopupMenu");
        menuManager.setRemoveAllWhenShown(true);
        Menu menu = menuManager.createContextMenu(tableViewer.getControl());
        tableViewer.getControl().setMenu(menu);
        menuManager.createContextMenu(tableViewer.getControl());
        getSite().registerContextMenu(ID, menuManager, tableViewer);
    }

    @Override
    public void setFocus() {
    }

    public void setSites(List<Site> sites) {
        table.clearAll();
        tableViewer.refresh();
        tableViewer.add(sites.toArray());
        table.setTopIndex(table.getItemCount());
    }

    static public TableAndViewer buildTable(Composite outerContainer, TableViewer tableViewer,
        boolean addSelectDeSelectButtons, DatabaseSession currentSession) {
        final Table table;
        final TableViewer myTableViewer;
        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 10;
        outerContainer.setLayout(layout);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.grabExcessHorizontalSpace = true;
        outerContainer.setLayoutData(data);
        if (tableViewer == null) {
            myTableViewer = CheckboxTableViewer.newCheckList(outerContainer, SWT.BORDER | SWT.V_SCROLL | 
            SWT.MULTI | SWT.FULL_SELECTION);
            tableViewer = myTableViewer;
        }
        else {
            myTableViewer = tableViewer;
        }
        tableViewer.setColumnProperties(SITE_COLUMN_HEADINGS);
        tableViewer.setLabelProvider(new ITableLabelProvider() {
            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }
            public String getColumnText(Object element, int columnIndex) {
                Site site = (Site)element;
                switch (columnIndex) {
                    case 0:
                        return site.getUrl();
                    case 1:
                        return site.getDescription();
                }
                return null;
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
        table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.heightHint = 300;
        table.setLayoutData(data);
        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);
        tableLayout.addColumnData(new ColumnWeightData(10, 100, true));
        TableColumn urlColumn = new TableColumn(table, SWT.NONE);
        urlColumn.setText(SITE_COLUMN_HEADINGS[0]);
        urlColumn.setAlignment(SWT.LEFT);
        tableLayout.addColumnData(new ColumnWeightData(15, 200, true));
        TableColumn descColumn = new TableColumn(table, SWT.NONE);
        descColumn.setText(SITE_COLUMN_HEADINGS[1]);
        descColumn.setAlignment(SWT.LEFT);
        CellEditor[] editors = new CellEditor[2];
        editors[1] = new TextCellEditor(table);
        tableViewer.setCellEditors(editors);
        tableViewer.setCellModifier(new TableCellModifier(SITE_COLUMN_HEADINGS, tableViewer,
            currentSession));
        Composite selectComposite = new Composite(outerContainer, SWT.RIGHT);
        layout = new GridLayout();
        layout.numColumns = 2;
        selectComposite.setLayout(layout);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        outerContainer.setData(data);
        if (addSelectDeSelectButtons) {
            // Select All button
            Button selectButton = createButton(selectComposite, IDialogConstants.SELECT_ALL_ID,
                "Select All", false);
            SelectionListener selectAllListener = new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (myTableViewer instanceof ICheckable) {
                        TableItem[] children = table.getItems();
                        for (int i = 0; i < children.length; i++) {
                            TableItem item = children[i];
                            item.setChecked(true);
                        }
                    }
                }
            };
            selectButton.addSelectionListener(selectAllListener);
            Button deselectButton = createButton(selectComposite, IDialogConstants.DESELECT_ALL_ID,
                "Deselect All", false);
            SelectionListener deselectAllListener = new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (myTableViewer instanceof ICheckable) {
                        TableItem[] children = table.getItems();
                        for (int i = 0; i < children.length; i++) {
                            TableItem item = children[i];
                            item.setChecked(false);
                        }
                    }
                }
            };
            deselectButton.addSelectionListener(deselectAllListener);
        }
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).pack();
        }
        return new TableAndViewer(table, myTableViewer);
    }
    
    public static Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        ((GridLayout)parent.getLayout()).numColumns++;
        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        button.setFont(JFaceResources.getDialogFont());
        button.setData(new Integer(id));
        if (defaultButton) {
            Shell shell = parent.getShell();
            if (shell != null) {
                shell.setDefaultButton(button);
            }
        }
        return button;
    }
    
    public static class TableAndViewer {
        public Table table;
        public TableViewer tableViewer;
        public TableAndViewer(Table table, TableViewer tableViewer) {
            this.table = table;
            this.tableViewer = tableViewer;
        }
    }
}