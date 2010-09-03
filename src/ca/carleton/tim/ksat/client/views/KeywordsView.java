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
package ca.carleton.tim.ksat.client.views;

//javase imports
import java.net.URLDecoder;
import java.util.List;

//Graphics (JFaces/SWT) imports
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

//RCP imports
import org.eclipse.ui.part.ViewPart;

//KSAT domain imports
import ca.carleton.tim.ksat.client.KSATApplication;
import ca.carleton.tim.ksat.client.KSATRoot;
import ca.carleton.tim.ksat.client.TableAndViewer;
import ca.carleton.tim.ksat.model.KeywordExpression;

public class KeywordsView extends ViewPart {

    public static final String ID = "ca.carleton.tim.ksat.client.views.keywords";
    
    protected Table table;
    public TableViewer tableViewer;
    
    public KeywordsView() {
    }

    public TableViewer getTableViewer() {
        return tableViewer;
    }

    @Override
    public void createPartControl(Composite parent) {
        DatabaseSession currentSession = KSATRoot.defaultInstance().getCurrentSession();
        TableAndViewer tAndv = buildTable(parent, 
            new TableViewer(parent, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI), false, currentSession);
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

    public void setKeywords(List<KeywordExpression> expressions) {
        table.clearAll();
        tableViewer.refresh();
        tableViewer.add(expressions.toArray());
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
            myTableViewer = CheckboxTableViewer.newCheckList(outerContainer, SWT.BORDER | 
                SWT.V_SCROLL | SWT.MULTI);
            tableViewer = myTableViewer;
        }
        else {
            myTableViewer = tableViewer;
        }
        tableViewer.setLabelProvider(new ITableLabelProvider() {
            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }
            public String getColumnText(Object element, int columnIndex) {
            	String columnText = "";
                KeywordExpression ke = (KeywordExpression)element;
                try {
                	columnText = URLDecoder.decode(ke.getExpression(), "UTF-8");
				} catch (Exception e) { 
					e.printStackTrace();
				}
				return columnText;
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
        data.widthHint = 500;
        table.setLayoutData(data);
        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);
        tableLayout.addColumnData(new ColumnWeightData(10, 100, true));
        Composite selectComposite = new Composite(outerContainer, SWT.RIGHT);
        layout = new GridLayout();
        layout.numColumns = 2;
        selectComposite.setLayout(layout);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        outerContainer.setData(data);
        if (addSelectDeSelectButtons) {
            // Select All button
            Button selectButton = KSATApplication.createButton(selectComposite,
                IDialogConstants.SELECT_ALL_ID, "Select All", false);
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
            Button deselectButton = KSATApplication.createButton(selectComposite,
                IDialogConstants.DESELECT_ALL_ID, "Deselect All", false);
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
    
}