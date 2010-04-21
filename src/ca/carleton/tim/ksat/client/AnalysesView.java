/*
 * This software is licensed under the terms of the ISC License.
 * (ISCL http://www.opensource.org/licenses/isc-license.txt
 * It is functionally equivalent to the 2-clause BSD licence,
 * with language "made unnecessary by the Berne convention" removed).
 * 
 * Copyright (c) 2009, Mike Norman
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
import java.io.InputStream;
import java.io.StringWriter;
import org.w3c.dom.Document;

//java eXtension imports
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

//RCP imports
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

//EclipseLink imports
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.platform.xml.XMLPlatformFactory;
import org.eclipse.persistence.sessions.UnitOfWork;

//KSAT domain imports
import ca.carleton.tim.ksat.model.AnalysisResult;
import ca.carleton.tim.ksat.persist.AnalysisReport;
import ca.carleton.tim.ksat.persist.AnalysisReportProject;

public class AnalysesView extends ViewPart {

    public static final String ID = "ca.carleton.tim.ksat.client.views.analyses";
    public static final String REPORT_HTML_XSL = "ca/carleton/tim/ksat/to_html.xsl";

    protected TreeViewer analysesViewer;
    protected Tree analysesTree;
    protected AnalysisDatabase currentDatabase = null;

    public AnalysesView() {
        super();
        currentDatabase = KSATRoot.defaultInstance().getDatabases().get(0);
    }

    public AnalysisDatabase getCurrentDatabase() {
        return currentDatabase;
    }
    public void setCurrentDatabase(AnalysisDatabase currentDatabase) {
        this.currentDatabase = currentDatabase;
    }

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout(SWT.HORIZONTAL));
        analysesViewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        analysesViewer.setAutoExpandLevel(3);
        getSite().setSelectionProvider(analysesViewer);
        analysesViewer.setLabelProvider(new AnalysesLabelProvider());
        IViewSite viewSite = getViewSite();
        analysesViewer.setContentProvider(new AnalysesContentProvider(viewSite));
        KSATInvisibleRoot.defaultInstance().parent = viewSite;
        analysesViewer.setInput(viewSite);
        analysesTree = analysesViewer.getTree();
        
        hookContextMenu();
        
        analysesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                Object selectedElement = selection.getFirstElement();
                if (selectedElement instanceof AnalysisAdapter) {
                    // TODO - throttle resetting sites/keywords is same analysis as last time was selected
                    AnalysisAdapter analysisAdapter = (AnalysisAdapter)selectedElement;
                    IWorkbenchWindow[] workbenchs = PlatformUI.getWorkbench().getWorkbenchWindows();
                    IViewPart sitesView = null;
                    IViewPart keywordsView = null;
                    for (IWorkbenchWindow workbench : workbenchs) {
                        IWorkbenchPage[] pages = workbench.getPages();
                        for (IWorkbenchPage page : pages) {
                            sitesView = page.findView(SitesView.ID);
                            keywordsView = page.findView(KeywordsView.ID);
                        }
                        if (sitesView != null && keywordsView != null) {
                            break;
                        }
                    }
                    ((SitesView)sitesView).setSites(analysisAdapter.getAnalysis().getSites());
                    ((KeywordsView)keywordsView).setKeywords(analysisAdapter.getAnalysis().getExpressions());
                }
                else if (selectedElement instanceof AnalysisResult) {
                    AnalysisResult analysisResult = (AnalysisResult)selectedElement;
                    UnitOfWork uow = currentDatabase.getSession().acquireUnitOfWork();
                    AnalysisReport analysisReport = (AnalysisReport)uow.registerNewObject(new AnalysisReport());
                    uow.assignSequenceNumber(analysisReport);
                    analysisReport.setDateTime(analysisResult.getDateTime());
                    analysisReport.setReportingAnalysis(analysisResult.getOwner());
                    try {
                        XMLContext context = new XMLContext(new AnalysisReportProject());
                        XMLMarshaller marshaller = context.createMarshaller();
                        Document doc = XMLPlatformFactory.getInstance().getXMLPlatform().createDocument();
                        marshaller.marshal(analysisReport, doc);
                        InputStream htmlXslStream = 
                            this.getClass().getClassLoader().getResourceAsStream(REPORT_HTML_XSL);
                        StreamSource xslSource = new StreamSource(htmlXslStream);
                        Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);
                        DOMSource domSource = new DOMSource(doc);
                        StringWriter htmlStringWriter = new StringWriter();
                        StreamResult htmlStreamResult = new StreamResult(htmlStringWriter);
                        transformer.transform(domSource, htmlStreamResult);
                        StringWriter xmlStringWriter = new StringWriter();
                        marshaller.marshal(analysisReport, xmlStringWriter);
                        IViewPart resultsView = null;
                        IWorkbenchWindow[] workbenchs = PlatformUI.getWorkbench().getWorkbenchWindows();
                        for (IWorkbenchWindow workbench : workbenchs) {
                            IWorkbenchPage[] pages = workbench.getPages();
                            for (IWorkbenchPage page : pages) {
                                resultsView = page.findView(ResultsView.ID);
                            }
                            if (resultsView != null) {
                                break;
                            }
                        }
                        if (resultsView != null) {
                            Browser browser = ((ResultsView)resultsView).browser;
                            browser.setText(htmlStringWriter.toString());
                            CTabFolder folder = (CTabFolder)browser.getParent();
                            folder.setSelection(0);
                            folder.forceFocus();
                            Text text = (Text)folder.getItem(1).getControl();
                            text.setText(xmlStringWriter.toString());
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    uow.revertAndResume();
                }
            }
        });
    }

    /**
     * Setup Context Menu.
     */
    private void hookContextMenu() {
        MenuManager menuManager = new MenuManager ("#PopupMenu");
        menuManager.setRemoveAllWhenShown (true);
        Menu menu = menuManager.createContextMenu(analysesViewer.getControl());
        analysesTree.setMenu(menu);
        getSite().registerContextMenu(ID, menuManager, analysesViewer);
    }

    
    @Override
    public void setFocus() {
        analysesViewer.getControl().setFocus();
    }

    static class KSATInvisibleRoot {
        static class KSATInvisibleRootHelper {
            static KSATInvisibleRoot singleton = new KSATInvisibleRoot();
        }
        public static KSATInvisibleRoot defaultInstance() {
            return KSATInvisibleRootHelper.singleton;
        }
        Object parent;
        KSATRoot[] children = new KSATRoot[1];
        KSATInvisibleRoot() {
            children[0] = KSATRoot.defaultInstance();
            children[0].parent = this;
        }
    }
}