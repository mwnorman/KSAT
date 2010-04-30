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
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
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
import org.eclipse.swt.widgets.Text;

//KSAT domain imports
import ca.carleton.tim.ksat.client.SitesView.TableAndViewer;
import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.model.Site;

public class AddSitesDialog extends Dialog {

    protected CheckboxTableViewer tableViewer;
    protected Table table;
    protected Vector<Site> allSitesFromDB;
    protected HashSet<Site> additionalSites;
    protected AddSitesHandler addSitesHandler;
    private DatabaseSession session;
    
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
        Analysis currentAnalysis = KSATRoot.defaultInstance().getCurrentAnalysis();
        List<Site> currentSites = currentAnalysis.getSites();
        HashSet<Site> currentSitesSet = new HashSet<Site>(currentSites);
        session = KSATRoot.defaultInstance().getCurrentSession();
        allSitesFromDB = session.readAllObjects(Site.class);
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
        TableAndViewer tAndv = SitesView.buildTable(outerContainer, null, true, session);
        table = tAndv.table;
        tableViewer = (CheckboxTableViewer)tAndv.tableViewer;
        for (Site s : additionalSites) {
            tableViewer.add(s);
            tableViewer.setChecked(s, true);
            table.setTopIndex(table.getItemCount());
        }
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
  
}