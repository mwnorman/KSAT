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
package ca.carleton.tim.ksat.persist;

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

//EclipseLink imports
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.platform.xml.XMLPlatformFactory;
import org.eclipse.persistence.sessions.UnitOfWork;

//KSAT imports
import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.model.AnalysisResult;

public class ReportAnalysisOperationModel extends AnalysisOperationModel {

    public static final String REPORT_XML_FORMAT = "xml";
    public static final String REPORT_HTML_FORMAT = "html";
    public static final String REPORT_HTML_XSL = "ca/carleton/tim/ksat/to_html.xsl";
    public static final String REPORT_CSV_XSL = "ca/carleton/tim/ksat/to_csv.xsl";
    public static final String REPORT_CSV_FORMAT = "csv";
    
    protected String reportFormat;
    protected String analysisDescription;
    protected String reportDestination;

    public ReportAnalysisOperationModel() {
        super();
    }

    public String getReportFormat() {
        return reportFormat;
    }
    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    public void build(AnalysisBuilder builder, UnitOfWork uow) {
        try {
            Analysis reportingAnalysis = (Analysis)uow.executeQuery("findByDescription",
                Analysis.class, analysisDescription);
            if (reportingAnalysis != null) {
                AnalysisResult result = reportingAnalysis.getResults().get(0);
                AnalysisReport analysisReport = (AnalysisReport)uow.registerNewObject(new AnalysisReport());
                uow.assignSequenceNumber(analysisReport);
                analysisReport.setDateTime(result.getDateTime());
                analysisReport.setReportingAnalysis(reportingAnalysis);
                XMLContext context = new XMLContext(new AnalysisReportProject());
                XMLMarshaller marshaller = context.createMarshaller();
                File destination = new File(reportDestination);
                if (REPORT_XML_FORMAT.equalsIgnoreCase(reportFormat)) {
                    marshaller.marshal(analysisReport, new StreamResult(destination));
                }
                else if (REPORT_HTML_FORMAT.equalsIgnoreCase(reportFormat)) {
                    Document doc = XMLPlatformFactory.getInstance().getXMLPlatform().createDocument();
                    marshaller.marshal(analysisReport, doc);
                    InputStream xslStream = this.getClass().getClassLoader().getResourceAsStream(REPORT_HTML_XSL);
                    StreamSource xslSource = new StreamSource(xslStream);
                    Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);
                    DOMSource domSource = new DOMSource(doc);
                    transformer.transform(domSource, new StreamResult(destination));
                }
                else if (REPORT_CSV_FORMAT.equalsIgnoreCase(reportFormat)) {
                    Document doc = XMLPlatformFactory.getInstance().getXMLPlatform().createDocument();
                    marshaller.marshal(analysisReport, doc);
                    InputStream cvsStream = this.getClass().getClassLoader().getResourceAsStream(REPORT_CSV_XSL);
                    StreamSource xslSource = new StreamSource(cvsStream);
                    Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);
                    DOMSource domSource = new DOMSource(doc);
                    transformer.transform(domSource, new StreamResult(destination));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        uow.revertAndResume();
    }
}