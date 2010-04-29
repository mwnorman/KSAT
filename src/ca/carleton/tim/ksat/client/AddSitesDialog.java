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
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;

//KSAT domain imports
import ca.carleton.tim.ksat.model.Site;
import static ca.carleton.tim.ksat.client.SitesView.SITE_COLUMN_HEADINGS;

public class AddSitesDialog extends Dialog {

    protected CheckboxTableViewer tableViewer;
    protected Table table;
    protected Vector<Site> allSitesFromDB;
    protected HashSet<Site> additionalSites;
    protected AddSitesHandler addSitesHandler;
    
    public AddSitesDialog(Shell parent) {
        super(parent);
        init();
    }

    public AddSitesDialog(IShellProvider parentShell) {
        super(parentShell);
        init();
    }
    
    public AddSitesDialog(Shell activeShell, AddSitesHandler addSitesHandler) {
        this(activeShell);
        this.addSitesHandler = addSitesHandler;
    }

    @SuppressWarnings("unchecked")
    protected void init() {
        List<IViewPart> views = KSATApplication.getViews(AnalysesView.ID);
        AnalysesView analysesView = (AnalysesView) views.get(0);
        AnalysisAdapter currentAnalysis = analysesView.getCurrentAnalysis();
        List<Site> currentSites = currentAnalysis.getAnalysis().getSites();
        HashSet<Site> currentSitesSet = new HashSet<Site>(currentSites);
        allSitesFromDB = analysesView.getCurrentDatabase().getSession().readAllObjects(Site.class);
        additionalSites = new HashSet<Site>(allSitesFromDB);
        additionalSites.removeAll(currentSitesSet);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.RESIZE | getShellStyle());
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite outerContainer = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 10;
        outerContainer.setLayout(layout);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.grabExcessHorizontalSpace = true;
        outerContainer.setLayoutData(data);
        new Label(outerContainer, SWT.NONE).setText("Available Sites:");
        tableViewer = CheckboxTableViewer.newCheckList(outerContainer, SWT.BORDER | SWT.V_SCROLL | 
            SWT.MULTI | SWT.FULL_SELECTION);
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
        for (Site s : additionalSites) {
            tableViewer.add(s);
            tableViewer.setChecked(s, true);
            table.setTopIndex(table.getItemCount());
        }
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).pack();
        }
        CellEditor[] editors = new CellEditor[2];
        editors[1] = new TextCellEditor(table);
        tableViewer.setCellEditors(editors);
        tableViewer.setCellModifier(new TableCellModifier());
        Composite selectComposite = new Composite(outerContainer, SWT.RIGHT);
        layout = new GridLayout();
        layout.numColumns = 2;
        selectComposite.setLayout(layout);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        outerContainer.setData(data);
        // Select All button
        Button selectButton = createButton(selectComposite, IDialogConstants.SELECT_ALL_ID,
            "Select All", false);
        SelectionListener selectAllListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                tableViewer.setAllChecked(true);
            }
        };
        selectButton.addSelectionListener(selectAllListener);
        Button deselectButton = createButton(selectComposite, IDialogConstants.DESELECT_ALL_ID,
            "Deselect All", false);
        SelectionListener deselectAllListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                tableViewer.setAllChecked(false);
            }
        };
        deselectButton.addSelectionListener(deselectAllListener);

        Composite buttonComposite = new Composite(outerContainer, SWT.NONE);
        FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
        fillLayout.spacing = 10;
        buttonComposite.setLayout(fillLayout);
        Button addButton = new Button(buttonComposite, SWT.PUSH);
        addButton.setText("Import from file");
        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(),
                    SWT.OPEN);
                String sitesFileName = fileDialog.open();
                try {
                    FileReader sitesReader = new FileReader(sitesFileName);
                    BufferedReader input = new BufferedReader(sitesReader);
                    String line = null;
                    while ((line = input.readLine()) != null) {
                        line = line.trim();
                        if (line != "" && !line.startsWith("#")) {
                            // already in database ?
                            Site siteFromDb = scanForExistingSite(line, allSitesFromDB);
                            if (siteFromDb == null) {
                               Site newSite = new Site(line, "");
                               tableViewer.add(newSite);
                               tableViewer.setChecked(newSite, true);
                            }
                        }
                    }
                    table.setTopIndex(table.getItemCount());
                    input.close();
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        new Label(outerContainer, SWT.NONE).setText("New Site URL:");
        final Text text = new Text(outerContainer, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.setText("enter URL of new site");
        text.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                String newSiteUrl = text.getText();
                if (!"enter name of new site".equals(newSiteUrl)) {
                    Site newSite = new Site(newSiteUrl, "");
                    tableViewer.add(newSite);
                    tableViewer.setChecked(newSite, true);
                    table.setTopIndex(table.getItemCount());
                }
            }
        });
        text.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    e.detail = SWT.TRAVERSE_NONE;
                }
            }
        });
        outerContainer.pack();
        return outerContainer;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) { //Ok
            Object[] checkedElements = tableViewer.getCheckedElements();
            for (Object checkedElement : checkedElements) {
                addSitesHandler.addSelectedSite((Site)checkedElement);
            }
        }
        super.buttonPressed(buttonId);
    }

    protected Site scanForExistingSite(String line, List<Site> allSites) {
        Site foundSite = null;
        for (Site site : allSites) {
            if (site.getUrl().equals(line)) {
                foundSite = site;
                break;
            }
        }
        return foundSite;
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