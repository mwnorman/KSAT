package ca.carleton.tim.ksat.client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import ca.carleton.tim.ksat.model.Site;

public class SitesFromDBPage extends WizardPage {

    final static String[] COLUMN_HEADINGS = {"Url", "Description"};

    protected final HashSet<Site> initialSites;
    protected Vector<Site> allSitesFromDB;
    protected Table table;
    protected CheckboxTableViewer tableViewer;

    protected SitesFromDBPage(String pageName, HashSet<Site> initialSites, Vector<Site> allSitesFromDB) {
        super(pageName);
        this.initialSites = initialSites;
        this.allSitesFromDB = allSitesFromDB;
        setTitle("Add Sites");
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 10;
        container.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.grabExcessHorizontalSpace = true;
        container.setLayoutData(data);

        new Label(container, SWT.NONE).setText("Available Sites:");

        tableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.V_SCROLL | 
            SWT.MULTI | SWT.FULL_SELECTION);
        tableViewer.setColumnProperties(COLUMN_HEADINGS);
        //ListContentProvider lcp = new ListContentProvider();
        //tableViewer.setContentProvider(lcp);
        tableViewer.setLabelProvider(new TableLabelProvider());
        table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.heightHint = 300;
        table.setLayoutData(data);
        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);
        tableLayout.addColumnData(new ColumnWeightData(10, 100, true));
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(COLUMN_HEADINGS[0]);
        column.setAlignment(SWT.LEFT);
        tableLayout.addColumnData(new ColumnWeightData(15, 200, true));
        column = new TableColumn(table, SWT.NONE);
        column.setText(COLUMN_HEADINGS[1]);
        column.setAlignment(SWT.LEFT);
        for (Site s : initialSites) {
            tableViewer.add(s);
            tableViewer.setChecked(s, true);
            table.setTopIndex(table.getItemCount());
        }
        Composite buttonComposite = new Composite(container, SWT.NONE);
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
                               table.setTopIndex(table.getItemCount());
                            }
                        }
                    }
                    input.close();
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        new Label(container, SWT.NONE).setText("New Site URL:");
        final Text text = new Text(container, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.setText("enter URL of new site");
        text.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                String newSiteUrl = text.getText();
                if (!"enter name of new site".equals(newSiteUrl)) {
                    Site newSite = new Site(newSiteUrl, "");
                    tableViewer.add(newSite);
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
        setControl(container);
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

    class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
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
    }

}