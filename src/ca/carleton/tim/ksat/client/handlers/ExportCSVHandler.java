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
package ca.carleton.tim.ksat.client.handlers;

//javase imports
import java.io.File;
import java.io.InputStream;
import org.w3c.dom.Document;

//java eXtension imports
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

//RCP imports
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

//EclipseLink imports
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.platform.xml.XMLPlatformFactory;
import org.eclipse.persistence.sessions.UnitOfWork;

//KSAT (domain) imports
import ca.carleton.tim.ksat.client.KSATRoot;
import ca.carleton.tim.ksat.client.ResultAdapter;
import ca.carleton.tim.ksat.model.AnalysisResult;
import ca.carleton.tim.ksat.persist.AnalysisReport;
import ca.carleton.tim.ksat.persist.AnalysisReportProject;

public class ExportCSVHandler extends AbstractHandler implements IHandler {

    public static final String REPORT_CSV_XSL = "ca/carleton/tim/ksat/to_csv.xsl";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell activeShell = HandlerUtil.getActiveShell(event);
        IStructuredSelection currentSelection = 
            (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
        ResultAdapter resultAdapter = (ResultAdapter)currentSelection.getFirstElement();
        AnalysisResult analysisResult = resultAdapter.getResult();
        FileDialog fileDialog = new FileDialog(activeShell, SWT.SAVE);
        fileDialog.setOverwrite(true);
        fileDialog.setFilterExtensions(new String[]{"*.csv"});
        String csvFileName = fileDialog.open();
        if (csvFileName != null) {
            File csvFile = new File(csvFileName);
            UnitOfWork uow = KSATRoot.defaultInstance().getCurrentSession().acquireUnitOfWork();
            AnalysisReport analysisReport = (AnalysisReport)uow.registerNewObject(new AnalysisReport());
            uow.assignSequenceNumber(analysisReport);
            analysisReport.setDateTime(analysisResult.getDateTime());
            analysisReport.setReportingAnalysis(analysisResult.getOwner());
            XMLContext context = new XMLContext(new AnalysisReportProject());
            XMLMarshaller marshaller = context.createMarshaller();
            Document doc = XMLPlatformFactory.getInstance().getXMLPlatform().createDocument();
            marshaller.marshal(analysisReport, doc);
            InputStream cvsStream = this.getClass().getClassLoader().getResourceAsStream(REPORT_CSV_XSL);
            StreamSource xslSource = new StreamSource(cvsStream);
            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);
                DOMSource domSource = new DOMSource(doc);
                transformer.transform(domSource, new StreamResult(csvFile));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            uow.revertAndResume();
        }
        return null;
    }
}