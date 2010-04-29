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
import java.util.Arrays;
import java.util.List;

//Graphics (JFaces/SWT) imports
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;

//RCP imports
import org.eclipse.ui.part.ViewPart;

//KSAT domain imports
import ca.carleton.tim.ksat.model.Site;

public class SitesView extends ViewPart {

    public static final String ID = "ca.carleton.tim.ksat.client.views.sites";
    public static final String[] SITE_COLUMN_HEADINGS = {"Url", "Description"};
    
    protected TableViewer tableViewer;
    protected Table table;
    
    public SitesView() {
    }

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout(SWT.HORIZONTAL));
        tableViewer = new TableViewer(parent, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI |
            SWT.FULL_SELECTION);
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
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.heightHint = 250;
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
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).pack();
        }
        CellEditor[] editors = new CellEditor[2];
        editors[1] = new TextCellEditor(table);
        tableViewer.setCellEditors(editors);
        tableViewer.setCellModifier(new TableCellModifier());
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
    
    class TableCellModifier implements ICellModifier {
        List<String> headings = null;
        public TableCellModifier() {
            headings = Arrays.asList(SITE_COLUMN_HEADINGS);
        }
        public boolean canModify(Object element, String property) {
              return true;
        }
        public Object getValue(Object element, String property) {
              Object result = null;
              Site site = (Site)element;
              int columnIndex = headings.indexOf(property);
              switch (columnIndex) {
              case 0:
                    result = site.getUrl();
                    break;
              case 1:
                    result = site.getDescription();
                    break;
              }
              return result;
        }
        public void modify(Object element, String property, Object value) {
              int columnIndex = headings.indexOf(property);
              TableItem tableItem = (TableItem)element;
              Site site = (Site)tableItem.getData();
              switch (columnIndex) {
              case 1:
                    String v = (String)value;
                    if (v.length() > 0) {
                        site.setDescription(v);
                    }
                    break;
              }
              tableViewer.update(site, null);
        }
  }
}
