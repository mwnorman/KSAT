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
import java.util.List;
import org.w3c.dom.Document;

//java eXtension imports
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.part.ViewPart;

//EclipseLink imports
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.platform.xml.XMLPlatformFactory;
import org.eclipse.persistence.sessions.UnitOfWork;

//KSAT domain imports
import ca.carleton.tim.ksat.model.Analysis;
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
        List<AnalysisDatabase> databases = KSATRoot.defaultInstance().getDatabases();
        if (databases.size() > 0) {
            currentDatabase = databases.get(0);
        }
    }

    public AnalysisDatabase getCurrentDatabase() {
        return currentDatabase;
    }

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout(SWT.HORIZONTAL));
        analysesViewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL
            | SWT.H_SCROLL);
        analysesViewer.setAutoExpandLevel(3);
        getSite().setSelectionProvider(analysesViewer);
        analysesViewer.setLabelProvider(new AnalysesLabelProvider());
        IViewSite viewSite = getViewSite();
        analysesViewer.setContentProvider(new AnalysesContentProvider(viewSite));
        KSATInvisibleRoot.defaultInstance().parent = viewSite;
        analysesViewer.setInput(viewSite);
        analysesTree = analysesViewer.getTree();

        hookContextMenu();

        analysesViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                Object selectedElement = selection.getFirstElement();
                if (selectedElement instanceof AnalysisDatabase) {
                    currentDatabase = (AnalysisDatabase)selectedElement;
                    BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
                        public void run() {
                            if (currentDatabase.isConnected()) {
                                currentDatabase.disconnect();
                                KSATRoot.defaultInstance().setCurrentSession(null);
                                KSATApplication.resetViewsOnDisconnectFromDatabase();
                            }
                            else {
                                currentDatabase.connect();
                                KSATRoot.defaultInstance().
                                    setCurrentSession(currentDatabase.getSession());
                                KSATApplication.resetViewsOnConnectToDatabase();
                            }
                        }
                    });
                }
            }
        });

        analysesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                Object selectedElement = selection.getFirstElement();
                if (selectedElement instanceof AnalysisAdapter) {
                    // TODO - throttle resetting sites/keywords is same analysis as last time was
                    // selected
                    AnalysisAdapter analysisAdapter = (AnalysisAdapter)selectedElement;
                    Analysis currentAnalysis = analysisAdapter.getAnalysis();
                    KSATRoot.defaultInstance().setCurrentAnalysis(currentAnalysis);
                    List<IViewPart> views = KSATApplication.getViews(SitesView.ID, KeywordsView.ID);
                    SitesView sitesView = (SitesView)views.get(0);
                    KeywordsView keywordsView = (KeywordsView)views.get(1);
                    sitesView.setSites(currentAnalysis.getSites());
                    keywordsView.setKeywords(currentAnalysis.getExpressions());
                }
                else if (selectedElement instanceof AnalysisResult) {
                    AnalysisResult analysisResult = (AnalysisResult)selectedElement;
                    UnitOfWork uow = currentDatabase.getSession().acquireUnitOfWork();
                    AnalysisReport analysisReport = (AnalysisReport)uow
                        .registerNewObject(new AnalysisReport());
                    uow.assignSequenceNumber(analysisReport);
                    analysisReport.setDateTime(analysisResult.getDateTime());
                    analysisReport.setReportingAnalysis(analysisResult.getOwner());
                    try {
                        XMLContext context = new XMLContext(new AnalysisReportProject());
                        XMLMarshaller marshaller = context.createMarshaller();
                        Document doc = XMLPlatformFactory.getInstance().getXMLPlatform()
                            .createDocument();
                        marshaller.marshal(analysisReport, doc);
                        InputStream htmlXslStream = this.getClass().getClassLoader()
                            .getResourceAsStream(REPORT_HTML_XSL);
                        StreamSource xslSource = new StreamSource(htmlXslStream);
                        Transformer transformer = TransformerFactory.newInstance().newTransformer(
                            xslSource);
                        DOMSource domSource = new DOMSource(doc);
                        StringWriter htmlStringWriter = new StringWriter();
                        StreamResult htmlStreamResult = new StreamResult(htmlStringWriter);
                        transformer.transform(domSource, htmlStreamResult);
                        StringWriter xmlStringWriter = new StringWriter();
                        marshaller.marshal(analysisReport, xmlStringWriter);
                        List<IViewPart> views = KSATApplication.getViews(ResultsView.ID);
                        ResultsView resultsView = (ResultsView)views.get(0);
                        CTabFolder folder = (CTabFolder)resultsView.browser.getParent();
                        boolean flag = resultsView.browser.setText(htmlStringWriter.toString());
                        if (!flag) {
                            MessageConsoleStream messageStream = KSATRoot.defaultInstance()
                                .getLogConsole().getMessageStream();
                            messageStream.write("aiee!\n");
                            messageStream.flush();
                        }
                        folder.layout(true);
                        resultsView.text.setText(xmlStringWriter.toString());
                        resultsView.text.getParent().layout(true);
                        folder.setSelection(0);
                        folder.forceFocus();
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
        MenuManager menuManager = new MenuManager("#PopupMenu");
        menuManager.setRemoveAllWhenShown(true);
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